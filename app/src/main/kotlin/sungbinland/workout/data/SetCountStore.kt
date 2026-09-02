package sungbinland.workout.data

import android.content.Context
import java.time.LocalDate
import sungbinland.workout.domain.RoutineExercise
import sungbinland.workout.event.EventBus
import sungbinland.workout.event.FirstSetChanged
import sungbinland.workout.event.SetCountsChanged

// 오늘 루틴의 종목별 수행 세트 수 + 첫 세트 완료 시각을 SharedPreferences(로컬)에 저장. 날짜가 바뀌면 초기화.
internal class SetCountStore(
  context: Context,
  val todayExercises: List<RoutineExercise>,
) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  init {
    EventBus.post(SetCountsChanged(readTodayCounts()))
    EventBus.post(FirstSetChanged(readFirstSetEpochMillis()))
  }

  // 종목을 순서대로 채워나간다: 아직 목표치를 못 채운 첫 종목에 +1. 웜업이 목록 맨 앞이라 웜업부터 채워진다.
  fun recordCompletedSet() {
    if (todayExercises.isEmpty()) return
    resetIfNewDay()

    val counts = readCountsList().toMutableList()
    val index = counts.indices.firstOrNull { counts[it] < todayExercises[it].targetSets } ?: return
    counts[index] = counts[index] + 1
    prefs.edit().putString(KEY_COUNTS, encodeCounts(counts)).apply()
    EventBus.post(SetCountsChanged(counts))

    if (prefs.getLong(KEY_FIRST_SET_EPOCH, 0L) == 0L) {
      val now = System.currentTimeMillis()
      prefs.edit().putLong(KEY_FIRST_SET_EPOCH, now).apply()
      EventBus.post(FirstSetChanged(now))
    }
  }

  private fun resetIfNewDay() {
    val today = LocalDate.now().toString()
    if (prefs.getString(KEY_DATE, "") != today) {
      prefs.edit()
        .putString(KEY_DATE, today)
        .putString(KEY_COUNTS, "")
        .putLong(KEY_FIRST_SET_EPOCH, 0L)
        .apply()
    }
  }

  private fun readTodayCounts(): List<Int> {
    resetIfNewDay()
    return readCountsList()
  }

  private fun encodeCounts(counts: List<Int>): String =
    todayExercises.indices.joinToString(ENTRY_DELIMITER) { index ->
      "${todayExercises[index].key}$COUNT_DELIMITER${counts.getOrElse(index) { 0 }}"
    }

  // 종목 순서가 아니라 종목 키로 복원한다 — 주차가 넘어가 종목 구성이 바뀌어도 남의 진행도를 물려받지 않는다.
  private fun readCountsList(): List<Int> {
    val stored = prefs.getString(KEY_COUNTS, "").orEmpty()
      .split(ENTRY_DELIMITER)
      .mapNotNull { entry ->
        val at = entry.lastIndexOf(COUNT_DELIMITER)
        val count = if (at < 0) null else entry.substring(at + 1).toIntOrNull()
        if (count == null) null else entry.substring(0, at) to count
      }
      .toMap()
    return todayExercises.map { stored[it.key] ?: 0 }
  }

  private fun readFirstSetEpochMillis(): Long {
    resetIfNewDay()
    return prefs.getLong(KEY_FIRST_SET_EPOCH, 0L)
  }

  private companion object {
    private const val PREFS_NAME = "workout"
    private const val KEY_DATE = "set_date"
    private const val KEY_COUNTS = "exercise_counts"
    private const val KEY_FIRST_SET_EPOCH = "first_set_epoch"

    private const val ENTRY_DELIMITER = "|"
    private const val COUNT_DELIMITER = "="
  }
}
