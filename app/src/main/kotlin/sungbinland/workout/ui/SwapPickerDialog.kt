package sungbinland.workout.ui

import android.app.Activity
import android.app.Dialog
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import sungbinland.workout.domain.DAY_LABELS
import sungbinland.workout.domain.routineOf

// 오늘 루틴을 다른 요일 루틴과 맞바꾸기 위한 요일 선택 다이얼로그.
internal object SwapPickerDialog {
  fun show(activity: Activity, dayIndex: Int, order: List<Int>, onPicked: (Int) -> Unit) {
    val content = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER, radiusDp = 16, strokeDp = 1)
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(12))
    }
    content.addView(
      TextView(activity).apply {
        text = "어느 요일 루틴과 바꿀까요?"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      },
    )
    content.addView(
      TextView(activity).apply {
        text = "이번 주에만 적용되고 월요일에 원래대로 돌아갑니다."
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

    // 루틴이 없는 날(휴식일)도 목록에 남긴다 — 빼버리면 휴식일로 옮긴 루틴을 되돌릴 방법이 사라진다.
    DAY_LABELS.indices.filter { it != dayIndex }.forEach { otherDayIndex ->
      val routine = routineOf(otherDayIndex, order)
      content.addView(
        TextView(activity).apply {
          text = "${DAY_LABELS[otherDayIndex]} · ${routine?.category ?: "휴식"}"
          setTextColor(Palette.TEXT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
          setPadding(activity.dp(4), activity.dp(14), activity.dp(4), activity.dp(14))
          setOnClickListener {
            dialog.dismiss()
            onPicked(otherDayIndex)
          }
        },
        LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(4) },
      )
    }
    content.addView(
      activity.flatButton("닫기") { dialog.dismiss() },
      LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(12) },
    )

    dialog.show()
  }
}
