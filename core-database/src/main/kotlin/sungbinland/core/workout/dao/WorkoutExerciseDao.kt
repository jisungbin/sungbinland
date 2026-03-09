package sungbinland.core.workout.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import sungbinland.core.workout.entity.WorkoutExerciseEntity

@Dao public interface WorkoutExerciseDao {
  @Query(
    "SELECT * FROM workout_exercises ORDER BY name ASC",
  )
  public suspend fun getAllWorkoutExercises(): List<WorkoutExerciseEntity>

  @Upsert
  public suspend fun upsertWorkoutExercise(exercise: WorkoutExerciseEntity)

  @Delete
  public suspend fun deleteWorkoutExercise(exercise: WorkoutExerciseEntity)
}
