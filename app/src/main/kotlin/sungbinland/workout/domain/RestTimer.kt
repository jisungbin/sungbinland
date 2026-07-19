package sungbinland.workout.domain

import android.os.SystemClock

internal class RestTimer {
  // 기본 55초 고정, +10초로 목표 시간 연장.
  var targetMillis: Long = 55_000L
    private set

  private var startNanos: Long = 0L
  private var stoppedElapsedMillis: Long = 0L

  fun start() {
    startNanos = SystemClock.elapsedRealtimeNanos()
  }

  fun stop() {
    if (startNanos != 0L) {
      stoppedElapsedMillis = ((SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000).coerceAtLeast(0)
    }
    startNanos = 0L
  }

  fun extend(millis: Long) {
    targetMillis += millis
  }

  fun elapsedMillis(): Long {
    val start = startNanos
    if (start == 0L) return stoppedElapsedMillis
    return ((SystemClock.elapsedRealtimeNanos() - start) / 1_000_000).coerceAtLeast(0)
  }

  fun remainingMillis(): Long = (targetMillis - elapsedMillis()).coerceAtLeast(0L)

  fun isFinished(): Boolean = elapsedMillis() >= targetMillis
}
