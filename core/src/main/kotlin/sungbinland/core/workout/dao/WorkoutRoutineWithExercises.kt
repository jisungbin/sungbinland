package sungbinland.core.workout.dao

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import dev.drewhamilton.poko.Poko
import sungbinland.core.workout.entity.WorkoutExerciseEntity
import sungbinland.core.workout.entity.WorkoutRoutineEntity

@Immutable
@Poko public class WorkoutRoutineWithExercises(
  @Embedded public val routine: WorkoutRoutineEntity,
  @Relation(
    parentColumn = "name",
    entityColumn = "routine_name",
  ) public val exercises: List<WorkoutExerciseEntity>,
)
