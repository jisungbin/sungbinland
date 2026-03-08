package sungbinland.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sungbinland.uikit.UiKitColors

@Composable internal fun StudyScreen(
  stateHolder: StudyStateHolder,
  modifier: Modifier = Modifier,
) {
  val state by stateHolder.state.collectAsState()
  StudyScreen(
    state = state,
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .systemBarsPadding()
      .padding(all = 16.dp),
    onCategoryClick = stateHolder::selectCategory,
    onSearchQueryChange = stateHolder::updateSearchQuery,
  )
}

@Composable private fun StudyScreen(
  state: StudyDashboardState,
  onSearchQueryChange: (String) -> Unit,
  onCategoryClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    StudyHeaderCard(
      state = state.header,
      modifier = Modifier.fillMaxWidth(),
      onSearchQueryChange = onSearchQueryChange,
      onCategoryClick = onCategoryClick,
    )
    StudySectionList(
      sections = state.sections,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
