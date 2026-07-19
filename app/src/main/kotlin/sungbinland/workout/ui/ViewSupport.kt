package sungbinland.workout.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.Button

internal fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

internal fun Context.borderedBox(fill: Int, stroke: Int): GradientDrawable = GradientDrawable().apply {
  setColor(fill)
  setStroke(dp(2), stroke)
}

internal fun Context.flatButton(label: String, onClick: () -> Unit): Button =
  Button(this).apply {
    text = label
    isAllCaps = false
    setTextColor(Palette.ACCENT)
    background = borderedBox(Palette.HEADER, Palette.BORDER)
    setPadding(dp(14), dp(8), dp(14), dp(8))
    setOnClickListener { onClick() }
  }
