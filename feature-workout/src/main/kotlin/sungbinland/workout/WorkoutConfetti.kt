package sungbinland.workout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ConfettiShape { Circle, Square, Rectangle, Triangle, Diamond, Star, Line }

private class ConfettiParticle(
  val x: Float,
  val velocityX: Float,
  val velocityY: Float,
  val rotation: Float,
  val rotationSpeed: Float,
  val color: Color,
  val shape: ConfettiShape,
  val size: Float,
)

private val confettiColors = listOf(
  Color(0xFFFF6B6B),
  Color(0xFF4ECDC4),
  Color(0xFFFFE66D),
  Color(0xFF95E1D3),
  Color(0xFFF38181),
  Color(0xFFAA96DA),
  Color(0xFF6C5CE7),
  Color(0xFFFD79A8),
  Color(0xFF00B894),
  Color(0xFFE17055),
  Color(0xFF0984E3),
  Color(0xFFFDAC53),
)

private fun generateParticles(width: Float, height: Float, count: Int = 400): List<ConfettiParticle> {
  val random = Random(System.nanoTime())
  val shapes = ConfettiShape.entries
  return List(count) {
    ConfettiParticle(
      x = random.nextFloat() * width,
      velocityX = (random.nextFloat() - 0.5f) * 5f,
      velocityY = random.nextFloat() * 1.8f + 0.5f,
      rotation = random.nextFloat() * 360f,
      rotationSpeed = (random.nextFloat() - 0.5f) * 12f,
      color = confettiColors[random.nextInt(confettiColors.size)],
      shape = shapes[random.nextInt(shapes.size)],
      size = random.nextFloat() * 10f + 5f,
    )
  }
}

@Composable internal fun WorkoutConfetti(trigger: Int, modifier: Modifier = Modifier) {
  if (trigger <= 0) return

  val progress = remember(trigger) { Animatable(0f) }
  val particles = remember(trigger) { mutableListOf<ConfettiParticle>() }

  LaunchedEffect(trigger) {
    particles.clear()
    progress.snapTo(0f)
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 5000, easing = LinearEasing),
    )
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    if (particles.isEmpty()) {
      particles.addAll(generateParticles(width = size.width, height = size.height))
    }
    val t = progress.value
    if (t <= 0f || t >= 1f) return@Canvas
    val alpha = if (t > 0.75f) ((1f - t) / 0.25f).coerceIn(0f, 1f) else 1f
    val height = size.height

    particles.forEach { particle ->
      val elapsed = t * 5f
      val px = particle.x + particle.velocityX * elapsed * 80f
      val py = -80f + particle.velocityY * elapsed * 140f + 50f * elapsed * elapsed
      if (py > height + 50f) return@forEach
      val rotation = particle.rotation + particle.rotationSpeed * elapsed * 60f
      val color = particle.color.copy(alpha = alpha)
      val s = particle.size

      rotate(degrees = rotation, pivot = Offset(px, py)) {
        when (particle.shape) {
          ConfettiShape.Circle -> drawCircle(color = color, radius = s, center = Offset(px, py))
          ConfettiShape.Square -> drawRect(
            color = color,
            topLeft = Offset(px - s, py - s),
            size = Size(s * 2, s * 2),
          )
          ConfettiShape.Rectangle -> drawRect(
            color = color,
            topLeft = Offset(px - s, py - s / 2),
            size = Size(s * 2, s),
          )
          ConfettiShape.Triangle -> drawPath(
            path = Path().apply {
              moveTo(px, py - s)
              lineTo(px - s, py + s)
              lineTo(px + s, py + s)
              close()
            },
            color = color,
          )
          ConfettiShape.Diamond -> drawPath(
            path = Path().apply {
              moveTo(px, py - s)
              lineTo(px + s, py)
              lineTo(px, py + s)
              lineTo(px - s, py)
              close()
            },
            color = color,
          )
          ConfettiShape.Star -> drawPath(path = starPath(px, py, s), color = color)
          ConfettiShape.Line -> drawLine(
            color = color,
            start = Offset(px - s, py),
            end = Offset(px + s, py),
            strokeWidth = 3f,
          )
        }
      }
    }
  }
}

private fun starPath(cx: Float, cy: Float, radius: Float): Path = Path().apply {
  val innerRadius = radius * 0.4f
  for (i in 0 until 10) {
    val r = if (i % 2 == 0) radius else innerRadius
    val angle = Math.toRadians((i * 36 - 90).toDouble())
    val x = cx + r * cos(angle).toFloat()
    val y = cy + r * sin(angle).toFloat()
    if (i == 0) moveTo(x, y) else lineTo(x, y)
  }
  close()
}
