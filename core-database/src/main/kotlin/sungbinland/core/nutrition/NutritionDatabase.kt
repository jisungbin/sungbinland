package sungbinland.core.nutrition

import android.content.Context
import androidx.annotation.NonUiContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sungbinland.core.converter.DateEpochMillisTypeConverter
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.nutrition.entity.BodyInfoEntity
import sungbinland.core.nutrition.entity.EatenFoodEntity
import sungbinland.core.nutrition.entity.FoodEntity

@Database(
  entities = [FoodEntity::class, EatenFoodEntity::class, BodyInfoEntity::class],
  version = 1,
  exportSchema = true,
)
@TypeConverters(DateEpochMillisTypeConverter::class)
public abstract class NutritionDatabase internal constructor() : RoomDatabase() {
  public abstract fun foodDao(): FoodDao
  public abstract fun eatenFoodDao(): EatenFoodDao
  public abstract fun bodyInfoDao(): BodyInfoDao

  public companion object {
    private const val DATABASE_NAME: String = "nutrition.db"
    @Volatile private var instance: NutritionDatabase? = null

    public fun getOrCreate(@NonUiContext context: Context): NutritionDatabase =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          NutritionDatabase::class.java,
          DATABASE_NAME,
        )
          .build()
          .also { created -> instance = created }
      }
  }
}
