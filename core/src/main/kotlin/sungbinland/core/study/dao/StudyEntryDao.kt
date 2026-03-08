package sungbinland.core.study.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import sungbinland.core.study.entity.StudyEntryEntity

@Dao public interface StudyEntryDao {
  @Upsert
  public suspend fun upsertStudyEntry(entry: StudyEntryEntity)

  @Query("SELECT * FROM study_entries ORDER BY category ASC, name ASC")
  public suspend fun getAllStudyEntries(): List<StudyEntryEntity>

  @Delete
  public suspend fun deleteStudyEntry(entry: StudyEntryEntity)
}
