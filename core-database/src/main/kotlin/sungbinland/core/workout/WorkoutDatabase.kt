package sungbinland.core.workout

import android.content.Context
import androidx.annotation.NonUiContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sungbinland.core.converter.DateEpochMillisTypeConverter
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutExerciseDao
import sungbinland.core.workout.dao.WorkoutRoutineDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.core.workout.entity.TimerRecordEntity
import sungbinland.core.workout.entity.SupplementEntity
import sungbinland.core.workout.entity.SupplementIntakeEntity
import sungbinland.core.workout.entity.SupplementIntakeItemEntity
import sungbinland.core.workout.entity.WorkoutExerciseEntity
import sungbinland.core.workout.entity.WorkoutRoutineEntity
import sungbinland.core.workout.entity.WorkoutSessionEntity

@Database(
  entities = [
    WorkoutSessionEntity::class,
    TimerRecordEntity::class,
    WorkoutRoutineEntity::class,
    WorkoutExerciseEntity::class,
    SupplementEntity::class,
    SupplementIntakeEntity::class,
    SupplementIntakeItemEntity::class,
  ],
  version = 5,
  exportSchema = true,
)
@TypeConverters(DateEpochMillisTypeConverter::class)
public abstract class WorkoutDatabase internal constructor() : RoomDatabase() {
  public abstract fun workoutRoutineDao(): WorkoutRoutineDao
  public abstract fun workoutExerciseDao(): WorkoutExerciseDao
  public abstract fun supplementDao(): SupplementDao
  public abstract fun workoutSessionDao(): WorkoutSessionDao
  public abstract fun timerRecordDao(): TimerRecordDao
  public abstract fun supplementIntakeDao(): SupplementIntakeDao

  public companion object {
    private const val DATABASE_NAME: String = "workout.db"
    @Volatile private var instance: WorkoutDatabase? = null

    public fun getOrCreate(@NonUiContext context: Context): WorkoutDatabase =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          WorkoutDatabase::class.java,
          DATABASE_NAME,
        )
          .fallbackToDestructiveMigration(true)
          .build()
          .also { created -> instance = created }
      }
  }
}
