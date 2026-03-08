package sungbinland.core.workout.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import sungbinland.core.workout.entity.WorkoutExerciseEntity

@Dao public interface WorkoutExerciseDao {
  @Upsert
  public suspend fun upsertWorkoutExercise(exercise: WorkoutExerciseEntity)

  @Query("SELECT * FROM workout_exercises ORDER BY name ASC")
  public suspend fun getAllWorkoutExercises(): List<WorkoutExerciseEntity>

  @Delete
  public suspend fun deleteWorkoutExercise(exercise: WorkoutExerciseEntity)
}
