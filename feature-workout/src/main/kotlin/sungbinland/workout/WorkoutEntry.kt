package sungbinland.workout

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

public fun EntryProviderScope<NavKey>.workoutEntry() {
  entry<WorkoutRoute>(metadata = mapOf("label" to "운동")) {
    WorkoutScreen()
  }
}
