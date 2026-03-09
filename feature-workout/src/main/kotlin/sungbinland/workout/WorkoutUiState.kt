package sungbinland.workout

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
@Poko internal class WorkoutDashboardState(
  internal val summary: WorkoutSummaryState,
  internal val supplements: WorkoutSupplementChecklistState,
)

@Immutable
@Poko internal class WorkoutSummaryState(
  internal val dayTag: String,
  internal val displayDate: String,
  internal val routineTitle: String,
  internal val mainExerciseValue: String,
  internal val maxWeightValue: String,
  internal val firstTimerStartedAt: String,
  internal val lastTimerStartedAt: String,
  internal val timerSpan: String,
  internal val trendDelta: String,
  internal val trendDeltaMeta: String,
  internal val trendValues: ImmutableList<WorkoutTrendValueState>,
)

@Immutable
@Poko internal class WorkoutTrendValueState(
  internal val label: String,
  internal val value: String,
)

@Immutable
@Poko internal class WorkoutSupplementChecklistState(
  internal val items: ImmutableList<WorkoutSupplementItemState>,
)

@Immutable
@Poko internal class WorkoutSupplementItemState(
  internal val name: String,
  internal val meta: String,
  internal val checked: Boolean,
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
      }.toImmutableList(),
    ),
    supplements = WorkoutSupplementChecklistState(
      items = persistentListOf(),
    ),
  )
