package sungbinland.core.workout.dao

import androidx.compose.ui.util.fastFilter
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.util.Date
import sungbinland.core.workout.entity.SupplementIntakeEntity
import sungbinland.core.workout.entity.SupplementIntakeItemEntity

@Dao public abstract class SupplementIntakeDao {
  @Upsert
  public abstract suspend fun upsertSupplementIntake(intake: SupplementIntakeEntity)

  @Upsert
  public abstract suspend fun upsertSupplementIntakeItems(items: List<SupplementIntakeItemEntity>)

  @Transaction
  public open suspend fun upsertIntake(
    intake: SupplementIntakeEntity,
    items: List<SupplementIntakeItemEntity>,
  ) {
    upsertSupplementIntake(intake)
    deleteSupplementIntakeItemsByDate(intake.intakeAt)
    upsertSupplementIntakeItems(items.fastFilter { item -> item.intakeAt == intake.intakeAt })
  }

  @Transaction
  @Query(
    "SELECT * FROM supplement_intakes " +
      "WHERE intake_at >= :startOfDay AND intake_at < :endOfDayExclusive " +
      "ORDER BY intake_at ASC",
  )
  public abstract suspend fun getSupplementIntakesByDate(
    startOfDay: Date,
    endOfDayExclusive: Date,
  ): List<SupplementIntakeWithItems>

  @Transaction
  @Query("SELECT * FROM supplement_intakes WHERE intake_at = :date")
  public abstract suspend fun getSupplementIntakeByExactDate(date: Date): SupplementIntakeWithItems?

  @Query("DELETE FROM supplement_intake_items WHERE intake_at = :intakeAt")
  public abstract suspend fun deleteSupplementIntakeItemsByDate(intakeAt: Date)

  @Delete
  public abstract suspend fun deleteSupplementIntake(intake: SupplementIntakeEntity)
}
