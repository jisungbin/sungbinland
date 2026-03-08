package sungbinland.workout

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.core.workout.entity.SupplementIntakeEntity
import sungbinland.core.workout.entity.SupplementIntakeItemEntity
import sungbinland.core.workout.entity.WorkoutSessionEntity

internal class WorkoutStateHolder(
  private val mapper: WorkoutDashboardStateMapper,
  private val supplementIntakeDao: SupplementIntakeDao,
  private val workoutSessionDao: WorkoutSessionDao,
  private val nowProvider: () -> LocalDate,
) {
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val refreshState: MutableStateFlow<Long> = MutableStateFlow(0L)
  private val selectedDateState: MutableStateFlow<LocalDate> = MutableStateFlow(nowProvider())
  private val zoneId: ZoneId = ZoneId.systemDefault()

  internal val state: StateFlow<WorkoutDashboardState> = scope.launchMolecule(
    mode = RecompositionMode.Immediate,
  ) {
    val selectedDate by selectedDateState.collectAsState()
    val refreshKey by refreshState.collectAsState()
    val dashboardState by produceState(
      initialValue = workoutLoadingState(selectedDate = selectedDate),
      selectedDate,
      refreshKey,
    ) {
      value = mapper.createState(selectedDate = selectedDate)
    }
    dashboardState
  }

  internal fun moveToPreviousDate() {
    selectedDateState.update { selectedDate -> selectedDate.minusDays(1) }
  }

  internal fun moveToNextDate() {
    selectedDateState.update { selectedDate -> selectedDate.plusDays(1) }
  }

  internal fun moveToToday() {
    selectedDateState.update { nowProvider() }
  }

  internal fun saveSession(
    routineName: String,
    mainExerciseName: String,
    heaviestWeightInput: String,
  ) {
    scope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate(zoneId = zoneId)
      val endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate(zoneId = zoneId)
      val existingSession = workoutSessionDao.getWorkoutSessionsByDate(
        startOfDay = startOfDay,
        endOfDayExclusive = endOfDayExclusive,
      ).maxByOrNull { session -> session.performedAt.time }
      val normalizedMainExercise = mainExerciseName.trim()
      val nextMainExercise = if (normalizedMainExercise.isNotBlank()) {
        normalizedMainExercise
      } else {
        existingSession?.mainExerciseName.orEmpty()
      }
      val parsedWeight = heaviestWeightInput.toIntOrNull()
      val nextWeight = parsedWeight ?: existingSession?.heaviestWeightKg
      if (nextMainExercise.isBlank() || nextWeight == null || nextWeight <= 0) {
        return@launch
      }
      workoutSessionDao.upsertWorkoutSession(
        session = WorkoutSessionEntity(
          routineName = existingSession?.routineName ?: routineName.ifBlank { "상체" },
          mainExerciseName = nextMainExercise,
          heaviestWeightKg = nextWeight,
          performedAt = existingSession?.performedAt ?: startOfDay,
        ),
      )
      refresh()
    }
  }

  internal fun toggleSupplement(name: String) {
    scope.launch {
      val selectedDate = selectedDateState.value
      val startOfDay = selectedDate.toStartOfDayDate(zoneId = zoneId)
      val endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate(zoneId = zoneId)
      val intakeWithItems = supplementIntakeDao.getSupplementIntakesByDate(
        startOfDay = startOfDay,
        endOfDayExclusive = endOfDayExclusive,
      ).firstOrNull()
      val nextNames = intakeWithItems
        ?.items
        ?.map { item -> item.supplementName }
        ?.toMutableSet()
        ?: mutableSetOf()
      if (!nextNames.add(name)) {
        nextNames.remove(name)
      }
      if (nextNames.isEmpty()) {
        if (intakeWithItems != null) {
          supplementIntakeDao.deleteSupplementIntakeItemsByDate(
            intakeAt = intakeWithItems.intake.intakeAt,
          )
          supplementIntakeDao.deleteSupplementIntake(
            intake = intakeWithItems.intake,
          )
        }
      } else {
        val intakeAt = intakeWithItems?.intake?.intakeAt ?: startOfDay
        val intake = SupplementIntakeEntity(intakeAt = intakeAt)
        val items = nextNames
          .sorted()
          .map { supplementName ->
            SupplementIntakeItemEntity(
              intakeAt = intakeAt,
              supplementName = supplementName,
            )
          }
        supplementIntakeDao.upsertIntake(
          intake = intake,
          items = items,
        )
      }
      refresh()
    }
  }

  internal fun refresh() {
    refreshState.update { value -> value + 1L }
  }

  internal fun close() {
    scope.cancel()
  }

  private fun LocalDate.toStartOfDayDate(zoneId: ZoneId): Date =
    Date.from(atStartOfDay(zoneId).toInstant())
}
