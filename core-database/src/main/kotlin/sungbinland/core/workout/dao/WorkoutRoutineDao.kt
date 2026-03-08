package sungbinland.core.workout.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import sungbinland.core.workout.entity.WorkoutRoutineEntity

@Dao public interface WorkoutRoutineDao {
  @Upsert
  public suspend fun upsertWorkoutRoutine(routine: WorkoutRoutineEntity)

  @Transaction
  @Query("SELECT * FROM workout_routines ORDER BY name ASC")
  public suspend fun getAllWorkoutRoutines(): List<WorkoutRoutineWithExercises>

  @Delete
  public suspend fun deleteWorkoutRoutine(routine: WorkoutRoutineEntity)
}
