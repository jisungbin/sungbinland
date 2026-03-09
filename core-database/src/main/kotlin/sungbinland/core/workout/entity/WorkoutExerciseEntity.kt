package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko

@Entity(
  tableName = "workout_exercises",
  indices = [
    Index("routine_name"),
  ],
)
@Immutable
@Poko public class WorkoutExerciseEntity(
  @PrimaryKey @ColumnInfo(name = "name") public val name: String,
  @ColumnInfo(name = "routine_name") public val routineName: String,
)
