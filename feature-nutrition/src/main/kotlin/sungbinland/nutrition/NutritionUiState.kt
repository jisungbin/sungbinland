package sungbinland.nutrition

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import java.time.LocalDate

@Immutable @Poko internal class NutritionDashboardState(
  val summary: NutritionSummaryState,
  val macroCards: List<NutritionMacroCardState>,
  val timeline: NutritionTimelineState,
)

@Immutable @Poko internal class NutritionSummaryState(
  val dayTag: String,
  val displayDate: String,
  val headline: String,
  val totalCaloriesValue: String,
  val progressPercent: Int,
  val progressMeta: String,
  val trendDelta: String,
  val trendValues: List<NutritionTrendValueState>,
)

@Immutable @Poko internal class NutritionTrendValueState(
  val label: String,
  val value: String,
)

@Immutable @Poko internal class NutritionMacroCardState(
  val title: String,
  val value: String,
  val meta: String,
  val highlighted: Boolean,
)

@Immutable @Poko internal class NutritionTimelineState(
  val meta: String,
  val items: List<NutritionTimelineItemState>,
)

@Immutable @Poko internal class NutritionTimelineItemState(
  val title: String,
  val subtitle: String,
  val calorieText: String,
)

internal fun nutritionDashboardLoadingState(selectedDate: LocalDate): NutritionDashboardState =
  NutritionDashboardState(
    summary = NutritionSummaryState(
      dayTag = "TODAY",
      displayDate = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
      headline = "오늘 0 kcal",
      totalCaloriesValue = "0",
      progressPercent = 0,
      progressMeta = "목표 2,400 kcal",
      trendDelta = "+0.0%",
      trendValues = (6 downTo 0).map { dayOffset ->
        val date = selectedDate.minusDays(dayOffset.toLong())
        NutritionTrendValueState(
          label = "${date.monthValue}/${date.dayOfMonth}",
          value = "0 kcal",
        )
      },
    ),
    macroCards = listOf(
      NutritionMacroCardState(
        title = "탄수화물",
        value = "0g",
        meta = "목표 160g",
        highlighted = false,
      ),
      NutritionMacroCardState(
        title = "단백질",
        value = "0g",
        meta = "목표 110g",
        highlighted = false,
      ),
      NutritionMacroCardState(
        title = "체중",
        value = "--kg",
        meta = "기록 없음",
        highlighted = true,
      ),
    ),
    timeline = NutritionTimelineState(
      meta = "오늘 0건",
      items = emptyList(),
    ),
  )
