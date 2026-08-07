package sungbinland.workout.rx

import android.os.Handler
import android.os.Looper
import android.os.Message
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.TimeUnit

// 메인 Looper에 작업을 싣는 Scheduler. RxAndroid를 들이지 않으려고 직접 둔다.
internal object MainThreadScheduler : Scheduler() {
  private val handler = Handler(Looper.getMainLooper())

  override fun createWorker(): Worker = HandlerWorker(handler)
}

private class HandlerWorker(private val handler: Handler) : Scheduler.Worker() {
  @Volatile private var disposed = false

  override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
    if (disposed) return Disposables.disposed()

    val scheduled = ScheduledRunnable(handler, RxJavaPlugins.onSchedule(run))
    // worker를 토큰으로 달아둬야 dispose에서 이 worker가 예약한 것만 골라 걷어낼 수 있다.
    val message = Message.obtain(handler, scheduled).also { it.obj = this }
    handler.sendMessageDelayed(message, unit.toMillis(delay))

    // 큐에 넣는 사이 dispose됐다면 위 일괄 제거가 이 작업을 지나쳤을 수 있다.
    if (disposed) {
      handler.removeCallbacks(scheduled)
      return Disposables.disposed()
    }
    return scheduled
  }

  override fun dispose() {
    disposed = true
    handler.removeCallbacksAndMessages(this)
  }

  override fun isDisposed(): Boolean = disposed
}

private class ScheduledRunnable(
  private val handler: Handler,
  private val delegate: Runnable,
) : Runnable, Disposable {
  @Volatile private var disposed = false

  override fun run() {
    try {
      delegate.run()
    } catch (throwable: Throwable) {
      // 메인 Looper까지 예외가 올라가면 앱이 죽는다. Rx 규약대로 전역 핸들러에 넘긴다.
      RxJavaPlugins.onError(throwable)
    }
  }

  override fun dispose() {
    disposed = true
    handler.removeCallbacks(this)
  }

  override fun isDisposed(): Boolean = disposed
}
