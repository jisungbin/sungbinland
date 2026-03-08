package sungbinland.study

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

public fun EntryProviderScope<NavKey>.studyEntry() {
  entry<StudyRoute>(metadata = mapOf("label" to "스터디")) {
    StudyScreen()
  }
}
