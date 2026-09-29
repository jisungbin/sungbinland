package sungbinland.workout.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import sungbinland.workout.data.ExercisePickStore
import sungbinland.workout.data.FreeRoutineStore
import sungbinland.workout.data.SetCountStore
import sungbinland.workout.data.WeekOrderStore
import sungbinland.workout.domain.DAY_LABELS
import sungbinland.workout.domain.KNOWN_EXERCISES
import sungbinland.workout.domain.RoutineDay
import sungbinland.workout.domain.exerciseOptions
import sungbinland.workout.domain.exercises
import sungbinland.workout.domain.freeRoutineExercises
import sungbinland.workout.domain.routineOf
import sungbinland.workout.domain.todayDayIndex
import sungbinland.workout.event.EventBus
import sungbinland.workout.event.FirstSetChanged
import sungbinland.workout.event.SetCountsChanged
import sungbinland.workout.event.Subscription
import sungbinland.workout.haptic.Haptics

internal fun installWorkoutView(activity: Activity): WorkoutViewHandle {
  val weekOrder = WeekOrderStore(activity)
  val freeRoutine = FreeRoutineStore(activity)
  val dayIndex = todayDayIndex()
  val today = routineOf(dayIndex, weekOrder.order)
  val picks = ExercisePickStore(activity, weekOrder.order[dayIndex])
  // 자유 루틴은 오늘의 요일 루틴(휴식일 포함)을 통째로 대신한다.
  val exercises = when {
    freeRoutine.isActive -> freeRoutineExercises(freeRoutine.names)
    today != null -> today.exercises(picks = picks.picks)
    else -> emptyList()
  }
  val store = SetCountStore(activity, exercises)
  val view = WorkoutView(activity, store, weekOrder, freeRoutine, picks, dayIndex, today)
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
  private val freeRoutine: FreeRoutineStore,
  private val exercisePicks: ExercisePickStore,
  private val dayIndex: Int,
  private val today: RoutineDay?,
) {
  // RoutineExercise.slotPosition으로 찾는 자리별 종목 후보(요일 루틴 전용).
  private val exerciseOptions: List<List<String>> = today?.exerciseOptions().orEmpty()

  private val hasWorkout: Boolean = store.todayExercises.isNotEmpty()

  private val subscriptions = mutableListOf<Subscription>()

  private val startTimeView: TextView
  private val cardAdapter = CardAdapter()
  private val confettiView: ConfettiView

  private var firstSetEpochMillis: Long = 0L
  private var completedSets: Int = 0

  val root: View

  /*
   * 화면은 세 칸이다: 고정 헤더(시작 시각·제목·루틴 선택) / 카드 목록 / 고정 하단(휴식 타이머).
   * 스크롤은 가운데 ListView 안에서만 일어나 헤더와 타이머 버튼은 항상 제자리에 있다.
   */
  init {
    val header = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
    }

    startTimeView = TextView(activity).apply {
      setTextColor(Palette.MUTED)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
      visibility = if (hasWorkout) View.VISIBLE else View.GONE
      setOnClickListener { openStartTimeEditor() }
    }
    header.addView(startTimeView, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

    header.addView(
      TextView(activity).apply {
        text = if (freeRoutine.isActive) "자유 루틴" else "오늘의 루틴"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      },
      rowParams(activity.dp(if (hasWorkout) 8 else 0)),
    )

    // 휴식일에도 띄운다 — 여기서 평일 루틴이나 자유 루틴을 끌어와야 쉬는 날에 운동할 수 있다.
    header.addView(
      TextView(activity).apply {
        // 실제 오늘 요일을 쓴다 — 스왑하면 today.day는 원래 요일이라 화면과 어긋난다.
        val swappedFrom = today?.day?.takeIf { weekOrder.order[dayIndex] != dayIndex }
        text = buildString {
          append(DAY_LABELS[dayIndex])
          if (freeRoutine.isActive) {
            append(" · 종목 직접 입력")
          } else {
            append(" · ${today?.category ?: "휴식"}")
            if (swappedFrom != null) append(" · $swappedFrom 루틴과 교환됨")
          }
          append("  ▾")
        }
        setTextColor(Palette.ACCENT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        setPadding(0, activity.dp(2), 0, activity.dp(2))
        setOnClickListener { openSwapPicker() }
      },
      rowParams(activity.dp(4)),
    )

    val body: View = if (hasWorkout) {
      ListView(activity).apply {
        adapter = cardAdapter
        // 카드 사이 간격은 투명 구분선으로 낸다 — ListView 아이템은 margin을 받지 않는다.
        divider = ColorDrawable(Color.TRANSPARENT)
        dividerHeight = activity.dp(8)
        selector = ColorDrawable(Color.TRANSPARENT)
        cacheColorHint = Color.TRANSPARENT
        clipToPadding = false
        setPadding(activity.dp(16), activity.dp(12), activity.dp(16), activity.dp(12))
      }
    } else {
      FrameLayout(activity).apply {
        setPadding(activity.dp(16), activity.dp(12), activity.dp(16), 0)
        addView(restDayText(), FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
      }
    }

    val footer = FrameLayout(activity).apply {
      visibility = if (hasWorkout) View.VISIBLE else View.GONE
      addView(
        activity.flatButton("휴식 타이머 시작") { openRestTimer() }.apply {
          setPadding(activity.dp(20), activity.dp(16), activity.dp(20), activity.dp(16))
        },
        FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT),
      )
    }

    val column = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(Palette.BG)
      addView(header, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
      addView(body, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
      addView(footer, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }
    confettiView = ConfettiView(activity)

    val frame = FrameLayout(activity)
    frame.addView(column, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    frame.addView(confettiView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    // status bar/navigation bar에 가려지지 않도록 고정 영역에 시스템 바 인셋만큼 패딩.
    frame.setOnApplyWindowInsetsListener { _, insets ->
      val bars = insets.getInsets(WindowInsets.Type.systemBars())
      header.setPadding(activity.dp(16), activity.dp(20) + bars.top, activity.dp(16), activity.dp(4))
      footer.setPadding(activity.dp(16), activity.dp(16), activity.dp(16), activity.dp(24) + bars.bottom)
      insets
    }
    root = frame
  }

  fun start() {
    if (!hasWorkout) return

    subscriptions.add(
      EventBus.subscribe(SetCountsChanged::class.java) { event ->
        completedSets = event.counts.sum()
        renderItems(event.counts)
        // 매 세트(=휴식 타이머 완료)마다만 재계산 — 1초 폴링 대신 배터리를 아낀다.
        updateStartTime()
      },
    )
    subscriptions.add(
      EventBus.subscribe(FirstSetChanged::class.java) { event ->
        firstSetEpochMillis = event.epochMillis
        updateStartTime()
      },
    )
  }

  fun dispose() {
    subscriptions.forEach { it.cancel() }
    subscriptions.clear()
  }

  private fun updateStartTime() {
    val start = firstSetEpochMillis
    startTimeView.text = if (start == 0L) {
      "시작 전 · 시작 시각 입력  ▾"
    } else {
      val clock = LocalTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault())
      val minutes = ((System.currentTimeMillis() - start) / 60_000L).coerceAtLeast(0L)
      String.format(Locale.KOREA, "%02d:%02d 시작 · %d분 경과  ▾", clock.hour, clock.minute, minutes)
    }
  }

  private fun openStartTimeEditor() {
    StartTimeDialog.show(activity, firstSetEpochMillis) { epochMillis -> store.setStartTime(epochMillis) }
  }

  // 진행한 세트가 하나라도 있으면 루틴 변경을 막는다 — 세트 진행도를 버리지 않고 안전하게 유지하려는 선택.
  private fun openSwapPicker() {
    if (completedSets > 0) {
      Toast.makeText(activity, "이미 진행한 세트가 있어 오늘 루틴을 바꿀 수 없습니다.", Toast.LENGTH_SHORT).show()
      return
    }
    SwapPickerDialog.show(
      activity = activity,
      dayIndex = dayIndex,
      order = weekOrder.order,
      freeRoutineActive = freeRoutine.isActive,
      onPickedDay = { otherDayIndex ->
        // 자유 루틴 중에 요일을 고르면 자유 루틴을 끝내고, 다른 요일이면 교환까지 한다.
        if (freeRoutine.isActive) freeRoutine.deactivate()
        if (otherDayIndex != dayIndex) weekOrder.swap(dayIndex, otherDayIndex)
        // 종목 목록이 통째로 바뀌므로 화면을 다시 세운다.
        activity.recreate()
      },
      onPickedFreeRoutine = {
        freeRoutine.activate()
        activity.recreate()
      },
    )
  }

  // 세트를 채운 종목은 바꾸지 못한다 — 진행도는 종목 이름으로 저장돼 있어 바꾸는 순간 그 종목 기록만 사라진다.
  private fun openExercisePicker(index: Int, count: Int) {
    if (count > 0) {
      Toast.makeText(activity, "이미 진행한 종목은 바꿀 수 없습니다.", Toast.LENGTH_SHORT).show()
      return
    }
    val exercise = store.todayExercises[index]
    if (freeRoutine.isActive) {
      ExerciseInputDialog.show(
        activity = activity,
        title = exercise.part,
        current = exercise.name,
        // 최근에 직접 입력한 이름을 먼저 추천한다.
        suggestions = (freeRoutine.history + KNOWN_EXERCISES).distinct(),
      ) { name ->
        freeRoutine.setName(exercise.slotPosition, name)
        activity.recreate()
      }
      return
    }
    ExercisePickerDialog.show(
      activity = activity,
      part = exercise.part,
      current = exercise.name,
      options = pickableOptions(index),
    ) { name ->
      exercisePicks.pick(exercise.slotPosition, name)
      // 종목이 바뀌면 세트 저장 키도 함께 바뀌므로 화면을 다시 세운다.
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
    // 연달아 나오는 같은 부위 종목(중부등 2종목 등)은 한 카드로 묶는다. 세트 카운트는 종목별로 유지.
    val exercises = store.todayExercises
    val groups = mutableListOf<List<Int>>()
    var index = 0
    while (index < exercises.size) {
      val part = exercises[index].part
      val positions = mutableListOf<Int>()
      while (index < exercises.size && exercises[index].part == part) {
        positions += index
        index++
      }
      groups += positions
    }
    cardAdapter.submit(groups, counts)
  }

  // 카드 하나 = 같은 부위 종목 묶음 하나. 카드 수가 적고 묶음마다 줄 수가 달라 뷰 재사용 없이 매번 새로 그린다.
  private inner class CardAdapter : BaseAdapter() {
    private var groups: List<List<Int>> = emptyList()
    private var counts: List<Int> = emptyList()

    fun submit(groups: List<List<Int>>, counts: List<Int>) {
      this.groups = groups
      this.counts = counts
      notifyDataSetChanged()
    }

    override fun getCount(): Int = groups.size

    override fun getItem(position: Int): List<Int> = groups[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
      partCard(store.todayExercises[groups[position].first()].part, groups[position], counts)
  }

  private fun partCard(part: String, positions: List<Int>, counts: List<Int>): View {
    val exercises = store.todayExercises
    val allDone = positions.all { position -> counts.getOrElse(position) { 0 } >= exercises[position].targetSets }

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
      positions.forEachIndexed { order, position ->
        addView(
          exerciseRow(position, counts.getOrElse(position) { 0 }),
          rowParams(activity.dp(if (order == 0) 4 else 10)),
        )
      }
    }
  }

  // 같은 부위가 이미 쓰고 있는 종목은 뺀다 — 세트 진행도가 종목 이름 키라, 겹치면 두 줄이 한 카운터를 공유한다.
  private fun pickableOptions(index: Int): List<String> {
    val exercises = store.todayExercises
    val taken = exercises
      .filterIndexed { other, exercise -> other != index && exercise.part == exercises[index].part }
      .mapTo(mutableSetOf()) { it.name }
    return exerciseOptions.getOrNull(exercises[index].slotPosition).orEmpty().filterNot { it in taken }
  }

  private fun exerciseRow(index: Int, count: Int): View {
    val exercise = store.todayExercises[index]
    val done = count >= exercise.targetSets
    // 자유 루틴은 모든 자리를 직접 채우므로 늘 바꿀 수 있다.
    val changeable = freeRoutine.isActive || pickableOptions(index).size > 1
    val empty = exercise.name.isEmpty()

    val name = TextView(activity).apply {
      val label = if (empty) "종목 이름 입력" else exercise.name
      text = if (changeable && !done) "$label  ▾" else label
      setTextColor(
        when {
          empty -> Palette.ACCENT
          done -> Palette.MUTED
          else -> Palette.TEXT
        },
      )
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
      if (changeable) setOnClickListener { openExercisePicker(index, count) }
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
