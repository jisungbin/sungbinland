package sungbinland.workout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import sungbinland.core.alarm.HapticFeedback
import kotlinx.coroutines.launch
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutExerciseDao
import sungbinland.core.workout.dao.WorkoutRoutineDao
import sungbinland.core.workout.dao.WorkoutRoutineWithExercises
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.core.workout.entity.SupplementEntity
import sungbinland.core.workout.entity.WorkoutExerciseEntity
import sungbinland.core.workout.entity.WorkoutRoutineEntity
import sungbinland.core.workout.entity.WorkoutSessionEntity
import sungbinland.uikit.BottomSheetSceneStrategy
import sungbinland.uikit.FloatingButtonState
import sungbinland.uikit.LocalFabController

public fun EntryProviderScope<NavKey>.workoutEntry(
  supplementDao: SupplementDao,
  supplementIntakeDao: SupplementIntakeDao,
  timerRecordDao: TimerRecordDao,
  workoutSessionDao: WorkoutSessionDao,
  workoutRoutineDao: WorkoutRoutineDao,
  workoutExerciseDao: WorkoutExerciseDao,
  alarmReceiverClass: Class<*>,
  onNavigate: (NavKey) -> Unit,
  onBack: () -> Unit,
) {
  val mapper = WorkoutDashboardStateMapper(
    supplementDao = supplementDao,
    supplementIntakeDao = supplementIntakeDao,
    timerRecordDao = timerRecordDao,
    workoutExerciseDao = workoutExerciseDao,
    workoutRoutineDao = workoutRoutineDao,
    workoutSessionDao = workoutSessionDao,
    nowProvider = { LocalDate.now() },
  )
  val restTimer = WorkoutRestTimer()
  val factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return WorkoutViewModel(
        mapper = mapper,
        timerRecordDao = timerRecordDao,
        supplementIntakeDao = supplementIntakeDao,
        workoutSessionDao = workoutSessionDao,
      ) as T
    }
  }

  entry<WorkoutRoute>(
    metadata = mapOf("label" to "운동"),
  ) {
    val viewModel = viewModel<WorkoutViewModel>(factory = factory)
    val context = LocalContext.current
    val fabController = LocalFabController.current
    val fabProgressState = remember { mutableStateOf(0f) }
    var confettiTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(restTimer.startNanos) {
      if (!restTimer.isRunning) {
        fabProgressState.value = 0f
        return@LaunchedEffect
      }
      while (restTimer.isRunning) {
        fabProgressState.value = (restTimer.elapsedMillis() / 80_000f).coerceAtMost(1f)
        delay(100L)
      }
      fabProgressState.value = 0f
    }
    LaunchedEffect(Unit) {
      viewModel.timerCompletedEvent.collect {
        HapticFeedback.vibrateHeavy(context.applicationContext)
        confettiTrigger++
      }
    }

    val fabState = remember {
      FloatingButtonState(
        icon = Icons.Rounded.Timer,
        onClick = { onNavigate(WorkoutTimerSheetRoute) },
        onLongClick = {
          restTimer.stop()
          viewModel.cancelTimerAlarm(context.applicationContext, alarmReceiverClass)
        },
        progress = fabProgressState,
      )
    }
    SideEffect { fabController.set(WorkoutRoute, fabState) }
    Box {
      WorkoutScreen(
        viewModel = viewModel,
        onOpenRoutineDetailClick = { onNavigate(WorkoutRoutineDetailSheetRoute) },
        onManageSupplementClick = { onNavigate(WorkoutSupplementManagementSheetRoute) },
      )
      WorkoutConfetti(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
  }
  entry<WorkoutTimerSheetRoute>(
    metadata = BottomSheetSceneStrategy.bottomSheet(),
  ) {
    val viewModel = viewModel<WorkoutViewModel>(factory = factory)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
      if (!restTimer.isRunning) {
        restTimer.start()
        viewModel.startTimer(context.applicationContext, alarmReceiverClass)
        viewModel.monitorTimer(restTimer, context.applicationContext)
      }
    }
    WorkoutTimerSheet(restTimer = restTimer)
  }
  entry<WorkoutRoutineDetailSheetRoute>(
    metadata = BottomSheetSceneStrategy.bottomSheet(),
  ) {
    val viewModel = viewModel<WorkoutViewModel>(factory = factory)
    val scope = rememberCoroutineScope()
    var routines: ImmutableList<WorkoutRoutineWithExercises> by remember { mutableStateOf(persistentListOf()) }
    var recentSessions: ImmutableList<WorkoutSessionEntity> by remember { mutableStateOf(persistentListOf()) }
    LaunchedEffect(Unit) {
      routines = workoutRoutineDao.getAllWorkoutRoutines().toImmutableList()
      val zoneId = ZoneId.systemDefault()
      val now = LocalDate.now()
      val weekAgo = now.minusDays(7)
      val startOfDay = Date.from(weekAgo.atStartOfDay(zoneId).toInstant())
      val endOfDay = Date.from(now.plusDays(1).atStartOfDay(zoneId).toInstant())
      recentSessions = workoutSessionDao.getWorkoutSessionsByDate(startOfDay, endOfDay)
        .sortedByDescending { it.performedAt.time }
        .toImmutableList()
    }
    WorkoutRoutineDetailSheet(
      routines = routines,
      recentSessions = recentSessions,
      onAddRoutine = { name ->
        scope.launch {
          workoutRoutineDao.upsertWorkoutRoutine(WorkoutRoutineEntity(name = name))
          routines = workoutRoutineDao.getAllWorkoutRoutines().toImmutableList()
          viewModel.refresh()
        }
      },
      onAddExercise = { routineName, exerciseName ->
        scope.launch {
          workoutExerciseDao.upsertWorkoutExercise(
            WorkoutExerciseEntity(name = exerciseName, routineName = routineName),
          )
          routines = workoutRoutineDao.getAllWorkoutRoutines().toImmutableList()
          viewModel.refresh()
        }
      },
    )
  }
  entry<WorkoutSupplementManagementSheetRoute>(
    metadata = BottomSheetSceneStrategy.bottomSheet(),
  ) {
    val viewModel = viewModel<WorkoutViewModel>(factory = factory)
    val scope = rememberCoroutineScope()
    var supplements: ImmutableList<SupplementEntity> by remember { mutableStateOf(persistentListOf()) }
    LaunchedEffect(Unit) {
      supplements = supplementDao.getAllSupplements().toImmutableList()
    }
    WorkoutSupplementManagementSheet(
      supplements = supplements,
      onDelete = { name ->
        scope.launch {
          supplementDao.deleteSupplement(SupplementEntity(name = name))
          supplements = supplementDao.getAllSupplements().toImmutableList()
          viewModel.refresh()
        }
      },
      onRegister = { name, targetIntakeCount ->
        scope.launch {
          supplementDao.upsertSupplement(SupplementEntity(name = name, targetIntakeCount = targetIntakeCount))
          supplements = supplementDao.getAllSupplements().toImmutableList()
          viewModel.refresh()
        }
      },
      onClose = onBack,
    )
  }
}
