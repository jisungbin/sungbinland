package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko

@Entity(tableName = "workout_routines")
@Immutable
@Poko public class WorkoutRoutineEntity(
  @PrimaryKey @ColumnInfo(name = "name") public val name: String,
)
