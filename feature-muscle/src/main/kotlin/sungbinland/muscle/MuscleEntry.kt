package sungbinland.muscle

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

public fun EntryProviderScope<NavKey>.muscleEntry(
  onOverlayChange: (Boolean) -> Unit,
) {
  entry<MuscleRoute>(
    metadata = mapOf("label" to "근육"),
  ) {
    MuscleScreen(onOverlayChange = onOverlayChange)
  }
}
