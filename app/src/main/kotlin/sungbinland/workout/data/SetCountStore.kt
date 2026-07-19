package sungbinland.workout.data

import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.time.LocalDate

// 오늘 수행 세트 수를 SharedPreferences(로컬)에 저장. 단일 키 덮어쓰기, 날짜가 바뀌면 초기화.
internal class SetCountStore(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val countSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(readTodayCount())

  val todaySetCount: Observable<Int> = countSubject

  fun recordCompletedSet() {
    val today = LocalDate.now().toString()
    val next = if (prefs.getString(KEY_DATE, "") == today) prefs.getInt(KEY_COUNT, 0) + 1 else 1
    prefs.edit().putString(KEY_DATE, today).putInt(KEY_COUNT, next).apply()
    countSubject.onNext(next)
  }

  private fun readTodayCount(): Int {
    val today = LocalDate.now().toString()
    return if (prefs.getString(KEY_DATE, "") == today) prefs.getInt(KEY_COUNT, 0) else 0
  }

  private companion object {
    private const val PREFS_NAME = "workout"
    private const val KEY_DATE = "set_date"
    private const val KEY_COUNT = "set_count"
  }
}
