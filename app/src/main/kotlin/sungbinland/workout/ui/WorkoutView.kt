package sungbinland.workout.ui

import android.app.Activity
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
  private val setCountView: TextView
  private val confettiView: ConfettiView

  val root: View

  init {
    val column = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(Palette.BG)
      setPadding(activity.dp(16), activity.dp(20), activity.dp(16), activity.dp(24))
    }

    column.addView(
      TextView(activity).apply {
        text = "오늘의 루틴"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      },
    )
    column.addView(buildTodayRoutine(), rowParams(activity.dp(12)))

    setCountView = TextView(activity).apply {
      setTextColor(Palette.ACCENT)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER)
      setPadding(activity.dp(16), activity.dp(14), activity.dp(16), activity.dp(14))
    }
    column.addView(setCountView, rowParams(activity.dp(24)))

    column.addView(
      activity.flatButton("휴식 타이머 시작") { openRestTimer() }.apply {
        setPadding(activity.dp(20), activity.dp(16), activity.dp(20), activity.dp(16))
      },
      rowParams(activity.dp(16)),
    )

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
    disposables.add(
      store.todaySetCount
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { count -> setCountView.text = "오늘 수행 세트: ${count}세트" },
    )
  }

  fun dispose() {
    disposables.clear()
  }

  private fun openRestTimer() {
    RestTimerDialog.show(activity) {
      Haptics.vibrateHeavy(activity.applicationContext)
      store.recordCompletedSet()
      // 진동 지속시간만큼 콘페티를 계속 쏟아붓는다.
      confettiView.burst(Haptics.VIBRATION_MILLIS)
    }
  }

  private fun buildTodayRoutine(): View {
    val today: RoutineDay = todayRoutine()
      ?: return TextView(activity).apply {
        text = "오늘은 휴식일입니다."
        setTextColor(Palette.MUTED)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        background = activity.borderedBox(Palette.SURFACE, Palette.BORDER)
        setPadding(activity.dp(20), activity.dp(24), activity.dp(20), activity.dp(24))
      }

    val card = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER)
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(20))
    }

    // 분류: 메인으로 크게 강조
    card.addView(
      TextView(activity).apply {
        text = today.category
        setTextColor(Palette.ACCENT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        setLineSpacing(activity.dp(4).toFloat(), 1f)
      },
    )

    // 20세트 구성: 항목별로 줄바꿈해 나열
    card.addView(
      TextView(activity).apply {
        text = "20세트 구성"
        setTextColor(Palette.MUTED)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setPadding(0, activity.dp(18), 0, activity.dp(8))
      },
    )
    today.composition.split("+").forEach { part ->
      card.addView(
        TextView(activity).apply {
          text = "· ${part.trim()}"
          setTextColor(Palette.TEXT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
          setPadding(0, activity.dp(3), 0, activity.dp(3))
        },
      )
    }

    // 마지막 4세트: 보조 정보
    card.addView(
      TextView(activity).apply {
        text = "마지막 4세트 · ${today.lastFour}"
        setTextColor(Palette.MUTED)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setPadding(0, activity.dp(18), 0, 0)
      },
    )
    return card
  }

  private fun rowParams(topMarginPx: Int): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = topMarginPx }
}
