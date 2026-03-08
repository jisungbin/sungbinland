package sungbinland.core.workout.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import sungbinland.core.workout.entity.SupplementEntity

@Dao public interface SupplementDao {
  @Upsert
  public suspend fun upsertSupplement(supplement: SupplementEntity)

  @Query("SELECT * FROM supplements ORDER BY name ASC")
  public suspend fun getAllSupplements(): List<SupplementEntity>

  @Delete
  public suspend fun deleteSupplement(supplement: SupplementEntity)
}
