package sungbinland.workout.data

import android.content.Context
import sungbinland.workout.domain.DEFAULT_WEEK_ORDER
import sungbinland.workout.domain.currentWeekIndex

// 이번 주에만 유효한 요일 배치. 저장된 주차가 지나면 기본 배치로 되돌아간다.
internal class WeekOrderStore(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  val order: List<Int> = read()

  // 두 요일 자리의 대분류를 맞바꾼다. 같은 요일을 다시 고르면 자기역원이라 원래대로 돌아간다.
  fun swap(dayIndex: Int, otherDayIndex: Int) {
    val next = order.toMutableList()
    next[dayIndex] = order[otherDayIndex]
    next[otherDayIndex] = order[dayIndex]
    prefs.edit()
      .putInt(KEY_WEEK, currentWeekIndex())
      .putString(KEY_ORDER, next.joinToString(","))
      .apply()
  }

  private fun read(): List<Int> {
    if (prefs.getInt(KEY_WEEK, NO_WEEK) != currentWeekIndex()) return DEFAULT_WEEK_ORDER
    val stored = prefs.getString(KEY_ORDER, "").orEmpty()
      .split(",")
      .mapNotNull { it.toIntOrNull() }
    // 순열이 깨져 있으면(중복·범위 이탈) 배치를 신뢰할 수 없으므로 기본으로 되돌린다.
    return if (stored.sorted() == DEFAULT_WEEK_ORDER) stored else DEFAULT_WEEK_ORDER
  }

  private companion object {
    private const val PREFS_NAME = "workout"
    private const val KEY_WEEK = "week_order_week"
    private const val KEY_ORDER = "week_order"
    private const val NO_WEEK = Int.MIN_VALUE
  }
}
