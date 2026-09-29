package sungbinland.workout.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

/*
 * 오늘 운동 시작 시각을 HH:MM으로 직접 고치는 다이얼로그.
 * 숫자 키패드만 띄우고 두 자리를 치면 콜론을 자동으로 넣는다 — 키패드에서 ':'를 찾아 누를 필요가 없다.
 * 시각으로 읽을 수 없는 값이면 확인 버튼만 비활성화한다.
 */
internal object StartTimeDialog {
  fun show(activity: Activity, currentEpochMillis: Long, onPicked: (Long) -> Unit) {
    val zone = ZoneId.systemDefault()
    val initial = if (currentEpochMillis == 0L) LocalDateTime.now() else
      LocalDateTime.ofInstant(Instant.ofEpochMilli(currentEpochMillis), zone)

    val content = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER, radiusDp = 16, strokeDp = 1)
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(16))
    }
    content.addView(
      TextView(activity).apply {
        text = "운동 시작 시각"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      },
    )
    content.addView(
      TextView(activity).apply {
        text = "24시간 형식으로 입력하세요. 경과 시간이 이 시각부터 다시 계산됩니다."
        setTextColor(Palette.MUTED)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setPadding(0, activity.dp(4), 0, 0)
      },
    )

    val dialog = Dialog(activity).apply {
      setContentView(content, FrameLayout.LayoutParams(activity.dp(320), WRAP_CONTENT))
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setGravity(Gravity.CENTER)
      // 열자마자 바로 입력할 수 있게 키패드를 띄운다.
      window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    val input = EditText(activity).apply {
      inputType = InputType.TYPE_CLASS_NUMBER
      imeOptions = EditorInfo.IME_ACTION_DONE
      filters = arrayOf(InputFilter.LengthFilter(5))
      hint = "HH:MM"
      setHintTextColor(Palette.BORDER)
      setTextColor(Palette.TEXT)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      letterSpacing = 0.08f
      gravity = Gravity.CENTER
      isSingleLine = true
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER)
      setPadding(activity.dp(16), activity.dp(14), activity.dp(16), activity.dp(14))
    }

    fun submit() {
      val epochMillis = parse(input.text.toString(), zone) ?: return
      dialog.dismiss()
      onPicked(epochMillis)
    }
    val confirm = activity.flatButton("확인") { submit() }
    fun syncConfirm() {
      val enabled = parse(input.text.toString(), zone) != null
      confirm.isEnabled = enabled
      confirm.alpha = if (enabled) 1f else 0.4f
    }

    input.setText(format(initial))
    input.selectAll()
    input.addTextChangedListener(ColonFormatter { syncConfirm() })
    syncConfirm()
    input.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        submit()
        true
      } else {
        false
      }
    }
    content.addView(input, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(16) })

    val buttons = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
    buttons.addView(
      activity.flatButton("취소") { dialog.dismiss() },
      LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f),
    )
    buttons.addView(confirm, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply { leftMargin = activity.dp(8) })
    content.addView(buttons, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(16) })

    dialog.show()
    input.requestFocus()
  }

  // 네 자리 HH:MM(00:00~23:59)만 받아 오늘 그 시각의 epoch millis로 바꾼다. 아니면 null.
  private fun parse(text: String, zone: ZoneId): Long? {
    val digits = text.filter(Char::isDigit)
    if (digits.length != 4) return null
    val hour = digits.take(2).toInt()
    val minute = digits.drop(2).toInt()
    if (hour > 23 || minute > 59) return null
    return LocalDate.now().atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
  }

  private fun format(time: LocalDateTime): String =
    String.format(Locale.KOREA, "%02d:%02d", time.hour, time.minute)

  // 숫자만 남기고 두 자리 뒤에 ':'를 끼운다. 지울 때도 같은 규칙이라 콜론이 따로 남지 않는다.
  private class ColonFormatter(private val onEdited: () -> Unit) : TextWatcher {
    private var formatting = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = Unit

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = Unit

    override fun afterTextChanged(s: Editable) {
      if (formatting) return
      val digits = s.filter(Char::isDigit).take(4).toString()
      val formatted = if (digits.length > 2) "${digits.take(2)}:${digits.drop(2)}" else digits
      if (formatted != s.toString()) {
        formatting = true
        s.replace(0, s.length, formatted)
        formatting = false
      }
      onEdited()
    }
  }
}
