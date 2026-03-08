package sungbinland.core.workout.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.util.Date
import sungbinland.core.workout.entity.WorkoutSessionEntity

@Dao public interface WorkoutSessionDao {
  @Upsert
  public suspend fun upsertWorkoutSession(session: WorkoutSessionEntity)

  @Query(
    "SELECT * FROM workout_sessions " +
      "WHERE performed_at >= :startOfDay AND performed_at < :endOfDayExclusive " +
      "ORDER BY performed_at ASC",
  )
  public suspend fun getWorkoutSessionsByDate(
    startOfDay: Date,
    endOfDayExclusive: Date,
  ): List<WorkoutSessionEntity>

  @Query("SELECT * FROM workout_sessions ORDER BY performed_at DESC")
  public suspend fun getAllWorkoutSessions(): List<WorkoutSessionEntity>

  @Delete
  public suspend fun deleteWorkoutSession(session: WorkoutSessionEntity)
}
