package sungbinland.study

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import sungbinland.uikit.UiKitChipState

@Immutable
@Poko internal class StudyDashboardState(
  internal val header: StudyHeaderState,
  internal val sections: ImmutableList<StudySectionState>,
)

@Immutable
@Poko internal class StudyHeaderState(
  internal val searchQuery: String,
  internal val chips: ImmutableList<UiKitChipState>,
)

@Immutable
@Poko internal class StudySectionState(
  internal val title: String,
  internal val entries: ImmutableList<StudyCardState>,
)

@Immutable
@Poko internal class StudyCardState(
  internal val name: String,
  internal val contentPreview: String,
  internal val thumbnailLabel: String?,
)

internal fun studyLoadingState(): StudyDashboardState =
  StudyDashboardState(
    header = StudyHeaderState(
      searchQuery = "",
      chips = persistentListOf(
        UiKitChipState(id = "전체", label = "전체", selected = true),
      ),
    ),
    sections = persistentListOf(),
  )
