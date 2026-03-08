package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko
import java.util.Date

@Entity(tableName = "workout_sessions")
@Immutable
@Poko public class WorkoutSessionEntity(
  @ColumnInfo(name = "routine_name") public val routineName: String,
  @ColumnInfo(name = "main_exercise_name") public val mainExerciseName: String,
  @ColumnInfo(name = "heaviest_weight_kg") public val heaviestWeightKg: Int,
  @PrimaryKey @ColumnInfo(name = "performed_at") public val performedAt: Date,
)
