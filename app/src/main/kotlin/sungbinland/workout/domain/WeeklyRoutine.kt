package sungbinland.workout.domain

import java.time.LocalDate

internal class RoutineDay(
  val day: String,
  val category: String,
  val composition: String,
  val lastFour: String,
)

internal val WEEKLY_ROUTINE: List<RoutineDay> = listOf(
  RoutineDay("월", "PUSH A (Horizontal Push)", "중부가슴 8 + 상부가슴 4 + 측면삼각근 4 + 삼두 4", "복직근"),
  RoutineDay("화", "PULL A (Vertical Pull)", "광배 12 + 후면삼각근 4 + 이두 4", "복직근"),
  RoutineDay("수", "LEGS", "대퇴사두 8 + 햄스트링 6 + 둔근 6", "카프"),
  RoutineDay("목", "PUSH B (Vertical Push)", "전면삼각근 8 + 하부가슴 4 + 삼두 4 + 측면삼각근 4", "복직근"),
  RoutineDay("금", "PULL B (Horizontal Pull)", "중부등(승모·능형근·광배) 12 + 후면삼각근 4 + 이두 4", "복직근"),
)

// 월~금(1~5)만 루틴 존재. 주말은 null(휴식일).
internal fun todayRoutine(): RoutineDay? = WEEKLY_ROUTINE.getOrNull(LocalDate.now().dayOfWeek.value - 1)
