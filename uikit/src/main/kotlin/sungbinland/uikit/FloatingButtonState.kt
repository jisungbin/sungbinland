package sungbinland.uikit

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.vector.ImageVector

@Stable public class FloatingButtonState(
  public val icon: ImageVector,
  public val onClick: () -> Unit,
  public val onLongClick: (() -> Unit)? = null,
  public val progress: State<Float>? = null,
)
