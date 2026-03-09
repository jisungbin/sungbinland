package sungbinland.nutrition

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import androidx.compose.ui.util.fastMapTo
import kotlinx.collections.immutable.persistentListOf
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
    val nowDate: LocalDate = LocalDate.now(zoneId)
    val foods = foodDao.getAllFoods().associateBy { it.name }
    val eatenFoodsOfDate = eatenFoodDao.getEatenFoodsByDate(
      startOfDay = selectedDate.toStartOfDayDate(zoneId = zoneId),
      endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate(zoneId = zoneId),
    )
    val allBodyInfos = bodyInfoDao.getAllBodyInfos()

    var totalCalories = 0
    var totalCarbohydrate = 0
    var totalProtein = 0
    eatenFoodsOfDate.forEach { eatenFood ->
      val food = foods[eatenFood.foodName] ?: return@forEach
      totalCalories += food.calories * eatenFood.quantity
      totalCarbohydrate += food.carbohydrateGrams * eatenFood.quantity
      totalProtein += food.proteinGrams * eatenFood.quantity
    }

    val bodyWeightKg = allBodyInfos.bodyWeightOf(date = selectedDate, zoneId = zoneId)
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
    val recentEatenFoods = eatenFoodDao.getEatenFoodsByDate(
      startOfDay = nowDate.minusDays(6).toStartOfDayDate(zoneId = zoneId),
      endOfDayExclusive = nowDate.plusDays(1).toStartOfDayDate(zoneId = zoneId),
    )
    val previousEatenFoods = eatenFoodDao.getEatenFoodsByDate(
      startOfDay = nowDate.minusDays(13).toStartOfDayDate(zoneId = zoneId),
      endOfDayExclusive = nowDate.minusDays(6).toStartOfDayDate(zoneId = zoneId),
    )
    val recentDailyCalories = dailyCalories(
      allEatenFoods = recentEatenFoods,
      foodsByName = foods,
    )
    val previousDailyCalories = dailyCalories(
      allEatenFoods = previousEatenFoods,
      foodsByName = foods,
    )
    val recentTrendValues = recentDates.map { date -> recentDailyCalories[date] ?: 0 }
    val previousTrendValues = previousDates.map { date -> previousDailyCalories[date] ?: 0 }
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

    val timelineItems = eatenFoodsOfDate.fastMapTo(persistentListOf<NutritionTimelineItemState>().builder()) { eatenFood ->
      val calories = (foods[eatenFood.foodName]?.calories ?: 0) * eatenFood.quantity
      val consumedAt = eatenFood.consumedAt.toInstant().atZone(zoneId).toLocalDateTime()
      NutritionTimelineItemState(
        title = "${consumedAt.format(timeFormatter)}  ${eatenFood.foodName} x${eatenFood.quantity}",
        subtitle = consumedAt.hour.toMealLabel(),
        calorieText = "$calories kcal",
      )
    }.build()

    return NutritionDashboardState(
      summary = NutritionSummaryState(
        dayTag = if (selectedDate == nowDate) "TODAY" else "DAY",
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
        ),
        NutritionMacroCardState(
          title = "단백질",
          value = "${totalProtein}g",
          meta = "목표 ${goalProtein}g",
          highlighted = false,
        ),
        NutritionMacroCardState(
          title = "체중",
          value = bodyWeightKg?.let { weight -> "${weight}kg" } ?: "--kg",
          meta = allBodyInfos.weightDeltaMeta(
            selectedDate = selectedDate,
            currentWeightKg = bodyWeightKg,
            zoneId = zoneId,
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
    allEatenFoods.forEach { eatenFood ->
      val food = foodsByName[eatenFood.foodName] ?: return@forEach
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

  private fun LocalDate.toStartOfDayDate(zoneId: ZoneId): Date =
    Date.from(atStartOfDay(zoneId).toInstant())

  private fun Int.toMealLabel(): String = when (this) {
    in 5..10 -> "아침"
    in 11..15 -> "점심"
    in 16..20 -> "간식"
    else -> "저녁"
  }

  private fun List<BodyInfoEntity>.bodyWeightOf(
    date: LocalDate,
    zoneId: ZoneId,
  ): Int? = filter { bodyInfo ->
    bodyInfo.recordedAt.toLocalDate(zoneId = zoneId) == date
  }.maxByOrNull { bodyInfo ->
    bodyInfo.recordedAt.time
  }?.bodyWeightKg

  private fun List<BodyInfoEntity>.weightDeltaMeta(
    selectedDate: LocalDate,
    currentWeightKg: Int?,
    zoneId: ZoneId,
  ): String {
    if (currentWeightKg == null) {
      return "기록 없음"
    }
    val previousWeekWeight = filter { bodyInfo ->
      bodyInfo.recordedAt.toLocalDate(zoneId = zoneId) <= selectedDate.minusDays(7)
    }.maxByOrNull { bodyInfo ->
      bodyInfo.recordedAt.time
    }?.bodyWeightKg ?: return "비교 데이터 없음"
    val delta = currentWeightKg - previousWeekWeight
    val sign = if (delta >= 0) "+" else "-"
    return "이번 주 ${sign}${delta.absoluteValue}kg"
  }
}
