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
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import sungbinland.workout.data.SetCountStore
import sungbinland.workout.domain.dailyExercises
import sungbinland.workout.event.EventBus
import sungbinland.workout.event.FirstSetChanged
import sungbinland.workout.event.SetCountsChanged
import sungbinland.workout.event.Subscription
import sungbinland.workout.haptic.Haptics

internal fun installWorkoutView(activity: Activity): WorkoutViewHandle {
  val exercises = dailyExercises()
  val store = SetCountStore(activity, exercises)
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
  private val hasWorkout: Boolean = store.todayExercises.isNotEmpty()

  private val subscriptions = mutableListOf<Subscription>()

  private val startTimeView: TextView
  private val cardAdapter = CardAdapter()
  private val confettiView: ConfettiView

  private var firstSetEpochMillis: Long = 0L

  val root: View

  /*
   * 화면은 고정 헤더(시작 시각·제목) / 카드 목록 / 하단 휴식 타이머로 구성한다.
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
        text = "오늘의 운동"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      },
      rowParams(activity.dp(if (hasWorkout) 8 else 0)),
    )

    val body = ListView(activity).apply {
        adapter = cardAdapter
        // 카드 사이 간격은 투명 구분선으로 낸다 — ListView 아이템은 margin을 받지 않는다.
        divider = ColorDrawable(Color.TRANSPARENT)
        dividerHeight = activity.dp(12)
        selector = ColorDrawable(Color.TRANSPARENT)
        cacheColorHint = Color.TRANSPARENT
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(activity.dp(24))
        clipToPadding = false
        setPadding(activity.dp(12), activity.dp(12), activity.dp(12), activity.dp(12))
    }

    val footer = FrameLayout(activity).apply {
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
    subscriptions.add(
      EventBus.subscribe(SetCountsChanged::class.java) { event ->
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

  private fun openRestTimer() {
    RestTimerDialog.show(activity) {
      Haptics.vibrateHeavy(activity.applicationContext)
      store.recordCompletedSet()
      // 진동 지속시간만큼 콘페티를 계속 쏟아붓는다.
      confettiView.burst(Haptics.VIBRATION_MILLIS)
    }
  }

  private fun renderItems(counts: List<Int>) {
    cardAdapter.submit(store.todayExercises.indices.toList(), counts)
  }

  // 종목마다 카드 하나를 표시한다. 카드마다 구성이 달라 뷰 재사용 없이 매번 새로 그린다.
  private inner class CardAdapter : BaseAdapter() {
    private var positions: List<Int> = emptyList()
    private var counts: List<Int> = emptyList()

    fun submit(positions: List<Int>, counts: List<Int>) {
      this.positions = positions
      this.counts = counts
      notifyDataSetChanged()
    }

    override fun getCount(): Int = positions.size

    override fun getItem(position: Int): Int = positions[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
      exerciseCard(positions[position], counts)
  }

  private fun exerciseCard(position: Int, counts: List<Int>): View {
    val exercises = store.todayExercises
    val allDone = counts.getOrElse(position) { 0 } >= exercises[position].targetSets

    return LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(
        fill = if (allDone) Palette.BG else Palette.SURFACE,
        stroke = Palette.BORDER,
        radiusDp = 12,
        strokeDp = 1,
      )
      minimumHeight = activity.dp(88)
      gravity = Gravity.CENTER_VERTICAL
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(20))
      addView(exerciseRow(position, counts.getOrElse(position) { 0 }))
    }
  }

  private fun exerciseRow(index: Int, count: Int): View {
    val exercise = store.todayExercises[index]
    val done = count >= exercise.targetSets
    val name = TextView(activity).apply {
      val ordinal = listOf("첫 종목", "둘째 종목", "셋째 종목", "넷째 종목").getOrElse(index) { "종목 ${index + 1}" }
      text = ordinal
      setTextColor(
        when {
          done -> Palette.MUTED
          else -> Palette.TEXT
        },
      )
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
      paintFlags = if (done) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    val counter = LinearLayout(activity).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.BOTTOM
      addView(
        TextView(activity).apply {
          text = if (done) "✓" else "$count/${exercise.targetSets}"
          setTextColor(if (done) Palette.MUTED else Palette.ACCENT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
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

  private fun rowParams(topMarginPx: Int): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = topMarginPx }
}
