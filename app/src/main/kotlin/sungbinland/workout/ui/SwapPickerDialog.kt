package sungbinland.workout.ui

import android.app.Activity
import android.app.Dialog
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import sungbinland.workout.domain.DAY_LABELS
import sungbinland.workout.domain.routineOf

/*
 * 오늘 루틴을 고르는 다이얼로그: 다른 요일 루틴과 맞바꾸거나, 종목을 직접 채우는 자유 루틴으로 전환한다.
 * 자유 루틴 중이면 onPickedDay(dayIndex)가 "원래 루틴으로 돌아가기"를 뜻한다.
 */
internal object SwapPickerDialog {
  fun show(
    activity: Activity,
    dayIndex: Int,
    order: List<Int>,
    freeRoutineActive: Boolean,
    onPickedDay: (Int) -> Unit,
    onPickedFreeRoutine: () -> Unit,
  ) {
    val content = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER, radiusDp = 16, strokeDp = 1)
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(12))
    }
    content.addView(
      TextView(activity).apply {
        text = "오늘 어떤 루틴으로 할까요?"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      },
    )
    content.addView(
      TextView(activity).apply {
        text = "요일 교환은 이번 주에만, 자유 루틴은 오늘만 적용됩니다."
        setTextColor(Palette.MUTED)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setPadding(0, activity.dp(4), 0, 0)
      },
    )

    val dialog = Dialog(activity).apply {
      setContentView(content, FrameLayout.LayoutParams(activity.dp(320), WRAP_CONTENT))
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setGravity(Gravity.CENTER)
    }

    val list = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    fun option(label: String, onClick: () -> Unit) {
      list.addView(
        TextView(activity).apply {
          text = label
          setTextColor(Palette.TEXT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
          setPadding(activity.dp(4), activity.dp(14), activity.dp(4), activity.dp(14))
          setOnClickListener {
            dialog.dismiss()
            onClick()
          }
        },
        LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(4) },
      )
    }

    if (freeRoutineActive) {
      val own = routineOf(dayIndex, order)
      option("원래 루틴으로 돌아가기 · ${own?.category ?: "휴식"}") { onPickedDay(dayIndex) }
    } else {
      option("자유 루틴 · 종목 직접 입력") { onPickedFreeRoutine() }
    }
    // 루틴이 없는 날(휴식일)도 목록에 남긴다 — 빼버리면 휴식일로 옮긴 루틴을 되돌릴 방법이 사라진다.
    DAY_LABELS.indices.filter { it != dayIndex }.forEach { otherDayIndex ->
      val routine = routineOf(otherDayIndex, order)
      option("${DAY_LABELS[otherDayIndex]} · ${routine?.category ?: "휴식"}") { onPickedDay(otherDayIndex) }
    }
    // 선택지가 8개라 작은 화면에서 닫기 버튼이 밀려나지 않도록 목록만 스크롤한다.
    content.addView(
      ScrollView(activity).apply { addView(list, MATCH_PARENT, WRAP_CONTENT) },
      LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f).apply { topMargin = activity.dp(8) },
    )
    content.addView(
      activity.flatButton("닫기") { dialog.dismiss() },
      LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(12) },
    )

    dialog.show()
  }
}
