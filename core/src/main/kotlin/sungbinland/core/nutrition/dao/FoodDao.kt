package sungbinland.core.nutrition.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import sungbinland.core.nutrition.entity.FoodEntity

@Dao public interface FoodDao {
  @Upsert
  public suspend fun upsertFood(food: FoodEntity)

  @Query("SELECT * FROM foods ORDER BY name ASC")
  public suspend fun getAllFoods(): List<FoodEntity>

  @Delete
  public suspend fun deleteFood(food: FoodEntity)
}
