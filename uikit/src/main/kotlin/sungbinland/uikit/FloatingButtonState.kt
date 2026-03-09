package sungbinland.uikit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.drewhamilton.poko.Poko

@Immutable
@Poko public class FloatingButtonState(
  public val icon: ImageVector,
  public val onClick: () -> Unit,
)
