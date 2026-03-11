package sungbinland.core.workout

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v7→v8: Add target_intake_count to supplements; add intake_count to supplement_intake_items */
internal val WorkoutMigration7To8 = object : Migration(7, 8) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `supplements` ADD COLUMN `target_intake_count` INTEGER NOT NULL DEFAULT 1")
    db.execSQL("ALTER TABLE `supplement_intake_items` ADD COLUMN `intake_count` INTEGER NOT NULL DEFAULT 1")
  }
}
