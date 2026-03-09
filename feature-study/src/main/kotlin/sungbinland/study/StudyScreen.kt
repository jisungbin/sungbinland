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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sungbinland.uikit.UiKitColors

@Composable internal fun StudyScreen(
  viewModel: StudyViewModel,
  onEntryClick: (category: String, name: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var searchQuery by rememberSaveable { mutableStateOf("") }

  StudyScreen(
    state = state,
    searchQuery = searchQuery,
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .verticalScroll(rememberScrollState())
      .systemBarsPadding()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
    onCategoryClick = viewModel::selectCategory,
    onSearchQueryChange = { query ->
      searchQuery = query
      viewModel.updateSearchQuery(query)
    },
    onEntryClick = onEntryClick,
  )
}

@Composable private fun StudyScreen(
  state: StudyDashboardState,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onCategoryClick: (String) -> Unit,
  onEntryClick: (category: String, name: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    StudyHeaderCard(
      state = state.header,
      searchQuery = searchQuery,
      modifier = Modifier.fillMaxWidth(),
      onSearchQueryChange = onSearchQueryChange,
      onCategoryClick = onCategoryClick,
    )
    StudySectionList(
      sections = state.sections,
      modifier = Modifier.fillMaxWidth(),
      onEntryClick = onEntryClick,
    )
  }
}
