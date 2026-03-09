package sungbinland.core.workout.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.util.Date
import sungbinland.core.workout.entity.TimerRecordEntity

@Dao public interface TimerRecordDao {
  @Upsert
  public suspend fun upsertTimerRecord(record: TimerRecordEntity)

  @Query(
    "SELECT * FROM timer_records " +
      "WHERE started_at >= :startOfDay AND started_at < :endOfDayExclusive " +
      "ORDER BY started_at ASC",
  )
  public suspend fun getTimerRecordsByDate(
    startOfDay: Date,
    endOfDayExclusive: Date,
  ): List<TimerRecordEntity>

  @Query("SELECT * FROM timer_records WHERE started_at = :date")
  public suspend fun getTimerRecordsByExactDate(date: Date): List<TimerRecordEntity>

  @Delete
  public suspend fun deleteTimerRecord(record: TimerRecordEntity)
}
