package sungbinland.uikit

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
public class FabController {
  private val states = mutableStateMapOf<Any, FloatingButtonState>()

  public fun set(key: Any, state: FloatingButtonState) {
    states[key] = state
  }

  public fun get(key: Any): FloatingButtonState? = states[key]
}

public val LocalFabController: ProvidableCompositionLocal<FabController> =
  staticCompositionLocalOf { error("No FabController provided") }
