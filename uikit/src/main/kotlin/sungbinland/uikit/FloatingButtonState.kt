package sungbinland.uikit

import androidx.compose.ui.graphics.vector.ImageVector

public data class FloatingButtonState(
  val icon: ImageVector,
  val onClick: () -> Unit,
)
