package sungbinland.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.collections.immutable.ImmutableList

@Composable public fun UiKitDateNavigator(
  dayTag: String,
  displayDate: String,
  modifier: Modifier = Modifier,
  onPreviousClick: () -> Unit,
  onNextClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    UiKitPillButton(
      text = "<",
      modifier = Modifier,
      onClick = onPreviousClick,
    )
    Column(
      modifier = Modifier.clickable(onClick = onCurrentDateClick),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
      BasicText(
        text = dayTag,
        style = UiKitTypography.Label.copy(
          color = UiKitColors.Accent,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicText(
        text = displayDate,
        style = UiKitTypography.Title.copy(color = UiKitColors.Primary),
      )
    }
    UiKitPillButton(
      text = ">",
      modifier = Modifier,
      onClick = onNextClick,
    )
  }
}

@Composable public fun UiKitPillButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  BasicText(
    text = text,
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFFCFBF9))
      .border(
        width = 1.dp,
        color = UiKitColors.BorderSoft,
        shape = RoundedCornerShape(12.dp),
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    style = UiKitTypography.Value.copy(color = UiKitColors.MutedTextStrong),
  )
}

@Composable public fun UiKitProgressBar(
  progressPercent: Int,
  modifier: Modifier = Modifier,
  fillColor: Color = UiKitColors.Accent,
) {
  val normalized: Float = progressPercent.coerceIn(0, 100) / 100f
  BoxWithConstraints(
    modifier = modifier
      .height(8.dp)
      .clip(CircleShape)
      .background(UiKitColors.ProgressTrack),
  ) {
    Box(
      modifier = Modifier
        .width(maxWidth * normalized)
        .height(8.dp)
        .clip(CircleShape)
        .background(fillColor),
    )
  }
}

@Composable public fun UiKitDeltaBadge(
  delta: String,
  meta: String? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(UiKitColors.PositiveSurface)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = delta,
      style = UiKitTypography.Label.copy(
        color = UiKitColors.PositiveText,
        fontWeight = FontWeight.SemiBold,
      ),
    )
    if (!meta.isNullOrBlank()) {
      BasicText(
        text = meta,
        style = UiKitTypography.Label.copy(color = UiKitColors.PositiveText),
      )
    }
  }
}

@Composable public fun UiKitMiniTrendBars(
  values: ImmutableList<Float>,
  modifier: Modifier = Modifier,
  activeStartIndex: Int = 3,
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFFAFAF8))
      .padding(12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Bottom,
  ) {
    values.fastForEachIndexed { index, value ->
      val normalized: Float = value.coerceIn(0.2f, 1f)
      val barColor: Color = when {
        index < activeStartIndex -> Color(0xFFF3E4E1)
        index == activeStartIndex -> UiKitColors.Primary
        else -> UiKitColors.Accent
      }
      Box(
        modifier = Modifier
          .weight(1f)
          .height((44.dp * normalized).coerceAtLeast(8.dp))
          .clip(RoundedCornerShape(10.dp))
          .background(barColor),
      )
    }
  }
}

@Composable public fun UiKitFloatingActionButton(
  state: FloatingButtonState,
  hazeState: HazeState,
  modifier: Modifier = Modifier,
  containerColor: Color = UiKitColors.BrandBlue.copy(alpha = 0.5f),
) {
  UiKitFloatingActionButton(
    icon = state.icon,
    hazeState = hazeState,
    modifier = modifier,
    containerColor = containerColor,
    onClick = state.onClick,
  )
}

@Composable public fun UiKitFloatingActionButton(
  symbol: String,
  hazeState: HazeState,
  modifier: Modifier = Modifier,
  containerColor: Color = UiKitColors.BrandBlue,
  onClick: () -> Unit,
) {
  UiKitFloatingActionButtonContainer(
    hazeState = hazeState,
    modifier = modifier,
    containerColor = containerColor,
    onClick = onClick,
  ) {
    BasicText(
      text = symbol,
      style = UiKitTypography.TitleLarge.copy(color = Color.White),
    )
  }
}

@Composable public fun UiKitFloatingActionButton(
  icon: ImageVector,
  hazeState: HazeState,
  modifier: Modifier = Modifier,
  containerColor: Color = UiKitColors.BrandBlue.copy(alpha = 0.5f),
  onClick: () -> Unit,
) {
  UiKitFloatingActionButtonContainer(
    hazeState = hazeState,
    modifier = modifier,
    containerColor = containerColor,
    onClick = onClick,
  ) {
    Image(
      modifier = Modifier.size(24.dp),
      imageVector = icon,
      contentDescription = null,
      colorFilter = ColorFilter.tint(Color.White),
    )
  }
}

@Composable private fun UiKitFloatingActionButtonContainer(
  hazeState: HazeState,
  containerColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier
      .size(56.dp)
      .clip(CircleShape)
      .hazeEffect(
        state = hazeState,
        style = HazeStyle(
          backgroundColor = Color.Transparent,
          tint = HazeTint(Color.White.copy(alpha = 0.10f)),
          blurRadius = 30.dp,
        ),
      )
      .background(color = containerColor)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}
