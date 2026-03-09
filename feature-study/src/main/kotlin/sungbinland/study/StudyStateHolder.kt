package sungbinland.study

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class StudyStateHolder(private val mapper: StudyDashboardStateMapper) {
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val selectedCategoryState: MutableStateFlow<String> = MutableStateFlow(ALL_CATEGORY)
  private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
  private val refreshState: MutableStateFlow<Long> = MutableStateFlow(0L)

  internal val state: StateFlow<StudyDashboardState> = scope.launchMolecule(
    mode = RecompositionMode.Immediate,
  ) {
    val selectedCategory by selectedCategoryState.collectAsState()
    val searchQuery by searchQueryState.collectAsState()
    val refreshKey by refreshState.collectAsState()
    val dashboardState by produceState(
      initialValue = studyLoadingState(),
      selectedCategory,
      searchQuery,
      refreshKey,
    ) {
      value = mapper.createState(
        selectedCategory = selectedCategory,
        searchQuery = searchQuery,
      )
    }
    dashboardState
  }

  internal fun updateSearchQuery(query: String) {
    searchQueryState.update { query }
  }

  internal fun selectCategory(category: String) {
    selectedCategoryState.update { category }
  }

  internal fun refresh() {
    refreshState.update { value -> value + 1L }
  }

  internal fun close() {
    scope.cancel()
  }

  private companion object {
    private const val ALL_CATEGORY: String = "전체"
  }
}
