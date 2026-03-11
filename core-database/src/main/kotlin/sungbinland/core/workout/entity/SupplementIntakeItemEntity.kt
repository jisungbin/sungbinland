package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import dev.drewhamilton.poko.Poko
import java.util.Date

@Entity(
  tableName = "supplement_intake_items",
  primaryKeys = ["intake_at", "supplement_name"],
  indices = [
    Index("supplement_name"),
  ],
)
@Immutable
@Poko public class SupplementIntakeItemEntity(
  @ColumnInfo(name = "intake_at") public val intakeAt: Date,
  @ColumnInfo(name = "supplement_name") public val supplementName: String,
  @ColumnInfo(name = "intake_count") public val intakeCount: Int = 1,
)
