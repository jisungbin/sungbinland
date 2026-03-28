package sungbinland.workout

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
@Poko internal class WorkoutDashboardState(
  internal val summary: WorkoutSummaryState,
  internal val timerRecords: ImmutableList<String>,
)

@Immutable
@Poko internal class WorkoutSummaryState(
  internal val dayTag: String,
  internal val displayDate: String,
  internal val routineTitle: String,
  internal val routineNames: ImmutableList<String>,
  internal val mainExerciseValue: String,
  internal val mainExerciseValue2: String,
  internal val mainExerciseSuggestions: ImmutableList<String>,
  internal val firstTimerStartedAt: WorkoutTimerValueState,
  internal val lastTimerStartedAt: WorkoutTimerValueState,
  internal val timerSpan: WorkoutTimerValueState,
)

@Immutable
@Poko internal class WorkoutTimerValueState(
  internal val hours: String,
  internal val minutes: String,
)

internal val emptyTimerValue = WorkoutTimerValueState(hours = "--", minutes = "--")

internal fun workoutLoadingState(selectedDate: LocalDate): WorkoutDashboardState =
  WorkoutDashboardState(
    summary = WorkoutSummaryState(
      dayTag = "TODAY",
      displayDate = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
      routineTitle = "기록 없음",
      routineNames = persistentListOf(),
      mainExerciseValue = "[...]",
      mainExerciseValue2 = "[...]",
      mainExerciseSuggestions = persistentListOf(),
      firstTimerStartedAt = emptyTimerValue,
      lastTimerStartedAt = emptyTimerValue,
      timerSpan = emptyTimerValue,
    ),
    timerRecords = persistentListOf(),
  )
