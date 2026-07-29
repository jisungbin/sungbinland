package sungbinland.workout.ui

import android.app.Activity
import android.graphics.Paint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import sungbinland.workout.data.SetCountStore
import sungbinland.workout.data.WeekOrderStore
import sungbinland.workout.domain.DAY_LABELS
import sungbinland.workout.domain.RoutineDay
import sungbinland.workout.domain.RoutineExercise
import sungbinland.workout.domain.exercises
import sungbinland.workout.domain.routineOf
import sungbinland.workout.domain.todayDayIndex
import sungbinland.workout.haptic.Haptics

internal fun installWorkoutView(activity: Activity): WorkoutViewHandle {
  val weekOrder = WeekOrderStore(activity)
  val dayIndex = todayDayIndex()
  val today = dayIndex?.let { routineOf(it, weekOrder.order) }
  val store = SetCountStore(activity, today?.exercises() ?: emptyList())
  val view = WorkoutView(activity, store, weekOrder, dayIndex, today)
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
  private val weekOrder: WeekOrderStore,
  private val dayIndex: Int?,
  private val today: RoutineDay?,
) {
  private val disposables = CompositeDisposable()

  private val elapsedTimeView: TextView
  private val itemsContainer: LinearLayout
  private val confettiView: ConfettiView

  private var firstSetEpochMillis: Long = 0L
  private var completedSets: Int = 0

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

    if (today != null && dayIndex != null) {
      column.addView(
        TextView(activity).apply {
          // 실제 오늘 요일을 쓴다 — 스왑하면 today.day는 원래 요일이라 화면과 어긋난다.
          val swappedFrom = today.day.takeIf { weekOrder.order[dayIndex] != dayIndex }
          text = buildString {
            append("${DAY_LABELS[dayIndex]} · ${today.category}")
            if (swappedFrom != null) append(" · $swappedFrom 루틴과 교환됨")
            append("  ▾")
          }
          setTextColor(Palette.ACCENT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
          setPadding(0, activity.dp(2), 0, activity.dp(2))
          setOnClickListener { openSwapPicker() }
        },
        rowParams(activity.dp(4)),
      )
    }

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
      store.todayExerciseCounts
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { counts ->
          completedSets = counts.sum()
          renderItems(counts)
          // 매 세트(=휴식 타이머 완료)마다만 재계산 — 1초 폴링 대신 배터리를 아낀다.
          updateElapsedTime()
        },
    )
    disposables.add(
      store.firstSetEpochMillis
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { epoch ->
          firstSetEpochMillis = epoch
          updateElapsedTime()
        },
    )
  }

  fun dispose() {
    disposables.clear()
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

  // 진행한 세트가 하나라도 있으면 스왑을 막는다 — 세트 진행도를 버리지 않고 안전하게 유지하려는 선택.
  private fun openSwapPicker() {
    if (dayIndex == null) return
    if (completedSets > 0) {
      Toast.makeText(activity, "이미 진행한 세트가 있어 오늘 루틴을 바꿀 수 없습니다.", Toast.LENGTH_SHORT).show()
      return
    }
    SwapPickerDialog.show(activity, dayIndex, weekOrder.order) { otherDayIndex ->
      weekOrder.swap(dayIndex, otherDayIndex)
      // 종목 목록이 통째로 바뀌므로 화면을 다시 세운다.
      activity.recreate()
    }
  }

  private fun openRestTimer() {
    RestTimerDialog.show(activity) {
      Haptics.vibrateHeavy(activity.applicationContext)
      store.recordCompletedSet()
      // 진동 지속시간만큼 콘페티를 계속 쏟아붓는다.
      confettiView.burst(Haptics.VIBRATION_MILLIS)
    }
  }

  private fun renderItems(counts: List<Int>) {
    itemsContainer.removeAllViews()
    // 같은 부위 종목(2종목 슬롯, 화요일 웜업 2종목 등)은 한 카드로 묶는다. 세트 카운트는 종목별로 유지.
    val exercises = store.todayExercises
    var index = 0
    while (index < exercises.size) {
      val part = exercises[index].part
      val group = mutableListOf<Pair<RoutineExercise, Int>>()
      while (index < exercises.size && exercises[index].part == part) {
        group += exercises[index] to counts.getOrElse(index) { 0 }
        index++
      }
      itemsContainer.addView(
        partCard(part, group),
        rowParams(if (itemsContainer.childCount == 0) 0 else activity.dp(8)),
      )
    }
  }

  private fun partCard(part: String, entries: List<Pair<RoutineExercise, Int>>): View {
    val allDone = entries.all { (exercise, count) -> count >= exercise.targetSets }

    return LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(
        fill = if (allDone) Palette.BG else Palette.SURFACE,
        stroke = Palette.BORDER,
        radiusDp = 12,
        strokeDp = 1,
      )
      setPadding(activity.dp(16), activity.dp(12), activity.dp(16), activity.dp(14))
      addView(
        TextView(activity).apply {
          text = part
          setTextColor(if (allDone) Palette.BORDER else Palette.MUTED)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
          letterSpacing = 0.06f
        },
      )
      entries.forEachIndexed { position, (exercise, count) ->
        addView(exerciseRow(exercise, count), rowParams(activity.dp(if (position == 0) 4 else 10)))
      }
    }
  }

  private fun exerciseRow(exercise: RoutineExercise, count: Int): View {
    val done = count >= exercise.targetSets

    val name = TextView(activity).apply {
      text = exercise.name
      setTextColor(if (done) Palette.MUTED else Palette.TEXT)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      paintFlags = if (done) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    val counter = LinearLayout(activity).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.BOTTOM
      addView(
        TextView(activity).apply {
          text = if (done) "✓" else "$count/${exercise.targetSets}"
          setTextColor(if (done) Palette.MUTED else Palette.ACCENT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        },
      )
      if (!done) {
        addView(
          TextView(activity).apply {
            text = "세트"
            setTextColor(Palette.MUTED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setPadding(activity.dp(3), 0, 0, activity.dp(2))
          },
        )
      }
    }

    return LinearLayout(activity).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
      addView(name, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
      addView(counter, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
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
