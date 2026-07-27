package sungbinland.workout.ui

import android.app.Activity
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import sungbinland.workout.data.SetCountStore
import sungbinland.workout.domain.RoutineDay
import sungbinland.workout.domain.todayRoutine
import sungbinland.workout.haptic.Haptics

internal fun installWorkoutView(activity: Activity): WorkoutViewHandle {
  val store = SetCountStore(activity)
  val view = WorkoutView(activity, store)
  activity.setContentView(view.root)
  view.start()
  return WorkoutViewHandle(view)
}

internal class WorkoutViewHandle(private val view: WorkoutView) {
  fun dispose() {
    view.dispose()
  }
}

internal class WorkoutView(
  private val activity: Activity,
  private val store: SetCountStore,
) {
  private val disposables = CompositeDisposable()
  private val tickerHandler = Handler(Looper.getMainLooper())
  private val today: RoutineDay? = todayRoutine()

  private val elapsedTimeView: TextView
  private val itemsContainer: LinearLayout
  private val confettiView: ConfettiView

  private var firstSetEpochMillis: Long = 0L

  val root: View

  init {
    val column = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(Palette.BG)
      setPadding(activity.dp(16), activity.dp(20), activity.dp(16), activity.dp(24))
    }

    elapsedTimeView = TextView(activity).apply {
      setTextColor(Palette.MUTED)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
      visibility = View.GONE
    }
    column.addView(elapsedTimeView)

    column.addView(
      TextView(activity).apply {
        text = "오늘의 루틴"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      },
      rowParams(activity.dp(8)),
    )

    itemsContainer = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
    }
    column.addView(itemsContainer, rowParams(activity.dp(12)))

    if (today != null) {
      column.addView(
        activity.flatButton("휴식 타이머 시작") { openRestTimer() }.apply {
          setPadding(activity.dp(20), activity.dp(16), activity.dp(20), activity.dp(16))
        },
        rowParams(activity.dp(16)),
      )
    }

    val scroll = ScrollView(activity).apply {
      setBackgroundColor(Palette.BG)
      clipToPadding = false
      addView(column, MATCH_PARENT, WRAP_CONTENT)
    }
    confettiView = ConfettiView(activity)

    val frame = android.widget.FrameLayout(activity)
    frame.addView(scroll, android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    frame.addView(confettiView, android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    // status bar/navigation bar에 가려지지 않도록 스크롤 콘텐츠에 시스템 바 인셋만큼 패딩.
    frame.setOnApplyWindowInsetsListener { _, insets ->
      val bars = insets.getInsets(WindowInsets.Type.systemBars())
      scroll.setPadding(0, bars.top, 0, bars.bottom)
      insets
    }
    root = frame
  }

  fun start() {
    if (today == null) {
      itemsContainer.addView(restDayText())
      return
    }

    disposables.add(
      store.todayItemCounts
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { counts -> renderItems(today, counts) },
    )
    disposables.add(
      store.firstSetEpochMillis
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { epoch ->
          firstSetEpochMillis = epoch
          updateElapsedTime()
        },
    )
    tickerHandler.post(elapsedTicker)
  }

  fun dispose() {
    tickerHandler.removeCallbacks(elapsedTicker)
    disposables.clear()
  }

  private val elapsedTicker = object : Runnable {
    override fun run() {
      updateElapsedTime()
      tickerHandler.postDelayed(this, 1_000L)
    }
  }

  private fun updateElapsedTime() {
    val start = firstSetEpochMillis
    if (start == 0L) {
      elapsedTimeView.visibility = View.GONE
      return
    }
    val minutes = (System.currentTimeMillis() - start) / 60_000L
    elapsedTimeView.text = "첫 세트로부터 ${minutes}분 경과"
    elapsedTimeView.visibility = View.VISIBLE
  }

  private fun openRestTimer() {
    RestTimerDialog.show(activity) {
      Haptics.vibrateHeavy(activity.applicationContext)
      store.recordCompletedSet()
      // 진동 지속시간만큼 콘페티를 계속 쏟아붓는다.
      confettiView.burst(Haptics.VIBRATION_MILLIS)
    }
  }

  private fun renderItems(day: RoutineDay, counts: List<Int>) {
    itemsContainer.removeAllViews()
    day.items.forEachIndexed { index, item ->
      val done = counts.getOrElse(index) { 0 } >= item.targetSets
      itemsContainer.addView(
        TextView(activity).apply {
          text = if (done) {
            "✓ ${item.name} · ${item.targetSets}/${item.targetSets}세트"
          } else {
            "${item.name} · ${counts.getOrElse(index) { 0 }}/${item.targetSets}세트"
          }
          setTextColor(if (done) Palette.MUTED else Palette.TEXT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
          paintFlags = if (done) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
          setPadding(0, activity.dp(6), 0, activity.dp(6))
        },
      )
    }
  }

  private fun restDayText(): View =
    TextView(activity).apply {
      text = "오늘은 휴식일입니다."
      setTextColor(Palette.MUTED)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER)
      setPadding(activity.dp(20), activity.dp(24), activity.dp(20), activity.dp(24))
    }

  private fun rowParams(topMarginPx: Int): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = topMarginPx }
}
