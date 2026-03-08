package sungbinland.workout

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutSessionDao

internal class WorkoutDashboardStateMapper(
  private val supplementDao: SupplementDao,
  private val supplementIntakeDao: SupplementIntakeDao,
  private val timerRecordDao: TimerRecordDao,
  private val workoutSessionDao: WorkoutSessionDao,
  private val nowProvider: () -> LocalDate,
) {
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  internal suspend fun createState(selectedDate: LocalDate): WorkoutDashboardState {
    val nowDate: LocalDate = nowProvider()
    val startOfDay: Date = selectedDate.toStartOfDayDate()
    val endOfDayExclusive: Date = selectedDate.plusDays(1).toStartOfDayDate()
    val sessionsOfDate = workoutSessionDao.getWorkoutSessionsByDate(
      startOfDay = startOfDay,
      endOfDayExclusive = endOfDayExclusive,
    )
    val latestSession = sessionsOfDate.maxByOrNull { session -> session.performedAt.time }
    val timerRecordsOfDate = timerRecordDao.getTimerRecordsByDate(
      startOfDay = startOfDay,
      endOfDayExclusive = endOfDayExclusive,
    )
    val firstTimerRecord = timerRecordsOfDate.minByOrNull { record -> record.startedAt.time }
    val lastTimerRecord = timerRecordsOfDate.maxByOrNull { record -> record.startedAt.time }
    val firstTimerStartedAt = firstTimerRecord?.startedAt?.toTimeText() ?: "--:--"
    val lastTimerStartedAt = lastTimerRecord?.startedAt?.toTimeText() ?: "--:--"
    val timerSpan = timerSpanText(
      firstStartedAt = firstTimerRecord?.startedAt,
      lastStartedAt = lastTimerRecord?.startedAt,
    )
    val recentDates: List<LocalDate> = (6 downTo 0).map { daysAgo ->
      nowDate.minusDays(daysAgo.toLong())
    }
    val previousDates: List<LocalDate> = (13 downTo 7).map { daysAgo ->
      nowDate.minusDays(daysAgo.toLong())
    }
    val recentSessions = workoutSessionDao.getWorkoutSessionsByDate(
      startOfDay = nowDate.minusDays(6).toStartOfDayDate(),
      endOfDayExclusive = nowDate.plusDays(1).toStartOfDayDate(),
    )
    val previousSessions = workoutSessionDao.getWorkoutSessionsByDate(
      startOfDay = nowDate.minusDays(13).toStartOfDayDate(),
      endOfDayExclusive = nowDate.minusDays(6).toStartOfDayDate(),
    )
    val recentMaxByDate = recentSessions.dailyMaxByDate()
    val previousMaxByDate = previousSessions.dailyMaxByDate()
    val recentMaxes = recentDates.map { date -> recentMaxByDate[date] ?: 0 }
    val previousMaxes = previousDates.map { date -> previousMaxByDate[date] ?: 0 }
    val trendValues = recentDates.map { date ->
      val maxWeight = recentMaxByDate[date] ?: 0
      WorkoutTrendValueState(
        label = "${date.monthValue}/${date.dayOfMonth}",
        value = "${maxWeight}kg",
      )
    }
    val recentPeak = recentMaxes.maxOrNull() ?: 0
    val previousPeak = previousMaxes.maxOrNull() ?: 0

    val intakeWithItems = supplementIntakeDao.getSupplementIntakesByDate(
      startOfDay = startOfDay,
      endOfDayExclusive = endOfDayExclusive,
    )
    val consumedNames = intakeWithItems
      .flatMap { intake -> intake.items.map { item -> item.supplementName } }
      .toSet()
    val registeredSupplements = supplementDao.getAllSupplements()
      .map { supplement -> supplement.name }
    val supplementNames = if (registeredSupplements.isNotEmpty()) {
      registeredSupplements
    } else {
      consumedNames.toList().sorted()
    }

    return WorkoutDashboardState(
      summary = WorkoutSummaryState(
        dayTag = if (selectedDate == nowDate) "TODAY" else "DAY",
        displayDate = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
        routineTitle = latestSession?.routineName ?: "상체",
        mainExerciseValue = latestSession?.mainExerciseName ?: "[...]",
        maxWeightValue = latestSession?.let { session -> "${session.heaviestWeightKg}kg" } ?: "[...]",
        firstTimerStartedAt = firstTimerStartedAt,
        lastTimerStartedAt = lastTimerStartedAt,
        timerSpan = timerSpan,
        trendDelta = "최고 중량 ${recentPeak}kg",
        trendDeltaMeta = "지난주 ${previousPeak}kg",
        trendValues = trendValues,
      ),
      supplements = WorkoutSupplementChecklistState(
        items = supplementNames.map { name ->
          WorkoutSupplementItemState(
            name = name,
            meta = "복용 기록",
            checked = consumedNames.contains(name),
          )
        },
      ),
    )
  }

  private fun List<sungbinland.core.workout.entity.WorkoutSessionEntity>.dailyMaxByDate(): Map<LocalDate, Int> {
    val maxByDate = mutableMapOf<LocalDate, Int>()
    forEach { session ->
      val date = session.performedAt.toInstant().atZone(zoneId).toLocalDate()
      val previous = maxByDate[date] ?: 0
      if (session.heaviestWeightKg > previous) {
        maxByDate[date] = session.heaviestWeightKg
      }
    }
    return maxByDate
  }

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())

  private fun Date.toTimeText(): String =
    toInstant().atZone(zoneId).toLocalTime().format(timeFormatter)

  private fun timerSpanText(
    firstStartedAt: Date?,
    lastStartedAt: Date?,
  ): String {
    if (firstStartedAt == null || lastStartedAt == null) {
      return "--:--"
    }
    val diffMinutes = ((lastStartedAt.time - firstStartedAt.time).coerceAtLeast(0L) / 60_000L)
    val hours = diffMinutes / 60L
    val minutes = diffMinutes % 60L
    val span = String.format(Locale.KOREA, "%02d:%02d", hours, minutes)
    return if (diffMinutes >= 120L) "$span 🔥" else span
  }
}
