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

// 한 자리의 종목을 같은 부위 풀의 다른 종목으로 바꾸기 위한 선택 다이얼로그.
internal object ExercisePickerDialog {
  fun show(
    activity: Activity,
    part: String,
    current: String,
    options: List<String>,
    onPicked: (String) -> Unit,
  ) {
    val content = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER, radiusDp = 16, strokeDp = 1)
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(12))
    }
    content.addView(
      TextView(activity).apply {
        text = "$part 종목 고르기"
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

    val list = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    options.filter { it != current }.forEach { name ->
      list.addView(
        TextView(activity).apply {
          text = name
          setTextColor(Palette.TEXT)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
          setPadding(activity.dp(4), activity.dp(14), activity.dp(4), activity.dp(14))
          setOnClickListener {
            dialog.dismiss()
            onPicked(name)
          }
        },
        LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(4) },
      )
    }
    // 풀이 큰 부위(6종목)는 목록이 길어 닫기 버튼을 화면 밖으로 밀어낼 수 있다.
    content.addView(
      ScrollView(activity).apply { addView(list, MATCH_PARENT, WRAP_CONTENT) },
      LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f),
    )

    content.addView(
      activity.flatButton("닫기") { dialog.dismiss() },
      LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(12) },
    )

    dialog.show()
  }
}
