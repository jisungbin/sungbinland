package sungbinland.workout

import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapTo
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.SupplementIntakeWithItems
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutExerciseDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.core.workout.entity.TimerRecordEntity
import sungbinland.core.workout.entity.WorkoutSessionEntity

internal class WorkoutDashboardStateMapper(
  private val supplementDao: SupplementDao,
  private val supplementIntakeDao: SupplementIntakeDao,
  private val timerRecordDao: TimerRecordDao,
  private val workoutExerciseDao: WorkoutExerciseDao,
  private val workoutSessionDao: WorkoutSessionDao,
  private val nowProvider: () -> LocalDate,
) {
  private val zoneId: ZoneId = ZoneId.systemDefault()

  internal suspend fun createState(selectedDate: LocalDate): WorkoutDashboardState {
    val nowDate = nowProvider()
    val startOfDay = selectedDate.toStartOfDayDate()
    val endOfDayExclusive = selectedDate.plusDays(1).toStartOfDayDate()
    val snapshot = coroutineScope {
      val sessionsOfDateDeferred = async {
        workoutSessionDao.getWorkoutSessionsByDate(
          startOfDay = startOfDay,
          endOfDayExclusive = endOfDayExclusive,
        )
      }
      val mainExerciseSuggestionsDeferred = async {
        workoutExerciseDao.getAllWorkoutExercises()
          .fastMapTo(persistentListOf<String>().builder()) { exercise -> exercise.name }
          .build()
      }
      val timerRecordsOfDateDeferred = async {
        timerRecordDao.getTimerRecordsByDate(
          startOfDay = startOfDay,
          endOfDayExclusive = endOfDayExclusive,
        )
      }
      val intakeWithItemsDeferred = async {
        supplementIntakeDao.getSupplementIntakesByDate(
          startOfDay = startOfDay,
          endOfDayExclusive = endOfDayExclusive,
        )
      }
      val registeredSupplementNamesDeferred = if (selectedDate < nowDate) {
        null
      } else {
        async { supplementDao.getAllSupplements().fastMap { supplement -> supplement.name } }
      }
      WorkoutQuerySnapshot(
        sessionsOfDate = sessionsOfDateDeferred.await(),
        mainExerciseSuggestions = mainExerciseSuggestionsDeferred.await(),
        timerRecordsOfDate = timerRecordsOfDateDeferred.await(),
        intakeWithItems = intakeWithItemsDeferred.await(),
        registeredSupplementNames = registeredSupplementNamesDeferred?.await(),
      )
    }
    val latestSession = snapshot.sessionsOfDate.maxByOrNull { session -> session.performedAt.time }
    val firstTimerRecord = snapshot.timerRecordsOfDate.minByOrNull { record -> record.startedAt.time }
    val lastTimerRecord = snapshot.timerRecordsOfDate.maxByOrNull { record -> record.startedAt.time }

    val consumedNames = buildSet {
      snapshot.intakeWithItems.fastForEach { intake ->
        intake.items.fastForEach { item ->
          add(item.supplementName)
        }
      }
    }
    val supplementNames = when {
      selectedDate < nowDate -> consumedNames.toList().sorted()
      else -> {
        val registered = snapshot.registeredSupplementNames ?: emptyList()
        when {
          registered.isNotEmpty() -> registered
          else -> consumedNames.toList().sorted()
        }
      }
    }

    return WorkoutDashboardState(
      summary = WorkoutSummaryState(
        dayTag = dayTag(selectedDate = selectedDate, nowDate = nowDate),
        displayDate = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
        routineTitle = latestSession?.routineName ?: "상체",
        mainExerciseValue = latestSession?.mainExerciseName ?: "[...]",
        mainExerciseSuggestions = snapshot.mainExerciseSuggestions,
        firstTimerStartedAt = firstTimerRecord?.startedAt?.toTimerValue() ?: emptyTimerValue,
        lastTimerStartedAt = lastTimerRecord?.startedAt?.toTimerValue() ?: emptyTimerValue,
        timerSpan = timerSpanValue(
          firstStartedAt = firstTimerRecord?.startedAt,
          lastStartedAt = lastTimerRecord?.startedAt,
        ),
      ),
      supplements = WorkoutSupplementChecklistState(
        items = supplementNames.fastMapTo(persistentListOf<WorkoutSupplementItemState>().builder()) { name ->
          WorkoutSupplementItemState(
            name = name,
            meta = "복용 기록",
            checked = consumedNames.contains(name),
          )
        }.build(),
      ),
    )
  }

  private fun dayTag(selectedDate: LocalDate, nowDate: LocalDate): String {
    val diff = java.time.temporal.ChronoUnit.DAYS.between(nowDate, selectedDate)
    return when {
      diff == 0L -> "오늘"
      diff > 0L -> "${diff}일 후"
      else -> "${-diff}일 전"
    }
  }

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())

  private fun Date.toTimerValue(): WorkoutTimerValueState {
    val time = toInstant().atZone(zoneId).toLocalTime()
    return WorkoutTimerValueState(
      hours = String.format(Locale.KOREA, "%02d", time.hour),
      minutes = String.format(Locale.KOREA, "%02d", time.minute),
    )
  }

  private fun timerSpanValue(
    firstStartedAt: Date?,
    lastStartedAt: Date?,
  ): WorkoutTimerValueState {
    if (firstStartedAt == null || lastStartedAt == null) return emptyTimerValue
    val diffMinutes = (lastStartedAt.time - firstStartedAt.time).coerceAtLeast(0L) / 60_000L
    return WorkoutTimerValueState(
      hours = String.format(Locale.KOREA, "%02d", diffMinutes / 60L),
      minutes = String.format(Locale.KOREA, "%02d", diffMinutes % 60L),
    )
  }

  private class WorkoutQuerySnapshot(
    val sessionsOfDate: List<WorkoutSessionEntity>,
    val mainExerciseSuggestions: ImmutableList<String>,
    val timerRecordsOfDate: List<TimerRecordEntity>,
    val intakeWithItems: List<SupplementIntakeWithItems>,
    val registeredSupplementNames: List<String>?,
  )
}
