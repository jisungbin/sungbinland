package sungbinland.workout

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import java.time.LocalDate

@Immutable @Poko internal class WorkoutDashboardState(
  val summary: WorkoutSummaryState,
  val supplements: WorkoutSupplementChecklistState,
)

@Immutable @Poko internal class WorkoutSummaryState(
  val dayTag: String,
  val displayDate: String,
  val routineTitle: String,
  val mainExerciseValue: String,
  val maxWeightValue: String,
  val firstTimerStartedAt: String,
  val lastTimerStartedAt: String,
  val timerSpan: String,
  val trendDelta: String,
  val trendDeltaMeta: String,
  val trendValues: List<WorkoutTrendValueState>,
)

@Immutable @Poko internal class WorkoutTrendValueState(
  val label: String,
  val value: String,
)

@Immutable @Poko internal class WorkoutSupplementChecklistState(
  val items: List<WorkoutSupplementItemState>,
)

@Immutable @Poko internal class WorkoutSupplementItemState(
  val name: String,
  val meta: String,
  val checked: Boolean,
)

internal fun workoutLoadingState(selectedDate: LocalDate): WorkoutDashboardState =
  WorkoutDashboardState(
    summary = WorkoutSummaryState(
      dayTag = "TODAY",
      displayDate = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
      routineTitle = "기록 없음",
      mainExerciseValue = "[...]",
      maxWeightValue = "[...]",
      firstTimerStartedAt = "--:--",
      lastTimerStartedAt = "--:--",
      timerSpan = "--:--",
      trendDelta = "0kg",
      trendDeltaMeta = "지난주 0kg",
      trendValues = (6 downTo 0).map { dayOffset ->
        val date = selectedDate.minusDays(dayOffset.toLong())
        WorkoutTrendValueState(
          label = "${date.monthValue}/${date.dayOfMonth}",
          value = "0kg",
        )
      },
    ),
    supplements = WorkoutSupplementChecklistState(
      items = emptyList(),
    ),
  )
