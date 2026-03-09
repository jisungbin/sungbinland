package sungbinland.nutrition

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.nutrition.entity.FoodEntity

internal class NutritionGraphStateHolder(
  private val bodyInfoDao: BodyInfoDao,
  private val eatenFoodDao: EatenFoodDao,
  private val foodDao: FoodDao,
  private val nowProvider: () -> LocalDate,
) {
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val selectedPeriodState: MutableStateFlow<NutritionGraphPeriod> =
    MutableStateFlow(NutritionGraphPeriod.THREE_MONTHS)
  private val zoneId: ZoneId = ZoneId.systemDefault()

  internal val state: StateFlow<NutritionGraphState> = scope.launchMolecule(
    mode = RecompositionMode.Immediate,
  ) {
    val selectedPeriod by selectedPeriodState.collectAsState()
    val graphState by produceState(
      initialValue = nutritionGraphLoadingState,
      selectedPeriod,
    ) {
      value = createState(period = selectedPeriod)
    }
    graphState
  }

  internal fun selectPeriod(period: NutritionGraphPeriod) {
    selectedPeriodState.update { period }
  }

  internal fun close() {
    scope.cancel()
  }

  private suspend fun createState(period: NutritionGraphPeriod): NutritionGraphState {
    val now = nowProvider()
    val startDate = now.minusDays(period.days)
    val startOfPeriod = startDate.toStartOfDayDate()
    val endOfToday = now.plusDays(1).toStartOfDayDate()

    val allBodyInfos = bodyInfoDao.getAllBodyInfos()
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

    val sampledDates = sampleDates(startDate = startDate, endDate = now, period = period)
    val weightValues = sampledDates.fastMap { date -> weightByDate[date]?.toFloat() ?: Float.NaN }
    val caloriesValues = sampledDates.fastMap { date -> (caloriesByDate[date] ?: 0).toFloat() }
    val carbsValues = sampledDates.fastMap { date -> (carbsByDate[date] ?: 0).toFloat() }

    val allWeights = weightByDate.values
    val allCalories = caloriesByDate.values
    val allCarbs = carbsByDate.values

    val weightAvg = when {
      allWeights.isEmpty() -> 0.0
      else -> allWeights.average()
    }
    val caloriesAvg = when {
      allCalories.isEmpty() -> 0.0
      else -> allCalories.average()
    }
    val carbsAvg = when {
      allCarbs.isEmpty() -> 0.0
      else -> allCarbs.average()
    }

    val weightMin = allWeights.minOrNull() ?: 0
    val weightMax = allWeights.maxOrNull() ?: 0
    val caloriesMin = allCalories.minOrNull() ?: 0
    val caloriesMax = allCalories.maxOrNull() ?: 0
    val carbsMin = allCarbs.minOrNull() ?: 0
    val carbsMax = allCarbs.maxOrNull() ?: 0

    val halfDays = period.days / 2
    val midDate = now.minusDays(halfDays)
    val recentWeights = allWeights(weightByDate, from = midDate, to = now)
    val previousWeights = allWeights(weightByDate, from = startDate, to = midDate.minusDays(1))
    val recentCalories = allValues(caloriesByDate, from = midDate, to = now)
    val previousCalories = allValues(caloriesByDate, from = startDate, to = midDate.minusDays(1))
    val recentCarbs = allValues(carbsByDate, from = midDate, to = now)
    val previousCarbs = allValues(carbsByDate, from = startDate, to = midDate.minusDays(1))

    val weightDelta = computeDelta(previous = previousWeights.average(), recent = recentWeights.average())
    val caloriesDelta = computeDelta(previous = previousCalories.average(), recent = recentCalories.average())
    val carbsDelta = computeDelta(previous = previousCarbs.average(), recent = recentCarbs.average())

    val yAxisLabels = computeYAxisLabels(min = weightMin, max = weightMax)
    val xAxisLabels = computeXAxisLabels(sampledDates = sampledDates, period = period)

    val normalizedWeight = normalizeMinMax(weightValues)
    val normalizedCalories = normalizeMinMax(caloriesValues)
    val normalizedCarbs = normalizeMinMax(carbsValues)

    return NutritionGraphState(
      selectedPeriod = period,
      chart = NutritionGraphChartState(
        weightPoints = normalizedWeight,
        caloriesPoints = normalizedCalories,
        carbsPoints = normalizedCarbs,
        yAxisLabels = yAxisLabels,
        xAxisLabels = xAxisLabels,
      ),
      stats = persistentListOf(
        NutritionGraphStatCardState(
          name = "체중",
          accentColor = 0xFFE85A4F,
          averageValue = when {
            allWeights.isEmpty() -> "--"
            else -> String.format(Locale.KOREA, "%.1fkg", weightAvg)
          },
          rangeText = when {
            allWeights.isEmpty() -> "데이터 없음"
            else -> "${weightMin} ~ ${weightMax}kg"
          },
          deltaArrow = when {
            weightDelta < 0 -> "↓"
            else -> "↑"
          },
          deltaText = String.format(Locale.KOREA, "%+.1fkg", weightDelta),
          deltaPositive = weightDelta <= 0,
        ),
        NutritionGraphStatCardState(
          name = "칼로리",
          accentColor = 0xFF1E3A5F,
          averageValue = when {
            allCalories.isEmpty() -> "--"
            else -> formatWithThousands(caloriesAvg.roundToInt())
          },
          rangeText = when {
            allCalories.isEmpty() -> "데이터 없음"
            else -> "${formatWithThousands(caloriesMin)} ~ ${formatWithThousands(caloriesMax)}kcal"
          },
          deltaArrow = when {
            caloriesDelta < 0 -> "↓"
            else -> "↑"
          },
          deltaText = String.format(Locale.KOREA, "%+.1f%%", caloriesDelta),
          deltaPositive = caloriesDelta <= 0,
        ),
        NutritionGraphStatCardState(
          name = "탄수화물",
          accentColor = 0xFFFF8400,
          averageValue = when {
            allCarbs.isEmpty() -> "--"
            else -> "${carbsAvg.roundToInt()}g"
          },
          rangeText = when {
            allCarbs.isEmpty() -> "데이터 없음"
            else -> "${carbsMin} ~ ${carbsMax}g"
          },
          deltaArrow = when {
            carbsDelta < 0 -> "↓"
            else -> "↑"
          },
          deltaText = String.format(Locale.KOREA, "%+.1f%%", carbsDelta),
          deltaPositive = carbsDelta <= 0,
        ),
      ),
      detailRows = persistentListOf(
        NutritionGraphDetailRowState(
          name = "체중",
          dotColor = 0xFFE85A4F,
          average = when {
            allWeights.isEmpty() -> "--"
            else -> String.format(Locale.KOREA, "%.1fkg", weightAvg)
          },
          min = when {
            allWeights.isEmpty() -> "--"
            else -> "${weightMin}kg"
          },
          max = when {
            allWeights.isEmpty() -> "--"
            else -> "${weightMax}kg"
          },
        ),
        NutritionGraphDetailRowState(
          name = "칼로리",
          dotColor = 0xFF1E3A5F,
          average = when {
            allCalories.isEmpty() -> "--"
            else -> formatWithThousands(caloriesAvg.roundToInt())
          },
          min = when {
            allCalories.isEmpty() -> "--"
            else -> formatWithThousands(caloriesMin)
          },
          max = when {
            allCalories.isEmpty() -> "--"
            else -> formatWithThousands(caloriesMax)
          },
        ),
        NutritionGraphDetailRowState(
          name = "탄수화물",
          dotColor = 0xFFFF8400,
          average = when {
            allCarbs.isEmpty() -> "--"
            else -> "${carbsAvg.roundToInt()}g"
          },
          min = when {
            allCarbs.isEmpty() -> "--"
            else -> "${carbsMin}g"
          },
          max = when {
            allCarbs.isEmpty() -> "--"
            else -> "${carbsMax}g"
          },
        ),
      ),
    )
  }

  private fun sampleDates(
    startDate: LocalDate,
    endDate: LocalDate,
    period: NutritionGraphPeriod,
  ): List<LocalDate> {
    val totalDays = period.days.toInt()
    val sampleCount = 20.coerceAtMost(totalDays)
    val step = totalDays.toDouble() / sampleCount
    return (0 until sampleCount).map { i ->
      startDate.plusDays((i * step).toLong())
    }
  }

  private fun computeYAxisLabels(min: Int, max: Int): ImmutableList<String> {
    if (min == 0 && max == 0) return persistentListOf("--", "--", "--")
    val range = (max - min).coerceAtLeast(1)
    val step = roundStep(range)
    val roundedMin = (min / step) * step
    val roundedMax = ((max + step - 1) / step) * step
    val mid = (roundedMin + roundedMax) / 2
    return persistentListOf(
      "${roundedMax}kg",
      "$mid",
      "$roundedMin",
    )
  }

  private fun roundStep(range: Int): Int = when {
    range <= 5 -> 1
    range <= 20 -> 5
    range <= 50 -> 10
    else -> 20
  }

  private fun computeXAxisLabels(
    sampledDates: List<LocalDate>,
    period: NutritionGraphPeriod,
  ): ImmutableList<String> {
    if (sampledDates.isEmpty()) return persistentListOf()
    val labelCount = when (period) {
      NutritionGraphPeriod.WEEK -> 4
      NutritionGraphPeriod.MONTH -> 4
      NutritionGraphPeriod.THREE_MONTHS -> 4
      NutritionGraphPeriod.SIX_MONTHS -> 4
      NutritionGraphPeriod.YEAR -> 4
    }
    val step = (sampledDates.size.toDouble() / labelCount).toInt().coerceAtLeast(1)
    val builder = persistentListOf<String>().builder()
    for (i in 0 until labelCount) {
      val index = (i * step).coerceAtMost(sampledDates.lastIndex)
      builder.add("${sampledDates[index].monthValue}월")
    }
    return builder.build()
  }

  private fun normalizeMinMax(values: List<Float>): ImmutableList<Float> {
    val validValues = values.filter { !it.isNaN() }
    if (validValues.isEmpty()) {
      return values.fastMapTo(persistentListOf<Float>().builder()) { Float.NaN }.build()
    }
    val min = validValues.min()
    val max = validValues.max()
    val range = (max - min).coerceAtLeast(1f)
    val padding = range * 0.15f
    val paddedMin = min - padding
    val paddedMax = max + padding
    val paddedRange = paddedMax - paddedMin
    return values.fastMapTo(persistentListOf<Float>().builder()) { value ->
      when {
        value.isNaN() -> Float.NaN
        else -> ((value - paddedMin) / paddedRange).coerceIn(0f, 1f)
      }
    }.build()
  }

  private fun allWeights(
    weightByDate: Map<LocalDate, Int>,
    from: LocalDate,
    to: LocalDate,
  ): List<Int> = weightByDate.entries
    .filter { (date, _) -> date in from..to }
    .map { (_, weight) -> weight }

  private fun allValues(
    valueByDate: Map<LocalDate, Int>,
    from: LocalDate,
    to: LocalDate,
  ): List<Int> = valueByDate.entries
    .filter { (date, _) -> date in from..to }
    .map { (_, value) -> value }

  private fun computeDelta(previous: Double, recent: Double): Double = when {
    previous <= 0.0 -> 0.0
    else -> ((recent - previous) / previous) * 100.0
  }

  private fun List<Int>.average(): Double = when {
    isEmpty() -> 0.0
    else -> sumOf { it.toLong() }.toDouble() / size
  }

  private fun formatWithThousands(value: Int): String =
    String.format(Locale.KOREA, "%,d", value)

  private fun Date.toLocalDate(): LocalDate =
    toInstant().atZone(zoneId).toLocalDate()

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())
}
