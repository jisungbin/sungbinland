package sungbinland.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import sungbinland.uikit.UiKitColors

@Composable internal fun StudyScreen(
  stateHolder: StudyStateHolder,
  showRegistrationSheet: Boolean,
  onDismissRegistrationSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by stateHolder.state.collectAsState()

  var categories: ImmutableList<String> by remember { mutableStateOf(persistentListOf()) }
  LaunchedEffect(showRegistrationSheet) {
    if (showRegistrationSheet) {
      categories = stateHolder.getCategories().toImmutableList()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    StudyScreen(
      state = state,
      modifier = Modifier
        .fillMaxSize()
        .background(UiKitColors.Background)
        .systemBarsPadding()
        .padding(all = 16.dp),
      onCategoryClick = stateHolder::selectCategory,
      onSearchQueryChange = stateHolder::updateSearchQuery,
    )
    StudyEntryRegistrationSheet(
      visible = showRegistrationSheet,
      categories = categories,
      onDismiss = onDismissRegistrationSheet,
      onSubmit = { category, name, content, imageUrl ->
        stateHolder.registerEntry(
          category = category,
          name = name,
          content = content,
          imageUrl = imageUrl,
        )
        onDismissRegistrationSheet()
      },
    )
  }
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
      .padding(bottom = 92.dp),
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
