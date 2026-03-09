package sungbinland.workout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import java.time.LocalDate
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.uikit.FloatingButtonState
import sungbinland.uikit.LocalFabController

public fun EntryProviderScope<NavKey>.workoutEntry(
  supplementDao: SupplementDao,
  supplementIntakeDao: SupplementIntakeDao,
  timerRecordDao: TimerRecordDao,
  workoutSessionDao: WorkoutSessionDao,
) {
  entry<WorkoutRoute>(
    metadata = mapOf("label" to "운동"),
  ) {
    val fabController = LocalFabController.current
    var showTimerSheet by rememberSaveable { mutableStateOf(false) }
    val fabState = remember {
      FloatingButtonState(icon = Icons.Rounded.Timer, onClick = { showTimerSheet = true })
    }
    SideEffect { fabController.set(WorkoutRoute, fabState) }
    val mapper = retain(supplementDao, supplementIntakeDao, timerRecordDao, workoutSessionDao) {
      WorkoutDashboardStateMapper(
        supplementDao = supplementDao,
        supplementIntakeDao = supplementIntakeDao,
        timerRecordDao = timerRecordDao,
        workoutSessionDao = workoutSessionDao,
        nowProvider = { LocalDate.now() },
      )
    }
    val stateHolder = retain(mapper, timerRecordDao, supplementIntakeDao, workoutSessionDao) {
      WorkoutStateHolder(
        mapper = mapper,
        timerRecordDao = timerRecordDao,
        supplementIntakeDao = supplementIntakeDao,
        workoutSessionDao = workoutSessionDao,
        nowProvider = { LocalDate.now() },
      )
    }
    RetainedEffect(stateHolder) { onRetire(stateHolder::close) }
    WorkoutScreen(
      stateHolder = stateHolder,
      showTimerSheet = showTimerSheet,
      onDismissTimerSheet = { showTimerSheet = false },
    )
  }
}
