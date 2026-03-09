package sungbinland.study

import androidx.compose.ui.util.fastMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.study.entity.StudyEntryEntity

@OptIn(ExperimentalCoroutinesApi::class)
internal class StudyViewModel(
  private val mapper: StudyDashboardStateMapper,
  private val studyEntryDao: StudyEntryDao,
) : ViewModel() {
  private val selectedCategoryState: MutableStateFlow<String> = MutableStateFlow(ALL_CATEGORY)
  private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
  private val refreshState: MutableStateFlow<Long> = MutableStateFlow(0L)

  internal val state: StateFlow<StudyDashboardState> =
    combine(selectedCategoryState, searchQueryState, refreshState) { category, query, _ -> category to query }
      .mapLatest { (category, query) ->
        mapper.createState(selectedCategory = category, searchQuery = query)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = studyLoadingState(),
      )

  internal fun updateSearchQuery(query: String) {
    searchQueryState.update { query }
  }

  internal fun selectCategory(category: String) {
    selectedCategoryState.update { category }
  }

  internal suspend fun getCategories(): List<String> =
    studyEntryDao.getAllStudyEntries()
      .fastMap { entry -> entry.category }
      .distinct()
      .sorted()

  internal suspend fun getEntry(category: String, name: String): StudyEntryEntity? =
    studyEntryDao.getStudyEntry(category = category, name = name)

  internal fun updateEntry(
    oldCategory: String,
    oldName: String,
    newCategory: String,
    newName: String,
    content: String,
  ) {
    viewModelScope.launch {
      if (oldCategory != newCategory || oldName != newName) {
        studyEntryDao.deleteStudyEntry(
          StudyEntryEntity(category = oldCategory, name = oldName, content = ""),
        )
      }
      studyEntryDao.upsertStudyEntry(
        StudyEntryEntity(category = newCategory, name = newName, content = content),
      )
      refresh()
    }
  }

  internal fun registerEntry(category: String, name: String, content: String) {
    viewModelScope.launch {
      studyEntryDao.upsertStudyEntry(
        StudyEntryEntity(category = category, name = name, content = content),
      )
      refresh()
    }
  }

  internal fun refresh() {
    refreshState.update { it + 1L }
  }

  private companion object {
    private const val ALL_CATEGORY: String = "전체"
  }
}
