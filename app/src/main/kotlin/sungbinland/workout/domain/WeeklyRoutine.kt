package sungbinland.workout.domain

import java.time.LocalDate

// 한 부위에 배정된 종목 풀. 매주 pool에서 pickCount개를 골라 각 setsPerExercise세트씩 수행한다.
internal class ExerciseSlot(
  val part: String,
  val pool: List<String>,
  val pickCount: Int = 1,
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

// 요일 루틴·자유 루틴 공통 볼륨: 종목당 4세트, 하루 5종목(복직근 포함) = 20세트.
internal const val SETS_PER_EXERCISE: Int = 4
internal const val EXERCISES_PER_DAY: Int = 5

private const val ABS_PART = "복직근"

// 선언 순서가 곧 수행 순서다.
private enum class Joint { COMPOUND, ISOLATION }

private enum class Equipment { FREE_WEIGHT, MACHINE, CABLE }

private class Traits(val joint: Joint, val equipment: Equipment)

/*
 * 종목별 관절·기구 분류. 맨몸 운동(풀업·레그 레이즈)은 프리웨이트로 친다.
 * 스미스 머신은 궤도가 고정돼 머신으로, 렛풀다운 계열과 롱 풀은 전용 스테이션이라 머신으로 분류했다.
 */
private val TRAITS: Map<String, Traits> = buildMap {
  fun add(joint: Joint, equipment: Equipment, vararg names: String) {
    names.forEach { put(it, Traits(joint, equipment)) }
  }
  add(
    Joint.COMPOUND, Equipment.FREE_WEIGHT,
    "풀업", "바벨 로우", "티바 로우", "고블릿 스쿼트", "루마니안 데드리프트",
  )
  add(
    Joint.COMPOUND, Equipment.MACHINE,
    "시티드 체스트 프레스", "라잉 체스트 프레스", "라잉 컨버징 체스트 프레스", "이너 체스트 프레스",
    "스미스 벤치 프레스", "인클라인 체스트 프레스", "시티드 딥스",
    "렛풀다운", "서큘러 렛풀다운", "롱 풀", "시티드 로우", "수평 로우", "로우 로우", "하이 로우",
    "파워 레그 프레스", "핵 스쿼트", "스쿼트 프레스", "슈퍼 스쿼트",
    "숄더 프레스", "컨버징 숄더 프레스", "스미스 비하인드 숄더 프레스",
    "트라이셉스 프레스",
  )
  add(
    Joint.ISOLATION, Equipment.FREE_WEIGHT,
    "레그 레이즈", "사이드 레터럴 레이즈", "벤트 오버 레이즈", "이두 드래그 컬", "이두 헤머컬", "이두 이지바 컬",
  )
  add(
    Joint.ISOLATION, Equipment.MACHINE,
    "팩댁 플라이", "풀오버 머신", "레그 익스텐션", "레그 컬", "리버스 팩댁 플라이", "이두 크리처 컬",
  )
  add(
    Joint.ISOLATION, Equipment.CABLE,
    "암풀다운", "케이블 트라이셉스 푸시다운", "케이블 오버헤드 트라이셉스 익스텐션",
  )
}

// 자유 루틴 종목 입력의 자동 완성 후보.
internal val KNOWN_EXERCISES: List<String> = TRAITS.keys.sorted()

/*
 * 하루 순서: 복직근 → 다관절 → 단일관절, 같은 관절 안에서는 프리웨이트 → 머신 → 케이블.
 * 풀에서 무엇이 뽑히든(혹은 사용자가 무엇으로 바꾸든) 이 순서가 유지되도록 슬롯 순서가 아니라 종목 속성으로 정렬한다.
 * 정렬은 안정적이라 속성이 같은 종목끼리는 슬롯 순서를 따른다.
 */
private val EXERCISE_ORDER: Comparator<RoutineExercise> = compareBy<RoutineExercise>(
  { it.part != ABS_PART },
  { TRAITS.getValue(it.name).joint },
  { TRAITS.getValue(it.name).equipment },
)

/*
 * 요일마다 복직근 4세트로 시작하고, 그 뒤 4종목 × 4세트 = 16세트를 둔다. 수행 순서는 EXERCISE_ORDER가 정한다.
 *
 * 종목 선정은 한 주 안에 견갑의 여섯 방향을 모두 쓰는 쪽을 지향한다(필수는 아님).
 *   거상: 전용 종목 없음 — 숄더 프레스·사이드 레터럴(목)에서 상방회전과 함께 일부 쓰인다.
 *   하강·하방회전: 풀업·렛풀다운·암풀다운·풀오버(화), 딥스(월)
 *   후인: 로우(화), 후면 삼각근(목)
 *   상방회전: 숄더 프레스·사이드 레터럴(목)
 *   전인: 체스트 프레스·팩댁 플라이(월) — 전용 종목이 없어 가장 약하다. 프레스 끝에서 견갑을 밀어내는 것으로 보완한다.
 *
 * 복직근 슬롯은 요일마다 새로 만든다 — PART_OCCURRENCES가 슬롯을 참조 동일성으로 구분하므로 한 인스턴스를 공유하면 등장 순번이 덮어써진다.
 */
private fun abs(): ExerciseSlot = ExerciseSlot(ABS_PART, listOf("레그 레이즈"), setsPerExercise = SETS_PER_EXERCISE)

internal val WEEKLY_ROUTINE: List<RoutineDay> = listOf(
  RoutineDay(
    "월", "가슴",
    listOf(
      abs(),
      ExerciseSlot(
        "중부가슴",
        listOf(
          "시티드 체스트 프레스",
          "라잉 체스트 프레스",
          "라잉 컨버징 체스트 프레스",
          "이너 체스트 프레스",
          "스미스 벤치 프레스",
        ),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot("상부가슴", listOf("인클라인 체스트 프레스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("하부가슴", listOf("시티드 딥스"), setsPerExercise = SETS_PER_EXERCISE),
      ExerciseSlot("중부가슴", listOf("팩댁 플라이"), setsPerExercise = SETS_PER_EXERCISE),
    ),
  ),
  RoutineDay(
    "화", "등",
    listOf(
      abs(),
      ExerciseSlot(
        "광배",
        listOf("풀업", "렛풀다운", "서큘러 렛풀다운", "롱 풀"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot(
        "중부등",
        listOf("시티드 로우", "수평 로우", "로우 로우", "하이 로우", "티바 로우", "바벨 로우"),
        pickCount = 2,
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot("광배", listOf("암풀다운", "풀오버 머신"), setsPerExercise = SETS_PER_EXERCISE),
    ),
  ),
  RoutineDay(
    "수", "다리",
    listOf(
      abs(),
      ExerciseSlot(
        "대퇴사두",
        listOf("파워 레그 프레스", "핵 스쿼트", "스쿼트 프레스", "슈퍼 스쿼트", "고블릿 스쿼트"),
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
      ExerciseSlot("측면삼각근", listOf("스미스 비하인드 숄더 프레스"), setsPerExercise = SETS_PER_EXERCISE),
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
        listOf("케이블 트라이셉스 푸시다운", "케이블 오버헤드 트라이셉스 익스텐션"),
        setsPerExercise = SETS_PER_EXERCISE,
      ),
      ExerciseSlot(
        "이두",
        listOf("이두 크리처 컬", "이두 드래그 컬", "이두 헤머컬", "이두 이지바 컬"),
        pickCount = 2,
        setsPerExercise = SETS_PER_EXERCISE,
      ),
    ),
  ),
)

// 대분류가 놓이지 않은 요일 자리. 그 날은 휴식일이 된다.
internal const val NO_ROUTINE: Int = -1

internal val DAY_LABELS: List<String> = WEEKLY_ROUTINE.map { it.day } + listOf("토", "일")

internal fun todayDayIndex(date: LocalDate = LocalDate.now()): Int = date.dayOfWeek.value - 1

/*
 * 요일 자리마다 WEEKLY_ROUTINE의 몇 번째 대분류를 놓을지. 기본은 월~금 정순 + 주말은 휴식.
 * 주말도 자리를 차지해야 휴식일에서도 평일 루틴을 끌어와 교환할 수 있다 — 교환은 이 리스트의 순열이므로
 * 주말이 목록에서 빠져 있으면 애초에 교환 대상이 되지 못한다.
 */
internal val DEFAULT_WEEK_ORDER: List<Int> = WEEKLY_ROUTINE.indices + listOf(NO_ROUTINE, NO_ROUTINE)

/*
 * 요일 배치는 order로만 표현하고 WEEKLY_ROUTINE 자체는 절대 재정렬하지 않는다.
 * 종목 순환 오프셋(PART_OCCURRENCES)이 이 리스트의 순서에서 계산되므로, 리스트를 뒤섞으면
 * 스왑하지 않은 요일의 종목까지 함께 바뀐다. 반대로 order만 갈아치우면 pick의 입력이 하나도 변하지 않아
 * 교환된 두 날조차 원래 뽑던 종목을 그대로 들고 이동한다.
 */
internal fun routineOf(dayIndex: Int, order: List<Int> = DEFAULT_WEEK_ORDER): RoutineDay? =
  WEEKLY_ROUTINE.getOrNull(order.getOrElse(dayIndex) { NO_ROUTINE })

internal fun currentWeekIndex(date: LocalDate = LocalDate.now()): Int = weekIndex(date)

/*
 * 종목 순환 규칙
 * ============
 *
 * 목표는 두 가지다.
 *   (1) 한 주 동안에는 종목이 절대 바뀌지 않는다 — 앱을 다시 켜도, 세트를 채워도 같은 종목이 뜬다.
 *   (2) 주가 넘어가면 다음 묶음으로 넘어가고, 몇 주 지나면 풀의 모든 종목이 빠짐없이 한 번씩 돌아온다.
 *
 * 그래서 난수를 쓰지 않는다. 난수는 (1)을 만족시키려면 시드를 어딘가에 저장해야 하고,
 * (2)의 "빠짐없이"를 보장하지 못한다(같은 종목이 연달아 뽑히거나 어떤 종목은 몇 달째 안 나옴).
 * 대신 "주차 번호"라는 이미 존재하는 단조 증가 정수를 인덱스로 삼아 풀을 순서대로 훑는다.
 * 저장할 상태가 0이고, 같은 주라면 언제 계산해도 결과가 같은 순수 함수가 된다.
 *
 * ── 1단계: 주차 번호 (weekIndex)
 *
 * epochDay는 1970-01-01부터 며칠 지났는지를 나타내는 정수다. 이걸 7로 나누면 주차가 되지만,
 * 1970-01-01이 *목요일*이라 그냥 나누면 주 경계가 목요일에 생긴다. 그러면 수요일에 운동하고
 * 목요일에 앱을 켰을 때 종목이 통째로 바뀐다 — 주 중간에 루틴이 갈리는 셈이니 곤란하다.
 *
 * +3을 더하면 경계가 월요일로 밀린다. 왜 3인가: 목요일을 월요일 자리로 옮기려면 3일을 앞당겨야 한다.
 *
 *   1970-01-04 (일) epochDay=3  → (3+3)/7  = 0
 *   1970-01-05 (월) epochDay=4  → (4+3)/7  = 1   ← 월요일에 번호가 올라간다
 *   1970-01-11 (일) epochDay=10 → (10+3)/7 = 1   ← 같은 주 내내 유지
 *   1970-01-12 (월) epochDay=11 → (11+3)/7 = 2
 *
 * ── 2단계: 풀에서 무엇을 꺼낼지 (start와 gap)
 *
 *   cursor = weekIndex + partOccurrence
 *   start  = cursor mod pool.size
 *   gap    = 1 + (cursor / pool.size) mod (pool.size - 1)
 *   꺼낼 인덱스 = start, start+gap, start+2*gap … (mod pool.size)
 *
 * 처음에는 start를 매주 pickCount칸씩 밀었다. 그런데 그러면 풀 크기가 pickCount의 배수일 때 조합이 굳는다.
 * 풀 6에서 2개씩 꺼내면 (0,1) (2,3) (4,5) 세 쌍만 3주 주기로 돌고 (0,2)나 (1,4)는 영원히 나오지 않는다.
 * 종목은 다 돌지만 *조합*은 세 가지뿐이라, 늘 같은 짝만 붙어 다니는 게 눈에 띈다.
 *
 * 그래서 start는 매주 1칸씩만 밀고, 풀을 한 바퀴 돌 때마다 간격(gap)을 1씩 올린다. 풀 6 / 2개씩:
 *
 *   0~5주:   gap=1 → (0,1) (1,2) (2,3) (3,4) (4,5) (5,0)
 *   6~11주:  gap=2 → (0,2) (1,3) (2,4) (3,5) (4,0) (5,1)
 *   12~17주: gap=3 → (0,3) (1,4) (2,5) …
 *
 * pool.size × (pool.size-1) 주 만에 제자리로 돌아오며, 그 사이 가능한 모든 조합이 같은 횟수씩 나온다.
 * 풀 크기가 1이면 (pool.size-1)이 0이라 mod가 터지므로 coerceAtLeast(1)로 막는다 — 결과는 어차피 하나뿐이다.
 *
 * ── 3단계: partOccurrence를 왜 더하나
 *
 * 한 주에 같은 부위가 여러 슬롯으로 등장하면 그 슬롯들이 같은 풀을 공유한다. 보정 없이 weekIndex만 쓰면
 * 그 날들에 똑같은 종목이 나오므로, 슬롯마다 커서를 밀어 갈라놓는다.
 *
 * 여기서 "요일 번호"나 "주간 통짜 슬롯 번호" 같은 임의의 정수를 쓰면 함정에 빠진다. 두 슬롯의 번호 차이가
 * pool.size의 배수이면 mod 연산에서 같은 값으로 뭉개지기 때문이다. 요일 번호로 잡으면 풀 2짜리 부위가
 * 월·금(차이 4)에서 겹치고, 슬롯 번호로 바꿔도 다른 요일 조합에서 같은 일이 그대로 재현된다.
 * 번호에 상수를 곱해도(요일×7 등) 차이가 배수라는 사실은 변하지 않아 절대 갈라지지 않는다.
 *
 * 임의 정수로는 못 푼다. 대신 *그 부위가 주간 루틴에서 몇 번째로 등장하는가*(같은 part끼리 0,1,2… 로 새로 셈)를
 * 쓴다. 연속된 정수이므로 등장 횟수가 pool.size 이하인 부위는 mod 후에도 전부 서로 다른 값이 되어 충돌이 불가능하다.
 * 등장 횟수가 pool.size를 넘으면 원리상 겹칠 수밖에 없는데, 이 방식은 그 경우에도 균등히 나눠
 * 각 종목을 정확히 같은 횟수만큼 쓴다.
 *
 * 트레이드오프: 같은 부위 슬롯을 추가·삭제하면 그 부위의 등장 번호가 밀려 해당 주 종목이 한 번 재배치된다.
 * 다른 부위는 영향받지 않고, 커버리지·등장 빈도 균등성도 그대로다.
 *
 * 지금 구성에서 매일 등장하는 부위는 복직근뿐이고 그 풀이 1개라 이 보정은 놀고 있다. 같은 날 두 번 나오는 부위
 * (다관절·단일관절 슬롯)는 서로 다른 풀을 쓰므로 겹칠 일이 없다.
 * 풀이 여럿인 부위를 여러 날에 배치하는 순간 다시 필요해진다.
 */
internal fun RoutineDay.exercises(
  date: LocalDate = LocalDate.now(),
  picks: Map<Int, String> = emptyMap(),
): List<RoutineExercise> {
  val weekIndex = weekIndex(date)
  var position = 0
  return slots
    .flatMap { slot ->
      slot.pick(weekIndex, PART_OCCURRENCES[slot] ?: 0).map { rotated ->
        val slotPosition = position++
        // 풀에 없는 이름은 버린다 — 종목 구성을 고친 뒤 남아 있던 저장값이 화면에 새는 걸 막는다.
        val name = picks[slotPosition]?.takeIf { it in slot.pool } ?: rotated
        RoutineExercise(slot.part, name, slot.setsPerExercise, slotPosition)
      }
    }
    .sortedWith(EXERCISE_ORDER)
}

// RoutineExercise.slotPosition 순서로, 각 자리에 놓을 수 있는 종목 후보.
internal fun RoutineDay.exerciseOptions(): List<List<String>> =
  slots.flatMap { slot -> List(slot.pickCount) { slot.pool } }

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

// 같은 부위 슬롯에 주간 등장 순번(0,1,2…)을 매긴다. ExerciseSlot은 equals를 두지 않아 참조 동일성으로 구분된다.
private val PART_OCCURRENCES: Map<ExerciseSlot, Int> = buildMap {
  val counted = mutableMapOf<String, Int>()
  for (day in WEEKLY_ROUTINE) {
    for (slot in day.slots) {
      val occurrence = counted.getOrElse(slot.part) { 0 }
      put(slot, occurrence)
      counted[slot.part] = occurrence + 1
    }
  }
}

// 주의: gap이 pool.size와 서로소가 아닐 수 있어, pickCount가 3 이상이면 같은 종목이 두 번 뽑힐 수 있다.
private fun ExerciseSlot.pick(weekIndex: Int, partOccurrence: Int): List<String> {
  val cursor = weekIndex + partOccurrence
  val start = cursor.mod(pool.size)
  val gap = 1 + (cursor / pool.size).mod((pool.size - 1).coerceAtLeast(1))
  return List(pickCount) { offset -> pool[(start + offset * gap).mod(pool.size)] }
}

// 월요일에 주차가 올라가도록 +3 보정. 자세한 이유는 위 순환 규칙 설명 참고.
private fun weekIndex(date: LocalDate): Int = ((date.toEpochDay() + 3) / 7).toInt()
