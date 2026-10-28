package sungbinland.workout.domain

import java.time.LocalDate

// 한 부위의 종목 후보. 첫 슬롯 외에는 부위별로 합쳐 정해진 개수만큼 선택한다.
internal class ExerciseSlot(
  val part: String,
  val pool: List<String>,
  val setsPerExercise: Int,
) {
  // 정렬이 종목 속성에 기대므로 분류가 빠진 종목은 앱을 띄우는 순간 드러나게 한다.
  init {
    require(pool.all { it in TRAITS }) { "관절·기구 분류가 없는 종목: ${pool.filterNot { it in TRAITS }}" }
  }
}

internal class RoutineDay(
  val day: String,
  val category: String,
  val slots: List<ExerciseSlot>,
)

internal class RoutineExercise(
  val part: String,
  val name: String,
  val targetSets: Int,
  // exerciseOptions()·ExercisePickStore가 쓰는 자리 번호. 화면 순서는 정렬을 거치므로 이 값과 다를 수 있다.
  val slotPosition: Int,
) {
  // 주차마다 종목 구성이 바뀌므로 진행 상황은 순서(index)가 아니라 이 키로 저장한다.
  val key: String get() = "$part:$name"
}

// 요일 루틴·자유 루틴 공통 볼륨: 종목당 4세트, 하루 5종목(가슴·등은 풀업, 나머지는 복직근 포함) = 20세트.
internal const val SETS_PER_EXERCISE: Int = 4
internal const val EXERCISES_PER_DAY: Int = 5

private const val ABS_PART = "복직근"

// 선언 순서가 곧 수행 순서다.
private enum class Joint { COMPOUND, ISOLATION }

private enum class Equipment { FREE_WEIGHT, MACHINE, CABLE }

private class Traits(val joint: Joint, val equipment: Equipment)

/*
 * 종목별 관절·기구 분류. 맨몸 운동(풀업·레그 레이즈)은 프리웨이트로 친다.
 * 렛풀다운 계열과 롱 풀은 전용 스테이션이라 머신으로 분류했다.
 */
private val TRAITS: Map<String, Traits> = buildMap {
  fun add(joint: Joint, equipment: Equipment, vararg names: String) {
    names.forEach { put(it, Traits(joint, equipment)) }
  }
  add(
    Joint.COMPOUND, Equipment.FREE_WEIGHT,
    "풀업", "바벨 로우", "티바 로우", "루마니안 데드리프트",
  )
  add(
    Joint.COMPOUND, Equipment.MACHINE,
    "시티드 체스트 프레스", "라잉 체스트 프레스", "라잉 컨버징 체스트 프레스", "이너 체스트 프레스",
    "벤치 프레스", "인클라인 체스트 프레스", "시티드 딥스",
    "렛풀다운", "서큘러 렛풀다운", "롱 풀", "시티드 로우", "수평 로우", "로우 로우", "하이 로우",
    "파워 레그 프레스", "핵 스쿼트", "스쿼트 프레스", "슈퍼 스쿼트",
    "숄더 프레스", "컨버징 숄더 프레스", "비하인드 숄더 프레스",
    "트라이셉스 프레스",
  )
  add(
    Joint.ISOLATION, Equipment.FREE_WEIGHT,
    "레그 레이즈", "사이드 레터럴 레이즈", "벤트 오버 레이즈", "이두 드래그 컬", "이두 헤머컬", "이두 이지바 컬",
  )
  add(
    Joint.ISOLATION, Equipment.MACHINE,
    "팩댁 플라이", "풀오버", "레그 익스텐션", "레그 컬", "리버스 팩댁 플라이", "이두 크리처 컬",
  )
  add(
    Joint.ISOLATION, Equipment.CABLE,
    "암풀다운", "트라이셉스 푸시다운", "오버헤드 트라이셉스 익스텐션",
  )
}

// 자유 루틴 종목 입력의 자동 완성 후보.
internal val KNOWN_EXERCISES: List<String> = TRAITS.keys.sorted()

/*
 * 기본 우선순위: 복직근 → 다관절 → 단일관절, 같은 관절 안에서는 프리웨이트 → 머신 → 케이블.
 * 종목 속성으로 먼저 정렬한 뒤, 같은 부위를 첫 등장 위치에 모아 연속해서 수행한다.
 * 정렬은 안정적이라 속성이 같은 종목끼리는 슬롯 순서를 따른다.
 */
private val EXERCISE_ORDER: Comparator<RoutineExercise> = compareBy<RoutineExercise>(
  { it.part != ABS_PART },
  { TRAITS.getValue(it.name).joint },
  { TRAITS.getValue(it.name).equipment },
)

