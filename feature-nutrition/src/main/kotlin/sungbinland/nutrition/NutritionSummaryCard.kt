package sungbinland.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitDateNavigator
import sungbinland.uikit.UiKitDeltaBadge
import sungbinland.uikit.UiKitPillButton
import sungbinland.uikit.UiKitProgressBar
import sungbinland.uikit.UiKitSurfaceCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun NutritionSummaryCard(
  state: NutritionSummaryState,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onOpenGraphClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  UiKitSurfaceCard(
    modifier = modifier.fillMaxWidth(),
    borderColor = UiKitColors.BorderStrong,
    verticalSpacing = 14.dp,
  ) {
    UiKitDateNavigator(
      dayTag = state.dayTag,
      displayDate = state.displayDate,
      modifier = Modifier.fillMaxWidth(),
      onPreviousClick = onPreviousDateClick,
      onNextClick = onNextDateClick,
      onCurrentDateClick = onCurrentDateClick,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = state.headline,
        style = UiKitTypography.Headline.copy(color = UiKitColors.Primary),
      )
      UiKitPillButton(
        text = "전체 그래프",
        modifier = Modifier,
        onClick = onOpenGraphClick,
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        BasicText(
          text = "총 칼로리",
          style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          BasicText(
            text = state.totalCaloriesValue,
            style = UiKitTypography.DisplayMetric.copy(color = UiKitColors.Primary),
          )
          BasicText(
            text = "kcal",
            style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
          )
        }
      }
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        BasicText(
          text = "목표 대비 진행률",
          style = UiKitTypography.Label.copy(color = UiKitColors.MutedText),
        )
        BasicText(
          text = "${state.progressPercent}%",
          style = UiKitTypography.TitleLarge.copy(color = UiKitColors.Primary),
        )
        BasicText(
          text = state.progressMeta,
          style = UiKitTypography.Micro.copy(color = UiKitColors.MutedText),
        )
      }
    }
    UiKitProgressBar(
      progressPercent = state.progressPercent,
      modifier = Modifier.fillMaxWidth(),
      fillColor = UiKitColors.Accent,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = "최근 7일 트렌드",
        style = UiKitTypography.Title.copy(color = UiKitColors.MutedText),
      )
      UiKitDeltaBadge(
        delta = state.trendDelta,
        meta = "지난주 대비",
        modifier = Modifier.align(Alignment.CenterVertically),
      )
    }
    NutritionTrendValueBoxes(
      trendValues = state.trendValues,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 2.dp),
    )
  }
}

@Composable private fun NutritionTrendValueBoxes(
  trendValues: List<NutritionTrendValueState>,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  LaunchedEffect(scrollState.maxValue) {
    scrollState.scrollTo(scrollState.maxValue)
  }
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFFAFAF8))
      .horizontalScroll(state = scrollState)
      .padding(10.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val previousDayIndex: Int = trendValues.lastIndex - 1
    val selectedDayIndex: Int = trendValues.lastIndex
    trendValues.fastForEachIndexed { index, trendValue ->
      val containerColor: Color = when (index) {
        selectedDayIndex -> UiKitColors.Accent
        previousDayIndex -> UiKitColors.Primary
        else -> UiKitColors.Background
      }
      val textColor = if (index >= previousDayIndex) {
        UiKitColors.Surface
      } else {
        UiKitColors.Primary
      }
      val labelColor = if (index >= previousDayIndex) {
        UiKitColors.Surface
      } else {
        UiKitColors.MutedText
      }
      val showBorder: Boolean = index != selectedDayIndex && index != previousDayIndex
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(containerColor)
          .then(
            if (showBorder) {
              Modifier.border(
                width = 1.dp,
                color = UiKitColors.BorderSoft,
                shape = RoundedCornerShape(10.dp),
              )
            } else {
              Modifier
            },
          )
          .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        BasicText(
          text = trendValue.label,
          style = UiKitTypography.Micro.copy(color = labelColor),
        )
        BasicText(
          text = trendValue.value,
          style = UiKitTypography.Value.copy(color = textColor),
        )
      }
    }
  }
}
