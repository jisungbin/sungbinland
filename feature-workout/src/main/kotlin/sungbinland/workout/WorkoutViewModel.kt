package sungbinland.workout

import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

  internal fun saveSession(routineName: String, mainExerciseName: String) {
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
      workoutSessionDao.upsertWorkoutSession(
        session = WorkoutSessionEntity(
          routineName = existingSession?.routineName ?: routineName.ifBlank { "상체" },
          mainExerciseName = nextMainExercise,
          performedAt = existingSession?.performedAt ?: startOfDay,
        ),
      )
      refresh()
    }
  }

  internal fun toggleSupplement(name: String) {
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate()
      val intakeWithItems = supplementIntakeDao.getSupplementIntakeByExactDate(date = startOfDay)
      val nextNames = intakeWithItems
        ?.items
        ?.fastMap { item -> item.supplementName }
        ?.toMutableSet()
        ?: mutableSetOf()
      if (!nextNames.add(name)) {
        nextNames.remove(name)
      }
      if (nextNames.isEmpty()) {
        if (intakeWithItems != null) {
          supplementIntakeDao.deleteSupplementIntakeItemsByDate(intakeAt = intakeWithItems.intake.intakeAt)
          supplementIntakeDao.deleteSupplementIntake(intake = intakeWithItems.intake)
        }
      } else {
        val intakeAt = intakeWithItems?.intake?.intakeAt ?: startOfDay
        val intake = SupplementIntakeEntity(intakeAt = intakeAt)
        val items = nextNames.sorted().fastMap { supplementName ->
          SupplementIntakeItemEntity(intakeAt = intakeAt, supplementName = supplementName)
        }
        supplementIntakeDao.upsertIntake(intake = intake, items = items)
      }
      refresh()
    }
  }

  internal fun startTimer() {
    viewModelScope.launch {
      timerRecordDao.upsertTimerRecord(TimerRecordEntity(startedAt = Date()))
      refresh()
    }
  }

  internal fun refresh() {
    refreshState.update { it + 1L }
  }

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())
}
