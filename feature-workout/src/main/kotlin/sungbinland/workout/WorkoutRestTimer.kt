package sungbinland.workout

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue

@Stable
internal class WorkoutRestTimer {
  internal var startNanos: Long by mutableLongStateOf(0L)
    private set

  internal val isRunning: Boolean get() = startNanos != 0L

  internal fun start(nanos: Long) {
    startNanos = nanos
  }

  internal fun stop() {
    startNanos = 0L
  }
}
