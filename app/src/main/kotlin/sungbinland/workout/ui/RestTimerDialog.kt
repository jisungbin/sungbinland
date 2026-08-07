package sungbinland.workout.ui

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale
import sungbinland.workout.domain.RestTimer

// 완료 시에만 닫히며 onCompleted로 완료 이펙트를 위임한다.
internal object RestTimerDialog {
  fun show(activity: Activity, onCompleted: () -> Unit) {
    val timer = RestTimer()
    timer.start()

    val content = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      gravity = Gravity.CENTER_HORIZONTAL
      background = activity.borderedBox(Palette.SURFACE, Palette.ACCENT)
      setPadding(activity.dp(24), activity.dp(24), activity.dp(24), activity.dp(24))
    }
    content.addView(
      TextView(activity).apply {
        text = "휴식 타이머"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
      },
      LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT),
    )
    val timerText = TextView(activity).apply {
      setTextColor(Palette.TEXT)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 56f)
      setPadding(0, activity.dp(16), 0, activity.dp(16))
    }
    content.addView(timerText)
    content.addView(
      activity.flatButton("+10초") { timer.extend(10_000L) },
      LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT),
    )

    val dialog = Dialog(activity).apply {
      setContentView(content, FrameLayout.LayoutParams(activity.dp(300), WRAP_CONTENT))
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setGravity(Gravity.BOTTOM)
      // 뒤 화면 어둡게(dim) 끄기 → 콘페티가 그대로 보이도록.
      window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
      // 바깥 터치·뒤로가기로 닫히지 않음. 타이머 완료 시에만 닫힌다.
      setCancelable(false)
      setCanceledOnTouchOutside(false)
    }

    val handler = Handler(Looper.getMainLooper())
    var completed = false
    val ticker = object : Runnable {
      override fun run() {
        val remaining = timer.remainingMillis() / 1000
        timerText.text = String.format(Locale.KOREA, "%02d:%02d", remaining / 60, remaining % 60)
        if (!completed && timer.isFinished()) {
          completed = true
          timer.stop()
          onCompleted()
          // 완료 시에만 닫는다(00:00을 잠깐 보여준 뒤).
          handler.postDelayed({ if (dialog.isShowing) dialog.dismiss() }, 500L)
        }
        if (dialog.isShowing) handler.postDelayed(this, 100L)
      }
    }
    dialog.setOnDismissListener {
      handler.removeCallbacks(ticker)
      timer.stop()
    }
    dialog.show()
    handler.post(ticker)
  }
}
