package sungbinland.workout.data

import android.content.Context
import sungbinland.workout.domain.currentWeekIndex

/*
 * 사용자가 직접 고른 종목. 이번 주에만 유효하고 저장된 주차가 지나면 전부 버린다.
 *
 * 자리는 요일이 아니라 대분류(routineIndex) 기준으로 붙든다 — 루틴을 다른 요일과 교환해도
 * 그 루틴을 따라 이동해야 "가슴 루틴의 이 종목을 바꿨다"는 사용자의 의도가 유지된다.
 */
internal class ExercisePickStore(context: Context, private val routineIndex: Int) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  // exercises() 순서의 자리 인덱스 → 고른 종목 이름.
  val picks: Map<Int, String> =
    readAll().filterKeys { it.first == routineIndex }.mapKeys { (key, _) -> key.second }

  fun pick(position: Int, name: String) {
    val next = readAll().toMutableMap()
    next[routineIndex to position] = name
    prefs.edit()
      .putInt(KEY_WEEK, currentWeekIndex())
      .putString(KEY_PICKS, encode(next))
      .apply()
  }

  private fun readAll(): Map<Pair<Int, Int>, String> {
    if (prefs.getInt(KEY_WEEK, NO_WEEK) != currentWeekIndex()) return emptyMap()
    return prefs.getString(KEY_PICKS, "").orEmpty()
      .split(ENTRY_DELIMITER)
      .mapNotNull { entry ->
        val at = entry.indexOf(NAME_DELIMITER)
        if (at < 0) return@mapNotNull null
        val slot = entry.substring(0, at).split(SLOT_DELIMITER)
        val routine = slot.getOrNull(0)?.toIntOrNull()
        val position = slot.getOrNull(1)?.toIntOrNull()
        val name = entry.substring(at + 1)
        if (routine == null || position == null || name.isEmpty()) null else (routine to position) to name
      }
      .toMap()
  }

  private fun encode(picks: Map<Pair<Int, Int>, String>): String =
    picks.entries.joinToString(ENTRY_DELIMITER) { (slot, name) ->
      "${slot.first}$SLOT_DELIMITER${slot.second}$NAME_DELIMITER$name"
    }

  private companion object {
    private const val PREFS_NAME = "workout"
    private const val KEY_WEEK = "exercise_picks_week"
    private const val KEY_PICKS = "exercise_picks"
    private const val NO_WEEK = Int.MIN_VALUE

    private const val ENTRY_DELIMITER = "|"
    private const val SLOT_DELIMITER = ":"
    private const val NAME_DELIMITER = "="
  }
}
