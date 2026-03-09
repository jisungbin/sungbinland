package sungbinland.nutrition

import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapTo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.nutrition.entity.FoodEntity

@OptIn(ExperimentalCoroutinesApi::class)
internal class NutritionGraphViewModel(
  private val bodyInfoDao: BodyInfoDao,
  private val eatenFoodDao: EatenFoodDao,
  private val foodDao: FoodDao,
) : ViewModel() {
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val selectedPeriodState: MutableStateFlow<NutritionGraphPeriod> =
    MutableStateFlow(NutritionGraphPeriod.THREE_MONTHS)

  internal val state: StateFlow<NutritionGraphState> =
    selectedPeriodState
      .mapLatest { period -> createState(period = period) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = nutritionGraphLoadingState,
      )

  internal fun selectPeriod(period: NutritionGraphPeriod) {
    selectedPeriodState.update { period }
  }

  private suspend fun createState(period: NutritionGraphPeriod): NutritionGraphState {
    val now = LocalDate.now()
    val startDate = now.minusDays(period.days)
    val startOfPeriod = startDate.toStartOfDayDate()
    val endOfToday = now.plusDays(1).toStartOfDayDate()

    val allBodyInfos = bodyInfoDao.getBodyInfosByDate(
      startOfDay = startOfPeriod,
      endOfDayExclusive = endOfToday,
    )
    val allEatenFoods = eatenFoodDao.getEatenFoodsByDate(
      startOfDay = startOfPeriod,
      endOfDayExclusive = endOfToday,
    )
    val foods = foodDao.getAllFoods().associateBy(FoodEntity::name)

    val weightByDate = buildMap<LocalDate, Int> {
      allBodyInfos.fastForEach { info ->
        val date = info.recordedAt.toLocalDate()
        if (date >= startDate && date <= now) {
          val existing = get(date)
          if (existing == null || info.recordedAt.time > 0) {
            put(date, info.bodyWeightKg)
          }
        }
      }
    }
    val caloriesByDate = mutableMapOf<LocalDate, Int>()
    val carbsByDate = mutableMapOf<LocalDate, Int>()
    allEatenFoods.fastForEach { eatenFood ->
      val food = foods[eatenFood.foodName] ?: return@fastForEach
      val date = eatenFood.consumedAt.toLocalDate()
      caloriesByDate[date] = (caloriesByDate[date] ?: 0) + food.calories * eatenFood.quantity
      carbsByDate[date] = (carbsByDate[date] ?: 0) + food.carbohydrateGrams * eatenFood.quantity
    }

    val sampledDates = sampleDates(startDate = startDate, period = period)
    val weightValues = sampledDates.fastMap { date -> weightByDate[date]?.toFloat() ?: Float.NaN }
    val caloriesValues = sampledDates.fastMap { date -> (caloriesByDate[date] ?: 0).toFloat() }
    val carbsValues = sampledDates.fastMap { date -> (carbsByDate[date] ?: 0).toFloat() }

    val allWeights = weightByDate.values
    val allCalories = caloriesByDate.values
    val allCarbs = carbsByDate.values

    val weightAvg = when { allWeights.isEmpty() -> 0.0; else -> allWeights.average() }
    val caloriesAvg = when { allCalories.isEmpty() -> 0.0; else -> allCalories.average() }
    val carbsAvg = when { allCarbs.isEmpty() -> 0.0; else -> allCarbs.average() }

    val weightMin = allWeights.minOrNull() ?: 0
    val weightMax = allWeights.maxOrNull() ?: 0
    val caloriesMin = allCalories.minOrNull() ?: 0
    val caloriesMax = allCalories.maxOrNull() ?: 0
    val carbsMin = allCarbs.minOrNull() ?: 0
    val carbsMax = allCarbs.maxOrNull() ?: 0

    val halfDays = period.days / 2
    val midDate = now.minusDays(halfDays)
    val recentWeights = filterValues(weightByDate, from = midDate, to = now)
    val previousWeights = filterValues(weightByDate, from = startDate, to = midDate.minusDays(1))
    val recentCalories = filterValues(caloriesByDate, from = midDate, to = now)
    val previousCalories = filterValues(caloriesByDate, from = startDate, to = midDate.minusDays(1))
    val recentCarbs = filterValues(carbsByDate, from = midDate, to = now)
    val previousCarbs = filterValues(carbsByDate, from = startDate, to = midDate.minusDays(1))

    val weightDelta = computeDelta(previous = previousWeights.avg(), recent = recentWeights.avg())
    val caloriesDelta = computeDelta(previous = previousCalories.avg(), recent = recentCalories.avg())
    val carbsDelta = computeDelta(previous = previousCarbs.avg(), recent = recentCarbs.avg())

    val yAxisLabels = computeYAxisLabels(min = weightMin, max = weightMax)
    val xAxisLabels = computeXAxisLabels(sampledDates = sampledDates, period = period)

    return NutritionGraphState(
      selectedPeriod = period,
      chart = NutritionGraphChartState(
        weightPoints = normalizeMinMax(weightValues),
        caloriesPoints = normalizeMinMax(caloriesValues),
        carbsPoints = normalizeMinMax(carbsValues),
        yAxisLabels = yAxisLabels,
        xAxisLabels = xAxisLabels,
      ),
      stats = persistentListOf(
        NutritionGraphStatCardState(name = "체중", accentColor = 0xFFE85A4F, averageValue = when { allWeights.isEmpty() -> "--"; else -> String.format(Locale.KOREA, "%.1fkg", weightAvg) }, rangeText = when { allWeights.isEmpty() -> "데이터 없음"; else -> "${weightMin} ~ ${weightMax}kg" }, deltaArrow = when { weightDelta < 0 -> "↓"; else -> "↑" }, deltaText = String.format(Locale.KOREA, "%+.1f%%", weightDelta), deltaPositive = weightDelta <= 0),
        NutritionGraphStatCardState(name = "칼로리", accentColor = 0xFF1E3A5F, averageValue = when { allCalories.isEmpty() -> "--"; else -> fmtK(caloriesAvg.roundToInt()) }, rangeText = when { allCalories.isEmpty() -> "데이터 없음"; else -> "${fmtK(caloriesMin)} ~ ${fmtK(caloriesMax)}kcal" }, deltaArrow = when { caloriesDelta < 0 -> "↓"; else -> "↑" }, deltaText = String.format(Locale.KOREA, "%+.1f%%", caloriesDelta), deltaPositive = caloriesDelta <= 0),
        NutritionGraphStatCardState(name = "탄수화물", accentColor = 0xFF20B07A, averageValue = when { allCarbs.isEmpty() -> "--"; else -> "${carbsAvg.roundToInt()}g" }, rangeText = when { allCarbs.isEmpty() -> "데이터 없음"; else -> "${carbsMin} ~ ${carbsMax}g" }, deltaArrow = when { carbsDelta < 0 -> "↓"; else -> "↑" }, deltaText = String.format(Locale.KOREA, "%+.1f%%", carbsDelta), deltaPositive = carbsDelta <= 0),
      ),
      detailRows = persistentListOf(
        NutritionGraphDetailRowState(name = "체중", dotColor = 0xFFE85A4F, average = when { allWeights.isEmpty() -> "--"; else -> String.format(Locale.KOREA, "%.1fkg", weightAvg) }, min = when { allWeights.isEmpty() -> "--"; else -> "${weightMin}kg" }, max = when { allWeights.isEmpty() -> "--"; else -> "${weightMax}kg" }),
        NutritionGraphDetailRowState(name = "칼로리", dotColor = 0xFF1E3A5F, average = when { allCalories.isEmpty() -> "--"; else -> fmtK(caloriesAvg.roundToInt()) }, min = when { allCalories.isEmpty() -> "--"; else -> fmtK(caloriesMin) }, max = when { allCalories.isEmpty() -> "--"; else -> fmtK(caloriesMax) }),
        NutritionGraphDetailRowState(name = "탄수화물", dotColor = 0xFF20B07A, average = when { allCarbs.isEmpty() -> "--"; else -> "${carbsAvg.roundToInt()}g" }, min = when { allCarbs.isEmpty() -> "--"; else -> "${carbsMin}g" }, max = when { allCarbs.isEmpty() -> "--"; else -> "${carbsMax}g" }),
      ),
    )
  }

  private fun sampleDates(startDate: LocalDate, period: NutritionGraphPeriod): List<LocalDate> {
    val totalDays = period.days.toInt()
    val sampleCount = 20.coerceAtMost(totalDays)
    val step = totalDays.toDouble() / sampleCount
    return (0 until sampleCount).map { i -> startDate.plusDays((i * step).toLong()) }
  }

  private fun computeYAxisLabels(min: Int, max: Int): ImmutableList<String> {
    if (min == 0 && max == 0) return persistentListOf("--", "--", "--")
    val range = (max - min).coerceAtLeast(1)
    val step = when { range <= 5 -> 1; range <= 20 -> 5; range <= 50 -> 10; else -> 20 }
    val roundedMin = (min / step) * step
    val roundedMax = ((max + step - 1) / step) * step
    return persistentListOf("${roundedMax}kg", "${(roundedMin + roundedMax) / 2}", "$roundedMin")
  }

  private fun computeXAxisLabels(sampledDates: List<LocalDate>, period: NutritionGraphPeriod): ImmutableList<String> {
    if (sampledDates.isEmpty()) return persistentListOf()
    val step = (sampledDates.size.toDouble() / 4).toInt().coerceAtLeast(1)
    return buildList {
      for (i in 0 until 4) {
        val index = (i * step).coerceAtMost(sampledDates.lastIndex)
        add("${sampledDates[index].monthValue}월")
      }
    }.toImmutableList()
  }

  private fun normalizeMinMax(values: List<Float>): ImmutableList<Float> {
    val valid = values.filter { !it.isNaN() }
    if (valid.isEmpty()) return values.fastMapTo(persistentListOf<Float>().builder()) { Float.NaN }.build()
    val min = valid.min(); val max = valid.max()
    val range = (max - min).coerceAtLeast(1f)
    val pad = range * 0.15f; val pMin = min - pad; val pRange = max + pad - pMin
    return values.fastMapTo(persistentListOf<Float>().builder()) { v ->
      when { v.isNaN() -> Float.NaN; else -> ((v - pMin) / pRange).coerceIn(0f, 1f) }
    }.build()
  }

  private fun filterValues(map: Map<LocalDate, Int>, from: LocalDate, to: LocalDate): List<Int> =
    map.entries.filter { (d, _) -> d in from..to }.map { (_, v) -> v }

  private fun computeDelta(previous: Double, recent: Double): Double =
    when { previous <= 0.0 -> 0.0; else -> ((recent - previous) / previous) * 100.0 }

  private fun List<Int>.avg(): Double = when { isEmpty() -> 0.0; else -> sumOf { it.toLong() }.toDouble() / size }

  private fun fmtK(value: Int): String = String.format(Locale.KOREA, "%,d", value)

  private fun Date.toLocalDate(): LocalDate = toInstant().atZone(zoneId).toLocalDate()

  private fun LocalDate.toStartOfDayDate(): Date = Date.from(atStartOfDay(zoneId).toInstant())
}
