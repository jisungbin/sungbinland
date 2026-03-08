package sungbinland.study

import sungbinland.uikit.UiKitChipState

internal data class StudyDashboardState(
  val header: StudyHeaderState,
  val sections: List<StudySectionState>,
)

internal data class StudyHeaderState(
  val searchQuery: String,
  val chips: List<UiKitChipState>,
)

internal data class StudySectionState(
  val title: String,
  val entries: List<StudyCardState>,
)

internal data class StudyCardState(
  val name: String,
  val contentPreview: String,
  val thumbnailLabel: String?,
)

internal fun studyLoadingState(): StudyDashboardState =
  StudyDashboardState(
    header = StudyHeaderState(
      searchQuery = "",
      chips = listOf(
        UiKitChipState(id = "전체", label = "전체", selected = true),
      ),
    ),
    sections = emptyList(),
  )
