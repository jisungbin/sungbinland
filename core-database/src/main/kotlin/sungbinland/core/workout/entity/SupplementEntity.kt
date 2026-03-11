package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko

@Entity(tableName = "supplements")
@Immutable
@Poko public class SupplementEntity(
  @PrimaryKey @ColumnInfo(name = "name") public val name: String,
  @ColumnInfo(name = "target_intake_count") public val targetIntakeCount: Int = 1,
)
