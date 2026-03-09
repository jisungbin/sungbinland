package sungbinland.core.study.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import dev.drewhamilton.poko.Poko

@Entity(
  tableName = "study_entries",
  primaryKeys = ["category", "name"],
)
@Immutable
@Poko public class StudyEntryEntity(
  @ColumnInfo(name = "category") public val category: String,
  @ColumnInfo(name = "name") public val name: String,
  @ColumnInfo(name = "content") public val content: String,
)
