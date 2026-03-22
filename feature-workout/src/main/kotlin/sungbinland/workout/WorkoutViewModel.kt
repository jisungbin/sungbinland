package sungbinland.workout

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sungbinland.core.alarm.HapticFeedback
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.core.workout.entity.SupplementIntakeEntity
import sungbinland.core.workout.entity.SupplementIntakeItemEntity
import sungbinland.core.workout.entity.TimerRecordEntity
import sungbinland.core.workout.entity.WorkoutSessionEntity

@OptIn(ExperimentalCoroutinesApi::class)
internal class WorkoutViewModel(
  private val mapper: WorkoutDashboardStateMapper,
  private val timerRecordDao: TimerRecordDao,
  private val supplementIntakeDao: SupplementIntakeDao,
  private val workoutSessionDao: WorkoutSessionDao,
) : ViewModel() {
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val selectedDateState: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())
  private val refreshState: MutableStateFlow<Long> = MutableStateFlow(0L)

  private val _timerCompletedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  internal val timerCompletedEvent: SharedFlow<Unit> = _timerCompletedEvent

  internal val state: StateFlow<WorkoutDashboardState> =
    combine(selectedDateState, refreshState) { date, _ -> date }
      .mapLatest { date -> mapper.createState(selectedDate = date) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = workoutLoadingState(selectedDate = selectedDateState.value),
      )

  internal fun moveToNextDate() {
    selectedDateState.update { it.plusDays(1) }
  }

  internal fun moveToPreviousDate() {
    selectedDateState.update { it.minusDays(1) }
  }

  internal fun moveToToday() {
    selectedDateState.update { LocalDate.now() }
  }

  internal fun saveSession(routineName: String, mainExerciseName: String, mainExerciseName2: String) {
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate()
      val endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate()
      val existingSession = workoutSessionDao.getWorkoutSessionsByDate(
        startOfDay = startOfDay,
        endOfDayExclusive = endOfDayExclusive,
      ).maxByOrNull { it.performedAt.time }
      val normalizedMainExercise = mainExerciseName.trim()
      val nextMainExercise = when {
        normalizedMainExercise.isNotBlank() -> normalizedMainExercise
        else -> existingSession?.mainExerciseName.orEmpty()
      }
      if (nextMainExercise.isBlank()) return@launch
      val normalizedMainExercise2 = mainExerciseName2.trim()
      val nextMainExercise2 = when {
        normalizedMainExercise2.isNotBlank() -> normalizedMainExercise2
        else -> existingSession?.mainExerciseName2.orEmpty()
      }
      workoutSessionDao.upsertWorkoutSession(
        session = WorkoutSessionEntity(
          routineName = existingSession?.routineName ?: routineName.ifBlank { return@launch },
          mainExerciseName = nextMainExercise,
          mainExerciseName2 = nextMainExercise2,
          performedAt = existingSession?.performedAt ?: startOfDay,
        ),
      )
      refresh()
    }
  }

  internal fun incrementSupplement(name: String) {
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate()
      val intakeWithItems = supplementIntakeDao.getSupplementIntakeByExactDate(date = startOfDay)
      val intakeAt = intakeWithItems?.intake?.intakeAt ?: startOfDay
      val existingItem = intakeWithItems?.items?.firstOrNull { it.supplementName == name }
      val newCount = (existingItem?.intakeCount ?: 0) + 1
      val otherItems = intakeWithItems?.items?.fastFilter { it.supplementName != name } ?: emptyList()
      val updatedItems = otherItems + SupplementIntakeItemEntity(
        intakeAt = intakeAt,
        supplementName = name,
        intakeCount = newCount,
      )
      supplementIntakeDao.upsertIntake(
        intake = SupplementIntakeEntity(intakeAt = intakeAt),
        items = updatedItems,
      )
      refresh()
    }
  }

  internal fun decrementSupplement(name: String) {
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate()
      val intakeWithItems = supplementIntakeDao.getSupplementIntakeByExactDate(date = startOfDay) ?: return@launch
      val existingItem = intakeWithItems.items.firstOrNull { it.supplementName == name } ?: return@launch
      val newCount = existingItem.intakeCount - 1
      val intakeAt = intakeWithItems.intake.intakeAt
      val otherItems = intakeWithItems.items.fastFilter { it.supplementName != name }
      if (newCount <= 0) {
        if (otherItems.isEmpty()) {
          supplementIntakeDao.deleteSupplementIntakeItemsByDate(intakeAt = intakeAt)
          supplementIntakeDao.deleteSupplementIntake(intake = intakeWithItems.intake)
        } else {
          supplementIntakeDao.upsertIntake(
            intake = SupplementIntakeEntity(intakeAt = intakeAt),
            items = otherItems,
          )
        }
      } else {
        val updatedItems = otherItems + SupplementIntakeItemEntity(
          intakeAt = intakeAt,
          supplementName = name,
          intakeCount = newCount,
        )
        supplementIntakeDao.upsertIntake(
          intake = SupplementIntakeEntity(intakeAt = intakeAt),
          items = updatedItems,
        )
      }
      refresh()
    }
  }

  internal fun selectRoutine(routineName: String) {
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate()
      val endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate()
      val existingSession = workoutSessionDao.getWorkoutSessionsByDate(
        startOfDay = startOfDay,
        endOfDayExclusive = endOfDayExclusive,
      ).maxByOrNull { it.performedAt.time }
      workoutSessionDao.upsertWorkoutSession(
        session = WorkoutSessionEntity(
          routineName = routineName,
          mainExerciseName = existingSession?.mainExerciseName ?: "",
          mainExerciseName2 = existingSession?.mainExerciseName2 ?: "",
          performedAt = existingSession?.performedAt ?: startOfDay,
        ),
      )
      refresh()
    }
  }

  internal fun clearTodayTimerRecords() {
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate()
      val endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate()
      timerRecordDao.deleteTimerRecordsByDate(startOfDay, endOfDayExclusive)
      refresh()
    }
  }

  internal fun startTimer(applicationContext: Context, alarmReceiverClass: Class<*>) {
    viewModelScope.launch {
      timerRecordDao.upsertTimerRecord(TimerRecordEntity(startedAt = Date()))
      refresh()
      @Suppress("UNCHECKED_CAST")
      sungbinland.core.alarm.RestTimerAlarmScheduler.schedule(
        context = applicationContext,
        delayMillis = 80_000L,
        receiverClass = alarmReceiverClass as Class<android.content.BroadcastReceiver>,
      )
    }
  }

  internal fun cancelTimerAlarm(applicationContext: Context, alarmReceiverClass: Class<*>) {
    @Suppress("UNCHECKED_CAST")
    sungbinland.core.alarm.RestTimerAlarmScheduler.cancel(
      context = applicationContext,
      receiverClass = alarmReceiverClass as Class<android.content.BroadcastReceiver>,
    )
  }

  internal fun monitorTimer(restTimer: WorkoutRestTimer, applicationContext: Context) {
    viewModelScope.launch {
      while (restTimer.isRunning) {
        if (restTimer.elapsedMillis() >= 80_000L) {
          restTimer.stop()
          HapticFeedback.vibrateHeavy(applicationContext)
          _timerCompletedEvent.emit(Unit)
          break
        }
        delay(100L)
      }
    }
  }

  internal fun refresh() {
    refreshState.update { it + 1L }
  }

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())
}
