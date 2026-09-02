package sungbinland.workout.event

import android.os.Handler
import android.os.Looper

internal interface Event

internal fun interface Subscription {
  fun cancel()
}

// 타입별로 마지막 이벤트를 붙들었다가 구독 즉시 되돌려준다(sticky) — 뷰가 이벤트보다 늦게 붙어도 현재 상태를 놓치지 않는다.
internal object EventBus {
  private val mainHandler = Handler(Looper.getMainLooper())
  private val lock = Any()
  private val listeners = HashMap<Class<out Event>, MutableList<(Event) -> Unit>>()
  private val stickyEvents = HashMap<Class<out Event>, Event>()

  fun <T : Event> subscribe(type: Class<T>, listener: (T) -> Unit): Subscription {
    @Suppress("UNCHECKED_CAST")
    val erased = listener as (Event) -> Unit

    val sticky = synchronized(lock) {
      listeners.getOrPut(type) { mutableListOf() }.add(erased)
      stickyEvents[type]
    }
    if (sticky != null) deliverOnMain(erased, sticky)

    return Subscription {
      synchronized(lock) { listeners[type]?.remove(erased) }
    }
  }

  fun post(event: Event) {
    val targets = synchronized(lock) {
      stickyEvents[event.javaClass] = event
      listeners[event.javaClass]?.toList()
    } ?: return
    targets.forEach { listener -> deliverOnMain(listener, event) }
  }

  // 구독자가 뷰를 만지므로 전달은 항상 메인 스레드에서.
  private fun deliverOnMain(listener: (Event) -> Unit, event: Event) {
    if (Looper.myLooper() == Looper.getMainLooper()) listener(event) else mainHandler.post { listener(event) }
  }
}
