package sungbinland.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sungbinland.uikit.UiKitCategoryChipRow
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitSearchField
import sungbinland.uikit.UiKitSurfaceCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun StudyHeaderCard(
  state: StudyHeaderState,
  searchQuery: String,
  modifier: Modifier = Modifier,
  onSearchQueryChange: (String) -> Unit,
  onCategoryClick: (String) -> Unit,
) {
  UiKitSurfaceCard(
    modifier = modifier.fillMaxWidth(),
    verticalSpacing = 16.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      BasicText(
        text = "스터디",
        style = UiKitTypography.Headline.copy(color = UiKitColors.Primary),
      )
      UiKitSearchField(
        value = searchQuery,
        placeholder = "항목 검색",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onSearchQueryChange,
      )
      UiKitCategoryChipRow(
        chips = state.chips,
        modifier = Modifier.fillMaxWidth(),
        onChipClick = onCategoryClick,
      )
    }
  }
}
