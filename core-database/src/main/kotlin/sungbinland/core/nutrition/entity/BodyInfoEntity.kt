package sungbinland.core.nutrition.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko
import java.util.Date

@Entity(tableName = "body_infos")
@Immutable
@Poko public class BodyInfoEntity(
  @PrimaryKey @ColumnInfo(name = "recorded_at") public val recordedAt: Date,
  @ColumnInfo(name = "body_weight_kg") public val bodyWeightKg: Int,
)
