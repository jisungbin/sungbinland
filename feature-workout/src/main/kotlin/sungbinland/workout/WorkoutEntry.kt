package sungbinland.workout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import java.time.LocalDate
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.uikit.FloatingButtonState

public fun EntryProviderScope<NavKey>.workoutEntry(
  supplementDao: SupplementDao,
  supplementIntakeDao: SupplementIntakeDao,
  timerRecordDao: TimerRecordDao,
  workoutSessionDao: WorkoutSessionDao,
) {
  entry<WorkoutRoute>(
    metadata = mapOf(
      "label" to "운동",
      "floatingButtonState" to FloatingButtonState(
        icon = Icons.Rounded.Timer,
        onClick = {},
      ),
    ),
  ) {
    val mapper = retain(supplementDao, supplementIntakeDao, timerRecordDao, workoutSessionDao) {
      WorkoutDashboardStateMapper(
        supplementDao = supplementDao,
        supplementIntakeDao = supplementIntakeDao,
        timerRecordDao = timerRecordDao,
        workoutSessionDao = workoutSessionDao,
        nowProvider = { LocalDate.now() },
      )
    }
    val stateHolder = retain(mapper, supplementIntakeDao, workoutSessionDao) {
      WorkoutStateHolder(
        mapper = mapper,
        supplementIntakeDao = supplementIntakeDao,
        workoutSessionDao = workoutSessionDao,
        nowProvider = { LocalDate.now() },
      )
    }
    RetainedEffect(stateHolder) {
      onRetire {
        stateHolder.close()
      }
    }
    WorkoutScreen(
      stateHolder = stateHolder,
    )
  }
}
