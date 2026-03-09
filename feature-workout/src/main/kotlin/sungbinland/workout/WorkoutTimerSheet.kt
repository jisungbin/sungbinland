package sungbinland.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutTimerSheet(
  visible: Boolean,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit,
  onStart: () -> Unit,
) {
  AnimatedVisibility(
    visible = visible,
    modifier = modifier.fillMaxSize(),
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
            onClick = onDismiss,
          ),
      )
      TimerSheetContent(
        modifier = Modifier
          .align(Alignment.BottomCenter)
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
        onStart = onStart,
      )
    }
  }
}

@Composable private fun TimerSheetContent(
  modifier: Modifier = Modifier,
  onStart: () -> Unit,
) {
  var elapsedMillis by remember { mutableLongStateOf(0L) }

  LaunchedEffect(Unit) {
    onStart()
    val startNanos = withFrameNanos { it }
    while (true) {
      withFrameNanos { frameNanos ->
        elapsedMillis = (frameNanos - startNanos) / 1_000_000
      }
    }
  }

  val totalSeconds = elapsedMillis / 1000
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  val timerText = "%02d:%02d".format(minutes, seconds)
  val progress = (elapsedMillis.coerceAtMost(80_000) / 80_000f)

  val trackColor = Color(0xFFE8E8E8)
  val progressColor = UiKitColors.BrandBlue

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      )
      .padding(top = 12.dp, bottom = 36.dp, start = 24.dp, end = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(28.dp),
  ) {
    Box(
      modifier = Modifier
        .size(width = 40.dp, height = 4.dp)
        .background(color = Color(0xFFD9D9D9), shape = RoundedCornerShape(2.dp)),
    )
    BasicText(
      text = "휴식 타이머",
      modifier = Modifier.fillMaxWidth(),
      style = UiKitTypography.TitleLarge.copy(color = UiKitColors.Text),
    )
    Box(
      modifier = Modifier.size(200.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
        val strokeWidth = 4.dp.toPx()
        drawCircle(
          color = trackColor,
          style = Stroke(width = strokeWidth),
        )
        drawArc(
          color = progressColor,
          startAngle = -90f,
          sweepAngle = 360f * progress,
          useCenter = false,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
      }
      BasicText(
        text = timerText,
        style = TextStyle(
          color = UiKitColors.Text,
          fontSize = 48.sp,
          fontWeight = FontWeight.Bold,
        ),
      )
    }
  }
}
