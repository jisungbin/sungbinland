package sungbinland.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import sungbinland.uikit.UiKitChecklistRow
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitPillButton
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutSupplementChecklistSection(
  state: WorkoutSupplementChecklistState,
  modifier: Modifier = Modifier,
  onItemClick: (String) -> Unit,
  onManageSupplementClick: () -> Unit,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = "보충제 복용 체크리스트",
        modifier = Modifier.align(Alignment.CenterVertically),
        style = UiKitTypography.Title.copy(color = UiKitColors.Text),
      )
      UiKitPillButton(
        text = "보충제 관리",
        modifier = Modifier.align(Alignment.CenterVertically),
        onClick = onManageSupplementClick,
      )
    }
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      state.items.fastForEach { item ->
        UiKitChecklistRow(
          title = item.name,
          subtitle = item.meta,
          checked = item.checked,
          modifier = Modifier.fillMaxWidth(),
          onClick = { onItemClick(item.name) },
        )
      }
    }
  }
}
