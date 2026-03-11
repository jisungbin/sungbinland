package sungbinland.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
  restTimer: WorkoutRestTimer,
  modifier: Modifier = Modifier,
) {
  var elapsedMillis by remember { mutableLongStateOf(0L) }

  LaunchedEffect(restTimer.startNanos) {
    if (restTimer.isRunning) {
      while (restTimer.isRunning) {
        withFrameNanos { now ->
          val start = restTimer.startNanos
          if (start != 0L) {
            elapsedMillis = ((now - start) / 1_000_000).coerceAtMost(80_000)
          }
        }
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
      .padding(top = 24.dp, bottom = 36.dp, start = 24.dp, end = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(28.dp),
  ) {
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
