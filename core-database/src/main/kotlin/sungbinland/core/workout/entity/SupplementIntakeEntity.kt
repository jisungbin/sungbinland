package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko
import java.util.Date

@Entity(tableName = "supplement_intakes")
@Immutable
@Poko public class SupplementIntakeEntity(
  @PrimaryKey @ColumnInfo(name = "intake_at") public val intakeAt: Date,
)
