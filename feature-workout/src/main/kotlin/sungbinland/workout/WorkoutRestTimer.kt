package sungbinland.workout

import android.os.SystemClock
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue

@Stable
internal class WorkoutRestTimer {
  internal var startNanos: Long by mutableLongStateOf(0L)
    private set

  internal val isRunning: Boolean get() = startNanos != 0L

  internal fun start() {
    startNanos = SystemClock.elapsedRealtimeNanos()
  }

  private var stoppedElapsedMillis: Long = 0L

  internal fun stop() {
    if (startNanos != 0L) {
      stoppedElapsedMillis = ((SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000).coerceAtLeast(0)
    }
    startNanos = 0L
  }

  internal fun advance(millis: Long) {
    if (startNanos != 0L) {
      startNanos -= millis * 1_000_000
    }
  }

  internal fun elapsedMillis(): Long {
    val start = startNanos
    if (start == 0L) return stoppedElapsedMillis
    return ((SystemClock.elapsedRealtimeNanos() - start) / 1_000_000).coerceAtLeast(0)
  }
}
