package sungbinland.workout.data

import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.time.LocalDate
import sungbinland.workout.domain.todayRoutine

// 오늘 루틴의 항목별 수행 세트 수 + 첫 세트 완료 시각을 SharedPreferences(로컬)에 저장. 날짜가 바뀌면 초기화.
internal class SetCountStore(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val itemCount = todayRoutine()?.items?.size ?: 0

  private val countsSubject: BehaviorSubject<List<Int>> = BehaviorSubject.createDefault(readTodayCounts())
  private val firstSetSubject: BehaviorSubject<Long> = BehaviorSubject.createDefault(readFirstSetEpochMillis())

  val todayItemCounts: Observable<List<Int>> = countsSubject

  // 0L이면 오늘 아직 세트를 하나도 완료하지 않은 상태.
  val firstSetEpochMillis: Observable<Long> = firstSetSubject

  // 항목을 순서대로 채워나간다: 아직 목표치를 못 채운 첫 항목에 +1.
  fun recordCompletedSet() {
    val targets = todayRoutine()?.items?.map { it.targetSets } ?: return
    resetIfNewDay()

    val counts = readCountsList().toMutableList()
    val index = counts.indices.firstOrNull { counts[it] < targets[it] } ?: return
    counts[index] = counts[index] + 1
    prefs.edit().putString(KEY_COUNTS, counts.joinToString(",")).apply()
    countsSubject.onNext(counts)

    if (prefs.getLong(KEY_FIRST_SET_EPOCH, 0L) == 0L) {
      val now = System.currentTimeMillis()
      prefs.edit().putLong(KEY_FIRST_SET_EPOCH, now).apply()
      firstSetSubject.onNext(now)
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

  private fun readCountsList(): List<Int> {
    val raw = prefs.getString(KEY_COUNTS, "").orEmpty()
    val stored = if (raw.isEmpty()) emptyList() else raw.split(",").map { it.toInt() }
    return List(itemCount) { stored.getOrElse(it) { 0 } }
  }

  private fun readFirstSetEpochMillis(): Long {
    resetIfNewDay()
    return prefs.getLong(KEY_FIRST_SET_EPOCH, 0L)
  }

  private companion object {
    private const val PREFS_NAME = "workout"
    private const val KEY_DATE = "set_date"
    private const val KEY_COUNTS = "item_counts"
    private const val KEY_FIRST_SET_EPOCH = "first_set_epoch"
  }
}
