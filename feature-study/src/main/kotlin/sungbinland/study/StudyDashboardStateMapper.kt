package sungbinland.study

import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapTo
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.study.entity.StudyEntryEntity
import sungbinland.uikit.UiKitChipState

internal class StudyDashboardStateMapper(private val studyEntryDao: StudyEntryDao) {
  internal suspend fun createState(
    selectedCategory: String,
    searchQuery: String,
  ): StudyDashboardState {
    val entries = studyEntryDao.getAllStudyEntries()
    val categories = entries
      .fastMap { entry -> entry.category }
      .distinct()
      .sortedWith(String.CASE_INSENSITIVE_ORDER)
    val chips = buildList {
      add(UiKitChipState(id = ALL_CATEGORY, label = ALL_CATEGORY, selected = selectedCategory == ALL_CATEGORY))
      categories.fastForEach { category ->
        add(UiKitChipState(id = category, label = category, selected = selectedCategory == category))
      }
    }.toImmutableList()
    val filteredEntries = entries.filterBy(
      selectedCategory = selectedCategory,
      searchQuery = searchQuery,
    )
    val groupedEntries = when {
      selectedCategory == ALL_CATEGORY -> filteredEntries.groupBy { entry -> entry.category }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
      else -> sortedMapOf(selectedCategory to filteredEntries)
    }

    return StudyDashboardState(
      header = StudyHeaderState(
        searchQuery = searchQuery,
        chips = chips,
      ),
      sections = groupedEntries.map { (category, sectionEntries) ->
        StudySectionState(
          title = category,
          entries = sectionEntries
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { entry -> entry.name })
            .fastMapTo(persistentListOf<StudyCardState>().builder()) { entry ->
              StudyCardState(
                category = entry.category,
                name = entry.name,
                contentPreview = entry.content,
              )
            }.build(),
        )
      }.toImmutableList(),
    )
  }

  private fun List<StudyEntryEntity>.filterBy(
    selectedCategory: String,
    searchQuery: String,
  ): List<StudyEntryEntity> {
    val normalizedQuery = searchQuery.trim()
    return fastFilter { entry ->
      val categoryMatches = selectedCategory == ALL_CATEGORY || entry.category == selectedCategory
      val queryMatches = when {
        normalizedQuery.isBlank() -> true
        else -> {
          entry.category.contains(normalizedQuery, ignoreCase = true) ||
            entry.name.contains(normalizedQuery, ignoreCase = true) ||
            entry.content.contains(normalizedQuery, ignoreCase = true)
        }
      }
      categoryMatches && queryMatches
    }
  }

  private companion object {
    private const val ALL_CATEGORY: String = "전체"
  }
}
