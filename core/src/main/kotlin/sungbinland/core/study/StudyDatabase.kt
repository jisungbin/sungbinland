package sungbinland.core.study

import android.content.Context
import androidx.annotation.NonUiContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.study.entity.StudyEntryEntity

@Database(
  entities = [StudyEntryEntity::class],
  version = 1,
  exportSchema = true,
)
public abstract class StudyDatabase internal constructor() : RoomDatabase() {
  public abstract fun studyEntryDao(): StudyEntryDao

  public companion object {
    private const val DATABASE_NAME: String = "study.db"
    @Volatile private var instance: StudyDatabase? = null

    public fun getOrCreate(@NonUiContext context: Context): StudyDatabase =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          StudyDatabase::class.java,
          DATABASE_NAME,
        )
          .build()
          .also { created -> instance = created }
      }
  }
}
