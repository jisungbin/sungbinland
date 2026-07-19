package sungbinland.workout.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.os.SystemClock
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.Random

// SurfaceView + 전용 렌더 스레드. 그리기를 메인 스레드에서 분리해 UI 랙 없이 콘페티를 쏟는다.
internal class ConfettiView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
  @Volatile private var emitUntil = 0L
  private var thread: RenderThread? = null

  init {
    setZOrderOnTop(true)
    holder.setFormat(PixelFormat.TRANSLUCENT)
    holder.addCallback(this)
    isClickable = false
    isFocusable = false
  }

  // 메인 스레드에서 호출. 이후 렌더 스레드가 emitUntil을 읽어 처리한다.
  fun burst(durationMillis: Long) {
    emitUntil = SystemClock.uptimeMillis() + durationMillis
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    thread = RenderThread(holder).also { it.start() }
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    thread?.let {
      it.running = false
      it.join()
    }
    thread = null
  }

  private inner class RenderThread(private val surfaceHolder: SurfaceHolder) : Thread("confetti-render") {
    @Volatile var running = true

    private val particles = ArrayList<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random()
    private val density = resources.displayMetrics.density
    private val colors = intArrayOf(
      0xFFE53935.toInt(), 0xFFFF8F00.toInt(), 0xFFFFEB3B.toInt(), 0xFF43A047.toInt(),
      0xFF00ACC1.toInt(), 0xFF1E88E5.toInt(), 0xFF8E24AA.toInt(), 0xFFEC407A.toInt(),
      0xFFFF5722.toInt(), 0xFF00E5FF.toInt(),
    )

    override fun run() {
      var clearedWhenIdle = false
      while (running) {
        val now = SystemClock.uptimeMillis()
        val emitting = now < emitUntil
        val active = emitting || particles.isNotEmpty()

        if (!active) {
          // 유휴 상태: 남은 잔상 한 번 지우고 CPU 양보.
          if (!clearedWhenIdle) {
            drawFrame(clearOnly = true)
            clearedWhenIdle = true
          }
          sleepQuietly(40L)
          continue
        }
        clearedWhenIdle = false

        val w = width
        if (emitting && w > 0 && particles.size < MAX_PARTICLES) {
          repeat(14) { spawn(w) }
        }
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
          val p = iterator.next()
          p.x += p.vx
          p.y += p.vy
          p.vy += 0.35f * density
          p.angle += p.spin
          if (p.y > height + 60) iterator.remove()
        }
        drawFrame(clearOnly = false)
        sleepQuietly(16L)
      }
    }

    private fun drawFrame(clearOnly: Boolean) {
      val canvas: Canvas = surfaceHolder.lockCanvas() ?: return
      try {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (!clearOnly) {
          for (p in particles) {
            paint.color = p.color
            canvas.save()
            canvas.rotate(p.angle, p.x, p.y)
            canvas.drawRect(p.x - p.size / 2f, p.y - p.size / 4f, p.x + p.size / 2f, p.y + p.size / 4f, paint)
            canvas.restore()
          }
        }
      } finally {
        surfaceHolder.unlockCanvasAndPost(canvas)
      }
    }

    private fun spawn(w: Int) {
      particles.add(
        Particle(
          x = random.nextFloat() * w,
          y = -20f * density,
          vx = (random.nextFloat() - 0.5f) * 8f * density,
          vy = (3f + random.nextFloat() * 9f) * density,
          size = (5f + random.nextFloat() * 8f) * density,
          angle = random.nextFloat() * 360f,
          spin = (random.nextFloat() - 0.5f) * 40f,
          color = colors[random.nextInt(colors.size)],
        ),
      )
    }

    private fun sleepQuietly(millis: Long) {
      try {
        sleep(millis)
      } catch (_: InterruptedException) {
      }
    }
  }

  private class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    var vy: Float,
    val size: Float,
    var angle: Float,
    val spin: Float,
    val color: Int,
  )

  private companion object {
    private const val MAX_PARTICLES = 600
  }
}
