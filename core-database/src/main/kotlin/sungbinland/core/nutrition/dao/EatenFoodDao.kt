package sungbinland.core.nutrition.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.util.Date
import sungbinland.core.nutrition.entity.EatenFoodEntity

@Dao public interface EatenFoodDao {
  @Upsert
  public suspend fun upsertEatenFood(food: EatenFoodEntity)

  @Query(
    "SELECT * FROM eaten_foods " +
      "WHERE consumed_at >= :startOfDay AND consumed_at < :endOfDayExclusive " +
      "ORDER BY consumed_at ASC",
  )
  public suspend fun getEatenFoodsByDate(
    startOfDay: Date,
    endOfDayExclusive: Date,
  ): List<EatenFoodEntity>

  @Query("SELECT * FROM eaten_foods WHERE consumed_at = :date")
  public suspend fun getEatenFoodsByExactDate(date: Date): List<EatenFoodEntity>

  @Delete
  public suspend fun deleteEatenFood(food: EatenFoodEntity)
}
