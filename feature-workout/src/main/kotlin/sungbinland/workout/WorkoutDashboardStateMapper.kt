package sungbinland.workout

import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapTo
import kotlinx.collections.immutable.persistentListOf
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutExerciseDao
import sungbinland.core.workout.dao.WorkoutSessionDao

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
    val nowDate: LocalDate = nowProvider()
    val startOfDay: Date = selectedDate.toStartOfDayDate()
    val endOfDayExclusive: Date = selectedDate.plusDays(1).toStartOfDayDate()
    val sessionsOfDate = workoutSessionDao.getWorkoutSessionsByDate(
      startOfDay = startOfDay,
      endOfDayExclusive = endOfDayExclusive,
    )
    val latestSession = sessionsOfDate.maxByOrNull { session -> session.performedAt.time }
    val mainExerciseSuggestions = workoutExerciseDao.getAllWorkoutExercises()
      .fastMapTo(persistentListOf<String>().builder()) { exercise -> exercise.name }
      .build()
    val timerRecordsOfDate = timerRecordDao.getTimerRecordsByDate(
      startOfDay = startOfDay,
      endOfDayExclusive = endOfDayExclusive,
    )
    val firstTimerRecord = timerRecordsOfDate.minByOrNull { record -> record.startedAt.time }
    val lastTimerRecord = timerRecordsOfDate.maxByOrNull { record -> record.startedAt.time }

    val intakeWithItems = supplementIntakeDao.getSupplementIntakesByDate(
      startOfDay = startOfDay,
      endOfDayExclusive = endOfDayExclusive,
    )
    val consumedNames = buildSet {
      intakeWithItems.fastForEach { intake ->
        intake.items.fastForEach { item ->
          add(item.supplementName)
        }
      }
    }
    val supplementNames = when {
      selectedDate < nowDate -> consumedNames.toList().sorted()
      else -> {
        val registered = supplementDao.getAllSupplements().fastMap { supplement -> supplement.name }
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
        mainExerciseSuggestions = mainExerciseSuggestions,
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
}
