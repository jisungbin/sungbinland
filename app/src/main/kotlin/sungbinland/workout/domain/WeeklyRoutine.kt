package sungbinland.workout.domain

import java.time.LocalDate

internal class RoutineItem(
  val name: String,
  val targetSets: Int,
)

internal class RoutineDay(
  val day: String,
  val category: String,
  val items: List<RoutineItem>,
)

internal val WEEKLY_ROUTINE: List<RoutineDay> = listOf(
  RoutineDay(
    "월", "Horizontal Push",
    listOf(
      RoutineItem("중부가슴", 8),
      RoutineItem("상부가슴", 4),
      RoutineItem("측면삼각근", 4),
      RoutineItem("삼두", 4),
      RoutineItem("복직근", 4),
    ),
  ),
  RoutineDay(
    "화", "Vertical Pull",
    listOf(
      RoutineItem("광배", 12),
      RoutineItem("후면삼각근", 4),
      RoutineItem("이두", 4),
      RoutineItem("복직근", 4),
    ),
  ),
  RoutineDay(
    "수", "LEGS",
    listOf(
      RoutineItem("대퇴사두", 8),
      RoutineItem("햄스트링", 6),
      RoutineItem("둔근", 6),
      RoutineItem("카프", 4),
    ),
  ),
  RoutineDay(
    "목", "Vertical Push",
    listOf(
      RoutineItem("전면삼각근", 8),
      RoutineItem("하부가슴", 4),
      RoutineItem("삼두", 4),
      RoutineItem("측면삼각근", 4),
      RoutineItem("복직근", 4),
    ),
  ),
  RoutineDay(
    "금", "Horizontal Pull",
    listOf(
      RoutineItem("중부등(승모·능형근·광배)", 12),
      RoutineItem("후면삼각근", 4),
      RoutineItem("이두", 4),
      RoutineItem("복직근", 4),
    ),
  ),
)

// 월~금(1~5)만 루틴 존재. 주말은 null(휴식일).
internal fun todayRoutine(): RoutineDay? = WEEKLY_ROUTINE.getOrNull(LocalDate.now().dayOfWeek.value - 1)
