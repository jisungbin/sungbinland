package sungbinland.core.workout.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko
import java.util.Date

@Entity(
  tableName = "timer_records",
  indices = [
    Index(value = ["started_at"]),
  ],
)
@Immutable
@Poko public class TimerRecordEntity(
  @PrimaryKey @ColumnInfo(name = "started_at") public val startedAt: Date,
)
