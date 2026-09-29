package sungbinland.workout.data

import android.content.Context
import java.time.LocalDate
import org.json.JSONArray

/*
 * 자유 루틴 상태. 오늘 날짜에만 유효하다 — 세트 집계(SetCountStore)가 날짜 단위로 초기화되므로
 * 자유 루틴도 같은 경계에서 요일 루틴으로 돌아가야 진행도와 종목 구성이 어긋나지 않는다.
 * 입력했던 종목 이름은 날짜와 무관하게 쌓아 두고 다음 입력의 자동 완성 후보로 쓴다.
 */
internal class FreeRoutineStore(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  val isActive: Boolean = prefs.getString(KEY_DATE, "") == today()

  // 자리 번호 → 입력한 종목 이름. 아직 입력하지 않은 자리는 빈 문자열.
  val names: List<String> = if (isActive) readArray(KEY_NAMES) else emptyList()

  val history: List<String> = readArray(KEY_HISTORY)

  fun activate() {
    prefs.edit()
      .putString(KEY_DATE, today())
      .putString(KEY_NAMES, JSONArray().toString())
      .apply()
  }

  fun deactivate() {
    prefs.edit().remove(KEY_DATE).remove(KEY_NAMES).apply()
  }

  fun setName(position: Int, name: String) {
    val next = names.toMutableList()
    while (next.size <= position) next += ""
    next[position] = name
    // 최근에 쓴 이름이 앞에 오도록 옮겨 담는다.
    val nextHistory = listOf(name) + history.filter { it != name }
    prefs.edit()
      .putString(KEY_NAMES, JSONArray(next).toString())
      .putString(KEY_HISTORY, JSONArray(nextHistory.take(HISTORY_LIMIT)).toString())
      .apply()
  }

  private fun readArray(key: String): List<String> {
    val raw = prefs.getString(key, null) ?: return emptyList()
    val array = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
    return List(array.length()) { array.optString(it) }
  }

  private fun today(): String = LocalDate.now().toString()

  private companion object {
    private const val PREFS_NAME = "workout"
    private const val KEY_DATE = "free_routine_date"
    private const val KEY_NAMES = "free_routine_names"
    private const val KEY_HISTORY = "free_routine_history"
    private const val HISTORY_LIMIT = 50
  }
}
