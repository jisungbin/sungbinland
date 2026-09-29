package sungbinland.workout.ui

import android.app.Activity
import android.app.Dialog
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/*
 * 자유 루틴의 한 자리에 종목 이름을 직접 입력하는 다이얼로그.
 * 한 글자부터 자동 완성 팝업을 띄우고, 팝업에서 고르면 입력칸이 그 이름으로 채워진다(확정은 확인 버튼으로).
 */
internal object ExerciseInputDialog {
  fun show(
    activity: Activity,
    title: String,
    current: String,
    suggestions: List<String>,
    onSubmitted: (String) -> Unit,
  ) {
    val content = LinearLayout(activity).apply {
      orientation = LinearLayout.VERTICAL
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER, radiusDp = 16, strokeDp = 1)
      setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(16))
    }
    content.addView(
      TextView(activity).apply {
        text = "$title 입력"
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      },
    )
    content.addView(
      TextView(activity).apply {
        text = "이름을 입력하면 추천 종목이 뜹니다. 목록에 없는 종목도 그대로 쓸 수 있어요."
        setTextColor(Palette.MUTED)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setPadding(0, activity.dp(4), 0, 0)
      },
    )

    val input = AutoCompleteTextView(activity).apply {
      inputType = InputType.TYPE_CLASS_TEXT
      imeOptions = EditorInfo.IME_ACTION_DONE
      isSingleLine = true
      threshold = 1
      hint = "예: 렛풀다운"
      setHintTextColor(Palette.BORDER)
      setTextColor(Palette.TEXT)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
      background = activity.borderedBox(Palette.SURFACE, Palette.BORDER)
      setPadding(activity.dp(16), activity.dp(14), activity.dp(16), activity.dp(14))
      setAdapter(SuggestionAdapter(activity, suggestions))
      setDropDownBackgroundDrawable(activity.borderedBox(Palette.SURFACE, Palette.BORDER, strokeDp = 1))
      dropDownVerticalOffset = activity.dp(6)
      setText(current)
      setSelection(length())
    }
    content.addView(input, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = activity.dp(16) })

    val dialog = Dialog(activity).apply {
      setContentView(content, FrameLayout.LayoutParams(activity.dp(320), WRAP_CONTENT))
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      // 위쪽에 두어 키보드와 자동 완성 팝업이 들어갈 자리를 남긴다.
      window?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
      window?.let { it.attributes = it.attributes.apply { y = activity.dp(72) } }
      window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    val confirm = activity.flatButton("확인") {
      val name = input.text.toString().trim()
      if (name.isNotEmpty()) {
        dialog.dismiss()
        onSubmitted(name)
      }
    }
    fun syncConfirm() {
      val enabled = input.text.isNotBlank()
      confirm.isEnabled = enabled
      confirm.alpha = if (enabled) 1f else 0.4f
    }
    syncConfirm()
    input.addTextChangedListener(
      object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = Unit

        override fun afterTextChanged(s: Editable) {
          syncConfirm()
        }
      },
    )
    input.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        confirm.performClick()
        true
      } else {
        false
      }
    }

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

  /*
   * 기본 ArrayAdapter는 단어 앞부분만 맞춰 "풀다운"으로 "렛풀다운"을 못 찾는다.
   * 공백을 무시한 부분 일치로 거르고, 입력으로 시작하는 이름을 앞에 둔다.
   */
  private class SuggestionAdapter(
    private val activity: Activity,
    private val all: List<String>,
  ) : BaseAdapter(), Filterable {
    private var shown: List<String> = all

    override fun getCount(): Int = shown.size

    override fun getItem(position: Int): String = shown[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      val view = convertView as? TextView ?: TextView(activity).apply {
        setTextColor(Palette.TEXT)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setPadding(activity.dp(16), activity.dp(12), activity.dp(16), activity.dp(12))
      }
      view.text = shown[position]
      return view
    }

    override fun getFilter(): Filter = object : Filter() {
      override fun performFiltering(constraint: CharSequence?): FilterResults {
        val query = normalize(constraint?.toString().orEmpty())
        val matched = all
          .filter { normalize(it).contains(query) }
          .sortedBy { !normalize(it).startsWith(query) }
        return FilterResults().apply {
          values = matched
          count = matched.size
        }
      }

      override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        shown = (results.values as? List<*>)?.filterIsInstance<String>().orEmpty()
        if (shown.isEmpty()) notifyDataSetInvalidated() else notifyDataSetChanged()
      }
    }

    private fun normalize(text: String): String = text.replace(" ", "").lowercase()
  }
}
