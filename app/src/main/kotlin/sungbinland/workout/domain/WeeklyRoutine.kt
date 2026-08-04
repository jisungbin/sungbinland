package sungbinland.workout.domain

import java.time.LocalDate

internal const val WARM_UP = "웜업"

// 한 부위에 배정된 종목 풀. 매주 pool에서 pickCount개를 골라 각 setsPerExercise세트씩 수행한다.
internal class ExerciseSlot(
  val part: String,
  val pool: List<String>,
  val pickCount: Int = 1,
  val setsPerExercise: Int,
)

internal class RoutineDay(
  val day: String,
  val category: String,
  val slots: List<ExerciseSlot>,
)

internal class RoutineExercise(
  val part: String,
  val name: String,
  val targetSets: Int,
) {
  // 주차마다 종목 구성이 바뀌므로 진행 상황은 순서(index)가 아니라 이 키로 저장한다.
  val key: String get() = "$part:$name"
}

private val ABS = listOf("케이블 크런치", "레그 레이즈")
private val SIDE_DELTOID = listOf("사이드 레터럴 레이즈", "스미스 비하인드 숄더 프레스")
private val REAR_DELTOID = listOf("벤트 오버 레이즈", "리버스 팩댁 플라이")
private val TRICEPS = listOf("트라이셉스 프레스", "케이블 트라이셉스 푸시다운")
private val BICEPS = listOf("이두 크리처 컬", "이두 드래그 컬", "이두 헤머컬")

internal val WEEKLY_ROUTINE: List<RoutineDay> = listOf(
  RoutineDay(
    "월", "Horizontal Push",
    listOf(
      ExerciseSlot(WARM_UP, listOf("덤벨 풀오버"), setsPerExercise = 2),
      ExerciseSlot(
        "중부가슴",
        listOf(
          "시티드 체스트 프레스",
          "라잉 체스트 프레스",
          "라잉 컨버징 체스트 프레스",
          "이너 체스트 프레스",
          "스미스 벤치 프레스",
          "팩댁 플라이",
        ),
        pickCount = 2,
        setsPerExercise = 6,
      ),
      ExerciseSlot("상부가슴", listOf("인클라인 체스트 프레스"), setsPerExercise = 4),
      ExerciseSlot("측면삼각근", SIDE_DELTOID, setsPerExercise = 4),
      ExerciseSlot("삼두", TRICEPS, setsPerExercise = 4),
      ExerciseSlot("복직근", ABS, setsPerExercise = 4),
    ),
  ),
  RoutineDay(
    "화", "Vertical Pull",
    listOf(
      ExerciseSlot(WARM_UP, listOf("풀오버 머신"), setsPerExercise = 2),
      ExerciseSlot(
        "광배",
        listOf("렛풀다운", "서큘러 렛풀다운", "롱 풀", "암풀다운"),
        pickCount = 2,
        setsPerExercise = 4,
      ),
      ExerciseSlot("후면삼각근", REAR_DELTOID, setsPerExercise = 4),
      ExerciseSlot("이두", BICEPS, setsPerExercise = 4),
      ExerciseSlot("복직근", ABS, setsPerExercise = 4),
    ),
  ),
  RoutineDay(
    "수", "LEGS",
    listOf(
      ExerciseSlot(WARM_UP, listOf("힙 어덕션·어브덕션"), setsPerExercise = 2),
      ExerciseSlot(
        "대퇴사두",
        listOf(
          "레그 프레스",
          "파워 레그 프레스",
          "핵 스쿼트",
          "스쿼트 프레스",
          "슈퍼 스쿼트",
          "고블릿 스쿼트",
          "레그 익스텐션",
        ),
        pickCount = 2,
        setsPerExercise = 4,
      ),
      ExerciseSlot(
        "햄스트링",
        listOf("루마니안 데드리프트", "스티프 데드리프트", "라잉 레그 컬", "레그 컬"),
        pickCount = 2,
        setsPerExercise = 6,
      ),
      // 스플릿 스쿼트는 대퇴사두 풀에 두지 않는다 — 같은 날 중복 등장을 막고 자극 비중이 큰 둔근에 전속.
      ExerciseSlot("둔근", listOf("킥 백", "스플릿 스쿼트"), setsPerExercise = 4),
      ExerciseSlot("카프", listOf("카프 레이즈"), setsPerExercise = 4),
    ),
  ),
  RoutineDay(
    "목", "Vertical Push",
    listOf(
      ExerciseSlot(WARM_UP, listOf("풀오버 머신"), setsPerExercise = 2),
      ExerciseSlot(
        "전면삼각근",
        listOf("숄더 프레스", "컨버징 숄더 프레스", "프론트 레이즈"),
        pickCount = 2,
        setsPerExercise = 6,
      ),
      ExerciseSlot("하부가슴", listOf("시티드 딥스"), setsPerExercise = 4),
      ExerciseSlot("측면삼각근", SIDE_DELTOID, setsPerExercise = 4),
      ExerciseSlot("삼두", TRICEPS, setsPerExercise = 4),
      ExerciseSlot("복직근", ABS, setsPerExercise = 4),
    ),
  ),
  RoutineDay(
    "금", "Horizontal Pull",
    listOf(
      ExerciseSlot(WARM_UP, listOf("슈러그"), setsPerExercise = 2),
      ExerciseSlot(
        "중부등",
        listOf("시티드 로우", "수평 로우", "로우 로우", "하이 로우", "티바 로우", "바벨 로우"),
        pickCount = 2,
        setsPerExercise = 5,
      ),
      ExerciseSlot("후면삼각근", REAR_DELTOID, setsPerExercise = 4),
      ExerciseSlot("이두", BICEPS, setsPerExercise = 4),
      ExerciseSlot("복직근", ABS, setsPerExercise = 4),
    ),
  ),
)

