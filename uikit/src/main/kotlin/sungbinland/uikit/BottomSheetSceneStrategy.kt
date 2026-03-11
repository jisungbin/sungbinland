package sungbinland.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.delay

public class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {
  override fun SceneStrategyScope<T>.calculateScene(
    entries: List<NavEntry<T>>,
  ): Scene<T>? {
    val lastEntry = entries.lastOrNull() ?: return null
    lastEntry.metadata[BottomSheetKey] ?: return null
    return BottomSheetOverlayScene(
      key = lastEntry.contentKey,
      entry = lastEntry,
      previousEntries = entries.dropLast(1),
      overlaidEntries = entries.dropLast(1),
      onBack = onBack,
    )
  }

  public companion object {
    public object BottomSheetKey : NavMetadataKey<Unit>

    public fun bottomSheet(): Map<String, Any> = metadata { put(BottomSheetKey, Unit) }
  }
}

internal class BottomSheetOverlayScene<T : Any>(
  override val key: Any,
  private val entry: NavEntry<T>,
  override val previousEntries: List<NavEntry<T>>,
  override val overlaidEntries: List<NavEntry<T>>,
  private val onBack: () -> Unit,
) : OverlayScene<T> {
  override val entries: List<NavEntry<T>> = listOf(entry)

  private lateinit var visibleState: MutableTransitionState<Boolean>

  override val content: @Composable () -> Unit = {
    visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
      visibleState = visibleState,
      modifier = Modifier.fillMaxSize(),
      enter = fadeIn(animationSpec = tween(durationMillis = 200)),
      exit = fadeOut(animationSpec = tween(durationMillis = 200)),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onBack,
            ),
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .imePadding()
            .animateEnterExit(
              enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 300),
              ),
              exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 300),
              ),
            ),
        ) {
          entry.Content()
        }
      }
    }
  }

  override suspend fun onRemove() {
    visibleState.targetState = false
    delay(300L)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BottomSheetOverlayScene<*>) return false
    return key == other.key &&
      entry == other.entry &&
      previousEntries == other.previousEntries &&
      overlaidEntries == other.overlaidEntries
  }

  override fun hashCode(): Int {
    var result = key.hashCode()
    result = 31 * result + entry.hashCode()
    result = 31 * result + previousEntries.hashCode()
    result = 31 * result + overlaidEntries.hashCode()
    return result
  }
}
