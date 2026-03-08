package sungbinland.study

import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.study.entity.StudyEntryEntity
import sungbinland.uikit.UiKitChipState

internal class StudyDashboardStateMapper(
  private val studyEntryDao: StudyEntryDao,
) {
  internal suspend fun createState(
    selectedCategory: String,
    searchQuery: String,
  ): StudyDashboardState {
    val entries = studyEntryDao.getAllStudyEntries()
    val categories = entries
      .map { entry -> entry.category }
      .distinct()
      .sorted()
    val chips = buildList {
      add(UiKitChipState(id = ALL_CATEGORY, label = ALL_CATEGORY, selected = selectedCategory == ALL_CATEGORY))
      categories.forEach { category ->
        add(UiKitChipState(id = category, label = category, selected = selectedCategory == category))
      }
    }
    val filteredEntries = entries.filterBy(
      selectedCategory = selectedCategory,
      searchQuery = searchQuery,
    )
    val groupedEntries = if (selectedCategory == ALL_CATEGORY) {
      filteredEntries.groupBy { entry -> entry.category }.toSortedMap()
    } else {
      sortedMapOf(selectedCategory to filteredEntries)
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
            .sortedBy { entry -> entry.name }
            .map { entry ->
              StudyCardState(
                name = entry.name,
                contentPreview = entry.content.previewText(),
                thumbnailLabel = if (entry.imageUrl.isNullOrBlank()) null else "썸네일",
              )
            },
        )
      },
    )
  }

  private fun List<StudyEntryEntity>.filterBy(
    selectedCategory: String,
    searchQuery: String,
  ): List<StudyEntryEntity> {
    val normalizedQuery = searchQuery.trim()
    return filter { entry ->
      val categoryMatches = selectedCategory == ALL_CATEGORY || entry.category == selectedCategory
      val queryMatches = if (normalizedQuery.isBlank()) {
        true
      } else {
        entry.category.contains(normalizedQuery, ignoreCase = true) ||
          entry.name.contains(normalizedQuery, ignoreCase = true) ||
          entry.content.contains(normalizedQuery, ignoreCase = true)
      }
      categoryMatches && queryMatches
    }
  }

  private fun String.previewText(): String =
    if (length <= 44) this else "${take(44)}..."

  private companion object {
    private const val ALL_CATEGORY: String = "전체"
  }
}
