package sungbinland.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import sungbinland.uikit.UiKitListCardRow
import sungbinland.uikit.UiKitSectionHeader

@Composable internal fun NutritionFoodTimelineSection(
  state: NutritionTimelineState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    UiKitSectionHeader(
      title = "먹은 음식",
      meta = state.meta,
      modifier = Modifier.fillMaxWidth(),
    )
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      state.items.fastForEach { item ->
        UiKitListCardRow(
          title = item.title,
          subtitle = item.subtitle,
          trailing = item.calorieText,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}
