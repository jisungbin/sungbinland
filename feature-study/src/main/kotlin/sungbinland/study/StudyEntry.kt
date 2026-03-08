package sungbinland.study

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.uikit.FloatingButtonState

public fun EntryProviderScope<NavKey>.studyEntry(
  studyEntryDao: StudyEntryDao,
) {
  entry<StudyRoute>(
    metadata = mapOf(
      "label" to "스터디",
      "floatingButtonState" to FloatingButtonState(
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        onClick = {},
      ),
    ),
  ) {
    val mapper = retain(studyEntryDao) {
      StudyDashboardStateMapper(
        studyEntryDao = studyEntryDao,
      )
    }
    val stateHolder = retain(mapper) {
      StudyStateHolder(mapper = mapper)
    }
    RetainedEffect(stateHolder) {
      onRetire {
        stateHolder.close()
      }
    }
    StudyScreen(stateHolder = stateHolder)
  }
}
