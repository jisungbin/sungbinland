package sungbinland.uikit

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.drewhamilton.poko.Poko

@Stable @Poko public class FloatingButtonState(
  public val icon: ImageVector,
  public val onClick: () -> Unit,
)