// 어깨는 후면 → 측면 → 전면을 우선하고, 같은 부위 안에서는 기존 종목 정렬을 따른다.
private val SHOULDER_EXERCISE_ORDER: Comparator<RoutineExercise> =
  compareBy<RoutineExercise> {
    when (it.part) {
      ABS_PART -> 0
      "후면삼각근" -> 1
      "측면삼각근" -> 2
      "전면삼각근" -> 3
      else -> 4
    }
  }.then(EXERCISE_ORDER)

/*
 * 가슴·등은 풀업, 나머지 요일은 복직근 4세트로 시작하고, 그 뒤 4종목 × 4세트 = 16세트를 둔다. 수행 순서는 EXERCISE_ORDER가 정한다.
 *
 * 종목 선정은 한 주 안에 견갑의 여섯 방향을 모두 쓰는 쪽을 지향한다(필수는 아님).
 *   거상: 전용 종목 없음 — 숄더 프레스·사이드 레터럴(목)에서 상방회전과 함께 일부 쓰인다.
 *   하강·하방회전: 풀업·렛풀다운·암풀다운·풀오버(수), 딥스(월)
 *   후인: 로우(수), 후면 삼각근(목)
 *   상방회전: 숄더 프레스·사이드 레터럴(목)
 *   전인: 체스트 프레스·팩댁 플라이(월) — 전용 종목이 없어 가장 약하다. 프레스 끝에서 견갑을 밀어내는 것으로 보완한다.
 *
 * 첫 슬롯만 고정하며, 나머지는 부위별 배분을 지켜 매주 4종목을 선택한다.
 */
private fun abs(): ExerciseSlot = ExerciseSlot(ABS_PART, listOf("레그 레이즈"), setsPerExercise = SETS_PER_EXERCISE)