internal val DAY_LABELS: List<String> = WEEKLY_ROUTINE.map { it.day }

// 월~금(0~4)만 루틴 존재. 주말은 null(휴식일).
internal fun todayDayIndex(date: LocalDate = LocalDate.now()): Int? =
  (date.dayOfWeek.value - 1).takeIf { it in WEEKLY_ROUTINE.indices }

// 요일 자리마다 WEEKLY_ROUTINE의 몇 번째 대분류를 놓을지. 기본은 정순.
internal val DEFAULT_WEEK_ORDER: List<Int> = WEEKLY_ROUTINE.indices.toList()

/*
 * 요일 배치는 order로만 표현하고 WEEKLY_ROUTINE 자체는 절대 재정렬하지 않는다.
 * 종목 순환 오프셋(PART_OCCURRENCES)이 이 리스트의 순서에서 계산되므로, 리스트를 뒤섞으면
 * 스왑하지 않은 요일의 종목까지 함께 바뀐다. 반대로 order만 갈아치우면 pick의 입력이 하나도 변하지 않아
 * 교환된 두 날조차 원래 뽑던 종목을 그대로 들고 이동한다.
 */
internal fun routineOf(dayIndex: Int, order: List<Int> = DEFAULT_WEEK_ORDER): RoutineDay? =
  WEEKLY_ROUTINE.getOrNull(order.getOrElse(dayIndex) { -1 })

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
 * ── 2단계: 풀에서 어디부터 꺼낼지 (start)
 *
 *   start = (weekIndex * pickCount + partOccurrence) mod pool.size
 *
 * 핵심은 매주 *pickCount칸씩* 전진한다는 것(stride = pickCount)이다. 지난주에 꺼낸 구간 바로 뒤에서
 * 이어받으므로 겹침도 빈틈도 없다. 풀 크기 6에서 2개씩 꺼내는 경우:
 *
 *   0주: [0,1]   1주: [2,3]   2주: [4,5]   3주: [0,1] …  → 3주에 6종목 전부 1회전
 *
 * 풀 크기가 stride로 나누어지지 않아도 끝에서 앞으로 되감으며 계속 이어진다. 풀 7 / 2개씩:
 *
 *   0주: [0,1]  1주: [2,3]  2주: [4,5]  3주: [6,0]  4주: [1,2]  5주: [3,4]  6주: [5,6]  7주: [0,1] …
 *
 * 7주 만에 정확히 1회전한다. 일반적으로 pool.size와 stride의 최소공배수 / stride 주 만큼이면 전종목을 돈다.
 *
 * ── 3단계: partOccurrence를 왜 더하나
 *
 * 복직근·삼두·이두처럼 한 주에 여러 번 등장하는 부위는 같은 풀을 공유한다. 보정 없이 weekIndex만 쓰면
 * 그 날들에 똑같은 종목이 나오므로, 슬롯마다 시작점을 밀어 갈라놓는다.
 *
 * 여기서 "요일 번호"나 "주간 통짜 슬롯 번호" 같은 임의의 정수를 쓰면 함정에 빠진다. 두 슬롯의 번호 차이가
 * pool.size의 배수이면 mod 연산에서 같은 값으로 뭉개지기 때문이다. 실제로 겪은 사례:
 *
 *   요일 번호 → 이두(풀 3)는 화=1, 금=4. 차이 3이 mod 3에서 0이라 화·금이 같은 종목.
 *              번호에 상수를 곱해도(요일×7 등) 차이가 3의 배수인 사실은 그대로라 절대 갈라지지 않는다.
 *   슬롯 번호 → 이두는 갈라지지만 후면삼각근(풀 2)이 화=9, 금=25로 차이 16 → mod 2에서 0이라 충돌.
 *
 * 임의 정수로는 못 푼다. 대신 *그 부위가 주간 루틴에서 몇 번째로 등장하는가*(같은 part끼리 0,1,2… 로 새로 셈)를
 * 쓴다. 연속된 정수이므로 등장 횟수가 pool.size 이하인 부위는 mod 후에도 전부 서로 다른 값이 되어 충돌이 불가능하다.
 * 등장 횟수가 pool.size를 넘으면(복직근: 4회 등장 / 풀 2개) 원리상 겹칠 수밖에 없는데, 이 방식은 그 경우에도
 * 0,1,0,1로 균등히 나눠 각 종목을 정확히 같은 횟수만큼 쓴다.
 *
 * 트레이드오프: 같은 부위 슬롯을 추가·삭제하면 그 부위의 등장 번호가 밀려 해당 주 종목이 한 번 재배치된다.
 * 다른 부위는 영향받지 않고, 커버리지·등장 빈도 균등성도 그대로다.
 *
 * 풀 크기가 1인 부위(카프 레이즈, 웜업 종목 등)는 start가 무엇이든 결과가 같으므로 이 계산이 그냥 무해하게 통과한다.
 */
internal fun RoutineDay.exercises(date: LocalDate = LocalDate.now()): List<RoutineExercise> {
  val weekIndex = weekIndex(date)
  return slots.flatMap { slot -> slot.pick(weekIndex, PART_OCCURRENCES[slot] ?: 0) }
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

private fun ExerciseSlot.pick(weekIndex: Int, partOccurrence: Int): List<RoutineExercise> {
  val start = (weekIndex * pickCount + partOccurrence).mod(pool.size)
  return List(pickCount) { offset ->
    RoutineExercise(
      part = part,
      name = pool[(start + offset) % pool.size],
      targetSets = setsPerExercise,
    )
  }
}

// 월요일에 주차가 올라가도록 +3 보정. 자세한 이유는 위 순환 규칙 설명 참고.
private fun weekIndex(date: LocalDate): Int = ((date.toEpochDay() + 3) / 7).toInt()
