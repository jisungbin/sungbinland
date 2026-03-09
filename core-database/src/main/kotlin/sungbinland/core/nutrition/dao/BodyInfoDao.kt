package sungbinland.core.nutrition.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.util.Date
import sungbinland.core.nutrition.entity.BodyInfoEntity

@Dao public interface BodyInfoDao {
  @Upsert
  public suspend fun upsertBodyInfo(bodyInfo: BodyInfoEntity)

  @Query(
    "SELECT * FROM body_infos " +
      "WHERE recorded_at >= :startOfDay AND recorded_at < :endOfDayExclusive " +
      "ORDER BY recorded_at ASC",
  )
  public suspend fun getBodyInfosByDate(
    startOfDay: Date,
    endOfDayExclusive: Date,
  ): List<BodyInfoEntity>

  @Query("SELECT * FROM body_infos WHERE recorded_at = :date LIMIT 1")
  public suspend fun getBodyInfoByExactDate(date: Date): BodyInfoEntity?

  @Delete
  public suspend fun deleteBodyInfo(bodyInfo: BodyInfoEntity)
}
