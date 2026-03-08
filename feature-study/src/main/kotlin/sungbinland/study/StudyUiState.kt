package sungbinland.study

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import sungbinland.uikit.UiKitChipState

@Immutable @Poko internal class StudyDashboardState(
  val header: StudyHeaderState,
  val sections: List<StudySectionState>,
)

@Immutable @Poko internal class StudyHeaderState(
  val searchQuery: String,
  val chips: List<UiKitChipState>,
)

@Immutable @Poko internal class StudySectionState(
  val title: String,
  val entries: List<StudyCardState>,
)

@Immutable @Poko internal class StudyCardState(
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
