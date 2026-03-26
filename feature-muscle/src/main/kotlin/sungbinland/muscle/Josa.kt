package sungbinland.muscle

/**
 * 마지막 글자의 받침 유무에 따라 "은"/"는"을 반환한다.
 * 한글 음절이 아닌 문자로 끝나면 "은(는)"을 반환한다.
 */
internal fun String.eunNeun(): String {
  val last = lastOrNull() ?: return "은(는)"
  if (last !in '\uAC00'..'\uD7AF') return "은(는)"
  val jongsung = (last.code - 0xAC00) % 28
  return if (jongsung > 0) "은" else "는"
}
