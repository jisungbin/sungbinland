package sungbinland.study

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.uikit.FloatingButtonState
import sungbinland.uikit.LocalFabController

public fun EntryProviderScope<NavKey>.studyEntry(
  studyEntryDao: StudyEntryDao,
) {
  entry<StudyRoute>(
    metadata = mapOf("label" to "스터디"),
  ) {
    val fabController = LocalFabController.current
    var showRegistrationSheet by rememberSaveable { mutableStateOf(false) }
    val fabState = remember {
      FloatingButtonState(icon = Icons.AutoMirrored.Rounded.MenuBook, onClick = { showRegistrationSheet = true })
    }
    SideEffect { fabController.set(StudyRoute, fabState) }
    val mapper = retain(studyEntryDao) {
      StudyDashboardStateMapper(
        studyEntryDao = studyEntryDao,
      )
    }
    val stateHolder = retain(mapper, studyEntryDao) {
      StudyStateHolder(mapper = mapper, studyEntryDao = studyEntryDao)
    }
    RetainedEffect(stateHolder) { onRetire(stateHolder::close) }
    StudyScreen(
      stateHolder = stateHolder,
      showRegistrationSheet = showRegistrationSheet,
      onDismissRegistrationSheet = { showRegistrationSheet = false },
    )
  }
}
