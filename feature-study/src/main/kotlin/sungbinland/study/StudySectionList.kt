package sungbinland.study

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitSectionHeader
import sungbinland.uikit.UiKitTypography

@Composable internal fun StudySectionList(
  sections: ImmutableList<StudySectionState>,
  modifier: Modifier = Modifier,
  onEntryClick: (category: String, name: String) -> Unit,
  onEntryLongClick: (category: String, name: String) -> Unit,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    sections.fastForEach { section ->
      StudySection(
        state = section,
        modifier = Modifier,
        onEntryClick = onEntryClick,
        onEntryLongClick = onEntryLongClick,
      )
    }
  }
}

@Composable private fun StudySection(
  state: StudySectionState,
  modifier: Modifier = Modifier,
  onEntryClick: (category: String, name: String) -> Unit,
  onEntryLongClick: (category: String, name: String) -> Unit,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    UiKitSectionHeader(
      title = state.title,
      modifier = Modifier.fillMaxWidth(),
      meta = null,
    )
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      state.entries.fastForEach { entry ->
        key(entry.category, entry.name) {
          StudyCard(
            state = entry,
            modifier = Modifier,
            onClick = { onEntryClick(entry.category, entry.name) },
            onLongClick = { onEntryLongClick(entry.category, entry.name) },
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable private fun StudyCard(
  state: StudyCardState,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(onClick = onClick, onLongClick = onLongClick)
      .background(
        color = UiKitColors.Surface,
        shape = RoundedCornerShape(16.dp),
      )
      .border(
        width = 1.dp,
        color = UiKitColors.Border,
        shape = RoundedCornerShape(16.dp),
      )
      .padding(horizontal = 14.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      BasicText(
        text = state.name,
        style = UiKitTypography.Title.copy(
          color = UiKitColors.Text,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicText(
        text = state.contentPreview,
        style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
      )
    }
  }
}
