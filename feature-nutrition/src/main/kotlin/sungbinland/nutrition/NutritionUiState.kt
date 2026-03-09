package sungbinland.nutrition

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
@Poko internal class NutritionDashboardState(
  internal val summary: NutritionSummaryState,
  internal val macroCards: ImmutableList<NutritionMacroCardState>,
  internal val timeline: NutritionTimelineState,
)

@Immutable
@Poko internal class NutritionSummaryState(
  internal val dayTag: String,
  internal val displayDate: String,
  internal val headline: String,
  internal val totalCaloriesValue: String,
  internal val progressPercent: Int,
  internal val progressMeta: String,
  internal val trendDelta: String,
  internal val trendValues: ImmutableList<NutritionTrendValueState>,
)

@Immutable
@Poko internal class NutritionTrendValueState(
  internal val label: String,
  internal val value: String,
)

@Immutable
@Poko internal class NutritionMacroCardState(
  internal val title: String,
  internal val value: String,
  internal val meta: String,
  internal val highlighted: Boolean,
)

@Immutable
@Poko internal class NutritionTimelineState(
  internal val meta: String,
  internal val items: ImmutableList<NutritionTimelineItemState>,
)

@Immutable
@Poko internal class NutritionTimelineItemState(
  internal val title: String,
  internal val subtitle: String,
  internal val calorieText: String,
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
      }.toImmutableList(),
    ),
    macroCards = persistentListOf(
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
      items = persistentListOf(),
    ),
  )
