package sungbinland.nutrition

import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapTo
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.nutrition.entity.BodyInfoEntity
import sungbinland.core.nutrition.entity.EatenFoodEntity
import sungbinland.core.nutrition.entity.FoodEntity

internal class NutritionDashboardStateMapper(
  private val bodyInfoDao: BodyInfoDao,
  private val eatenFoodDao: EatenFoodDao,
  private val foodDao: FoodDao,
) {
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  internal suspend fun createState(selectedDate: LocalDate): NutritionDashboardState {
    val nowDate = LocalDate.now(zoneId)
    val selectedStartOfDay = selectedDate.toStartOfDayDate(zoneId = zoneId)
    val selectedEndOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate(zoneId = zoneId)
    val previousWeekStartOfDay = selectedDate.minusDays(14).toStartOfDayDate(zoneId = zoneId)
    val previousWeekEndOfDayExclusive = selectedDate.minusDays(6).toStartOfDayDate(zoneId = zoneId)
    val recentTrendStartOfDay = nowDate.minusDays(6).toStartOfDayDate(zoneId = zoneId)
    val recentTrendEndOfDayExclusive = nowDate.plusDays(1).toStartOfDayDate(zoneId = zoneId)
    val previousTrendStartOfDay = nowDate.minusDays(13).toStartOfDayDate(zoneId = zoneId)
    val previousTrendEndOfDayExclusive = nowDate.minusDays(6).toStartOfDayDate(zoneId = zoneId)

    val snapshot = coroutineScope {
      val foodsDeferred = async { foodDao.getAllFoods().associateBy { it.name } }
      val eatenFoodsOfDateDeferred = async {
        eatenFoodDao.getEatenFoodsByDate(
          startOfDay = selectedStartOfDay,
          endOfDayExclusive = selectedEndOfDayExclusive,
        )
      }
      val bodyInfoOfDateDeferred = async {
        bodyInfoDao.getBodyInfoByExactDate(
          date = selectedStartOfDay,
        )
      }
      val previousWeekBodyInfosDeferred = async {
        bodyInfoDao.getBodyInfosByDate(
          startOfDay = previousWeekStartOfDay,
          endOfDayExclusive = previousWeekEndOfDayExclusive,
        )
      }
      val recentEatenFoodsDeferred = async {
        eatenFoodDao.getEatenFoodsByDate(
          startOfDay = recentTrendStartOfDay,
          endOfDayExclusive = recentTrendEndOfDayExclusive,
        )
      }
      val previousEatenFoodsDeferred = async {
        eatenFoodDao.getEatenFoodsByDate(
          startOfDay = previousTrendStartOfDay,
          endOfDayExclusive = previousTrendEndOfDayExclusive,
        )
      }
      NutritionQuerySnapshot(
        foodsByName = foodsDeferred.await(),
        eatenFoodsOfDate = eatenFoodsOfDateDeferred.await(),
        bodyInfoOfDate = bodyInfoOfDateDeferred.await(),
        previousWeekBodyInfos = previousWeekBodyInfosDeferred.await(),
        recentEatenFoods = recentEatenFoodsDeferred.await(),
        previousEatenFoods = previousEatenFoodsDeferred.await(),
      )
    }
    val foods = snapshot.foodsByName
    val eatenFoodsOfDate = snapshot.eatenFoodsOfDate
    val bodyInfoOfDate = snapshot.bodyInfoOfDate
    val previousWeekBodyInfos = snapshot.previousWeekBodyInfos
    val recentEatenFoods = snapshot.recentEatenFoods
    val previousEatenFoods = snapshot.previousEatenFoods

    var totalCalories = 0
    var totalCarbohydrate = 0
    var totalProtein = 0
    eatenFoodsOfDate.fastForEach { eatenFood ->
      val food = foods[eatenFood.foodName] ?: return@fastForEach
      totalCalories += food.calories * eatenFood.quantity
      totalCarbohydrate += food.carbohydrateGrams * eatenFood.quantity
      totalProtein += food.proteinGrams * eatenFood.quantity
    }

    val bodyWeightKg = bodyInfoOfDate?.bodyWeightKg
    val goalCalories = bodyWeightKg?.let { weightKg ->
      (weightKg.toDouble() * 30.0 * 1.2).roundToInt()
    } ?: 2400
    val goalCarbohydrate = bodyWeightKg?.let { weightKg ->
      (weightKg.toDouble() * 2.0 * 1.2).roundToInt()
    } ?: 160
    val goalProtein = bodyWeightKg?.times(2) ?: 110
    val progressPercent = when {
      goalCalories == 0 -> 0
      else -> {
        ((totalCalories.toDouble() / goalCalories.toDouble()) * 100.0)
          .roundToInt()
          .coerceAtLeast(0)
      }
    }

    val recentDates: List<LocalDate> = (6 downTo 0).map { daysAgo ->
      nowDate.minusDays(daysAgo.toLong())
    }
    val previousDates: List<LocalDate> = (13 downTo 7).map { daysAgo ->
      nowDate.minusDays(daysAgo.toLong())
    }
    val recentDailyCalories = dailyCalories(
      allEatenFoods = recentEatenFoods,
      foodsByName = foods,
    )
    val previousDailyCalories = dailyCalories(
      allEatenFoods = previousEatenFoods,
      foodsByName = foods,
    )
    val recentTrendValues = recentDates.fastMap { date -> recentDailyCalories[date] ?: 0 }
    val previousTrendValues = previousDates.fastMap { date -> previousDailyCalories[date] ?: 0 }
    val trendValues = recentDates.fastMapTo(persistentListOf<NutritionTrendValueState>().builder()) { date ->
      val calories = recentDailyCalories[date] ?: 0
      NutritionTrendValueState(
        label = "${date.monthValue}/${date.dayOfMonth}",
        value = "${formatWithThousands(value = calories)} kcal",
      )
    }.build()
    val trendDelta = formatTrendDelta(
      previousAverage = previousTrendValues.average(),
      recentAverage = recentTrendValues.average(),
    )

    val timelineItems = eatenFoodsOfDate.asReversed().fastMapTo(persistentListOf<NutritionTimelineItemState>().builder()) { eatenFood ->
      val food = foods[eatenFood.foodName]
      val calories = (food?.calories ?: 0) * eatenFood.quantity
      val carbs = (food?.carbohydrateGrams ?: 0) * eatenFood.quantity
      val protein = (food?.proteinGrams ?: 0) * eatenFood.quantity
      val consumedAt = eatenFood.consumedAt.toInstant().atZone(zoneId).toLocalDateTime()
      NutritionTimelineItemState(
        title = "${consumedAt.format(timeFormatter)}  ${eatenFood.foodName} x${eatenFood.quantity}",
        subtitle = "탄 ${carbs}g · 단 ${protein}g",
        calorieText = "$calories kcal",
      )
    }.build()

    return NutritionDashboardState(
      summary = NutritionSummaryState(
        dayTag = dayTag(selectedDate = selectedDate, nowDate = nowDate),
        displayDate = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
        headline = when {
          selectedDate == nowDate -> "오늘 ${formatWithThousands(value = totalCalories)} kcal"
          else -> "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 ${formatWithThousands(value = totalCalories)} kcal"
        },
        totalCaloriesValue = formatWithThousands(value = totalCalories),
        progressPercent = progressPercent,
        progressMeta = "목표 ${formatWithThousands(value = goalCalories)} kcal",
        trendDelta = trendDelta,
        trendValues = trendValues,
      ),
      macroCards = persistentListOf(
        NutritionMacroCardState(
          title = "탄수화물",
          value = "${totalCarbohydrate}g",
          meta = "목표 ${goalCarbohydrate}g",
          highlighted = false,
          goalAchieved = totalCarbohydrate >= goalCarbohydrate,
        ),
        NutritionMacroCardState(
          title = "단백질",
          value = "${totalProtein}g",
          meta = "목표 ${goalProtein}g",
          highlighted = false,
          goalAchieved = totalProtein >= goalProtein,
        ),
        NutritionMacroCardState(
          title = "체중",
          value = bodyWeightKg?.let { weight -> "${weight}kg" } ?: "--kg",
          meta = weightDeltaMeta(
            currentWeightKg = bodyWeightKg,
            previousWeekBodyInfos = previousWeekBodyInfos,
          ),
          highlighted = true,
        ),
      ),
      timeline = NutritionTimelineState(
        meta = when {
          selectedDate == nowDate -> "오늘 ${timelineItems.size}건"
          else -> "${timelineItems.size}건"
        },
        items = timelineItems,
      ),
    )
  }

  private fun dailyCalories(
    allEatenFoods: List<EatenFoodEntity>,
    foodsByName: Map<String, FoodEntity>,
  ): Map<LocalDate, Int> {
    val caloriesByDate = mutableMapOf<LocalDate, Int>()
    allEatenFoods.fastForEach { eatenFood ->
      val food = foodsByName[eatenFood.foodName] ?: return@fastForEach
      val date = eatenFood.consumedAt.toLocalDate(zoneId = zoneId)
      val calories = food.calories * eatenFood.quantity
      caloriesByDate[date] = (caloriesByDate[date] ?: 0) + calories
    }
    return caloriesByDate
  }

  private fun formatTrendDelta(
    previousAverage: Double,
    recentAverage: Double,
  ): String {
    if (previousAverage <= 0.0) {
      return "+0.0%"
    }
    val delta = ((recentAverage - previousAverage) / previousAverage) * 100.0
    return String.format(Locale.KOREA, "%+.1f%%", delta)
  }

  private fun formatWithThousands(value: Int): String =
    String.format(Locale.KOREA, "%,d", value)

  private fun Date.toLocalDate(zoneId: ZoneId): LocalDate =
    toInstant().atZone(zoneId).toLocalDate()

  private fun dayTag(selectedDate: LocalDate, nowDate: LocalDate): String {
    val diff = ChronoUnit.DAYS.between(nowDate, selectedDate)
    return when {
      diff == 0L -> "오늘"
      diff > 0L -> "${diff}일 후"
      else -> "${-diff}일 전"
    }
  }

  private fun LocalDate.toStartOfDayDate(zoneId: ZoneId): Date =
    Date.from(atStartOfDay(zoneId).toInstant())

  private fun weightDeltaMeta(
    currentWeightKg: Int?,
    previousWeekBodyInfos: List<BodyInfoEntity>,
  ): String {
    if (currentWeightKg == null) return "기록 없음"
    val previousWeekWeight = previousWeekBodyInfos
      .maxByOrNull { it.recordedAt.time }
      ?.bodyWeightKg
      ?: return "비교 데이터 없음"
    val delta = currentWeightKg - previousWeekWeight
    val sign = when { delta >= 0 -> "+"; else -> "-" }
    return "이번 주 ${sign}${delta.absoluteValue}kg"
  }

  private class NutritionQuerySnapshot(
    val foodsByName: Map<String, FoodEntity>,
    val eatenFoodsOfDate: List<EatenFoodEntity>,
    val bodyInfoOfDate: BodyInfoEntity?,
    val previousWeekBodyInfos: List<BodyInfoEntity>,
    val recentEatenFoods: List<EatenFoodEntity>,
    val previousEatenFoods: List<EatenFoodEntity>,
  )
}