internal val WEEKLY_ROUTINE: List<RoutineDay> = listOf(
  RoutineDay(
    "월", "가슴",
    listOf(
      ExerciseSlot("광배", listOf("풀업"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot(
        "중부가슴",
        listOf(
          "시티드 체스트 프레스",
          "라잉 체스트 프레스",
          "라잉 컨버징 체스트 프레스",
          "이너 체스트 프레스",
          "벤치 프레스",
        ),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot("상부가슴", listOf("인클라인 체스트 프레스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("하부가슴", listOf("시티드 딥스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("중부가슴", listOf("팩댁 플라이"), setsPerExercise = SETS_PER_EXERCISE),
    ),
  ),
  RoutineDay(
    "수", "등",
    listOf(
      ExerciseSlot("광배", listOf("풀업"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot(
        "광배",
        listOf("렛풀다운", "서큘러 렛풀다운", "롱 풀"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot(
        "중부등",
        listOf("시티드 로우", "수평 로우", "로우 로우", "하이 로우", "티바 로우", "바벨 로우"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot("광배", listOf("암풀다운", "풀오버"), setsPerExercise = SETS_PER_EXERCISE),
    ),
  ),
  RoutineDay(
    "화", "다리",
    listOf(
      abs(),
      ExerciseSlot(
        "대퇴사두",
        listOf("파워 레그 프레스", "핵 스쿼트", "스쿼트 프레스", "슈퍼 스쿼트"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot("햄스트링", listOf("루마니안 데드리프트"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("대퇴사두", listOf("레그 익스텐션"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("햄스트링", listOf("레그 컬"), setsPerExercise = SETS_PER_EXERCISE),
    ),
  ),
  RoutineDay(
    "목", "어깨",
    listOf(
      abs(),
      ExerciseSlot("전면삼각근", listOf("숄더 프레스", "컨버징 숄더 프레스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("측면삼각근", listOf("비하인드 숄더 프레스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("측면삼각근", listOf("사이드 레터럴 레이즈"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("후면삼각근", listOf("벤트 오버 레이즈", "리버스 팩댁 플라이"), setsPerExercise = SETS_PER_EXERCISE),
    ),
  ),
  RoutineDay(
    "금", "팔",
    listOf(
      abs(),
      ExerciseSlot("삼두", listOf("트라이셉스 프레스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot(
        "삼두",
        listOf("트라이셉스 푸시다운", "오버헤드 트라이셉스 익스텐션"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot(
        "이두",
        listOf("이두 크리처 컬", "이두 드래그 컬", "이두 헤머컬", "이두 이지바 컬"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
    ),
  ),
)

// 대분류가 놓이지 않은 요일 자리. 그 날은 휴식일이 된다.
internal const val NO_ROUTINE: Int = -1

internal val DAY_LABELS: List<String> = listOf("월", "화", "수", "목", "금", "토", "일")

internal fun todayDayIndex(date: LocalDate = LocalDate.now()): Int = date.dayOfWeek.value - 1

/*
 * 요일 자리마다 WEEKLY_ROUTINE의 몇 번째 대분류를 놓을지. 기본은 가슴 → 다리 → 등 → 어깨 → 팔, 주말은 휴식.
 * 주말도 자리를 차지해야 휴식일에서도 평일 루틴을 끌어와 교환할 수 있다 — 교환은 이 리스트의 순열이므로
 * 주말이 목록에서 빠져 있으면 애초에 교환 대상이 되지 못한다.
 */
internal val DEFAULT_WEEK_ORDER: List<Int> = listOf(0, 2, 1, 3, 4, NO_ROUTINE, NO_ROUTINE)

/*
 * 요일 배치는 order로만 표현하고 WEEKLY_ROUTINE 자체는 절대 재정렬하지 않는다.
 * 종목은 각 루틴의 후보와 주차로 정하므로, order만 교환하면 원래 종목 구성을 그대로 들고 이동한다.
 */
internal fun routineOf(dayIndex: Int, order: List<Int> = DEFAULT_WEEK_ORDER): RoutineDay? =
  WEEKLY_ROUTINE.getOrNull(order.getOrElse(dayIndex) { NO_ROUTINE })

internal fun currentWeekIndex(date: LocalDate = LocalDate.now()): Int = weekIndex(date)

/*
 * 고정 첫 종목 외 네 자리는 세부 부위별 배분을 유지한다.
 * 후보는 같은 부위끼리 합치고, 주차와 해당 부위 안의 자리 번호로 중복 없이 순환한다.
 * 수동 변경도 같은 부위 후보로 제한해 부위가 빠지거나 배분이 달라지지 않게 한다.
 */
private fun RoutineDay.rotatingParts(): List<String> = when (category) {
  "가슴" -> listOf("중부가슴", "중부가슴", "상부가슴", "하부가슴")
  "등" -> listOf("광배", "광배", "중부등", "중부등")
  "다리" -> listOf("대퇴사두", "대퇴사두", "햄스트링", "햄스트링")
  "어깨" -> listOf("후면삼각근", "측면삼각근", "측면삼각근", "전면삼각근")
  "팔" -> listOf("삼두", "삼두", "이두", "이두")
  else -> error("부위 배분이 없는 루틴: $category")
}

private fun RoutineDay.poolFor(part: String): List<String> =
  slots.drop(1).filter { it.part == part }.flatMap { it.pool }.distinct()

internal fun RoutineDay.exercises(
  date: LocalDate = LocalDate.now(),
  picks: Map<Int, String> = emptyMap(),
): List<RoutineExercise> {
  val fixed = slots.first()
  val used = mutableSetOf(fixed.pool.single())
  val occurrences = mutableMapOf<String, Int>()
  val exercises = mutableListOf(
    RoutineExercise(fixed.part, fixed.pool.single(), fixed.setsPerExercise, 0),
  )
  val parts = rotatingParts()
  require(parts.size == EXERCISES_PER_DAY - 1)
  for ((index, part) in parts.withIndex()) {
    val pool = poolFor(part)
    require(pool.size >= parts.count { it == part }) { "$part 종목 후보가 부족합니다" }
    val occurrence = occurrences.getOrDefault(part, 0)
    occurrences[part] = occurrence + 1
    val start = (weekIndex(date) + occurrence).mod(pool.size)
    val position = index + 1
    val preferred = picks[position]?.takeIf { it in pool && it !in used }
    val selected = preferred ?: pool.indices
      .map { pool[(start + it).mod(pool.size)] }
      .first { it !in used }
    used.add(selected)
    exercises.add(RoutineExercise(part, selected, SETS_PER_EXERCISE, position))
  }
  return listOf(exercises.first()) + exercises.drop(1)
    .sortedWith(if (category == "어깨") SHOULDER_EXERCISE_ORDER else EXERCISE_ORDER)
    .groupBy { it.part }
    .values
    .flatten()
}

// 첫 자리는 고정하고, 나머지는 각 자리에 배정된 부위 안에서만 변경한다.
internal fun RoutineDay.exerciseOptions(): List<List<String>> =
  listOf(slots.first().pool) + rotatingParts().map { poolFor(it) }

/*
 * 자유 루틴: 요일 루틴과 같은 볼륨(EXERCISES_PER_DAY종목 × SETS_PER_EXERCISE세트)에 종목만 사용자가 채운다.
 * 사용자가 직접 짠 순서를 그대로 따르므로 정렬하지 않는다. 아직 입력하지 않은 자리는 이름이 빈 문자열이다.
 * 자리마다 부위 이름을 달리 둬 같은 종목을 두 자리에 넣어도 세트 저장 키가 겹치지 않는다.
 */
internal fun freeRoutineExercises(names: List<String>): List<RoutineExercise> =
  List(EXERCISES_PER_DAY) { position ->
    RoutineExercise(
      part = "종목 ${position + 1}",
      name = names.getOrElse(position) { "" },
      targetSets = SETS_PER_EXERCISE,
      slotPosition = position,
    )
  }

// 1970-01-01이 목요일이므로 +3 보정해 월요일마다 주차가 올라가게 한다.
private fun weekIndex(date: LocalDate): Int = ((date.toEpochDay() + 3) / 7).toInt()
