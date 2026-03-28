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

/** v8→v9: Add second main exercise name to workout_sessions */
internal val WorkoutMigration8To9 = object : Migration(8, 9) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `workout_sessions` ADD COLUMN `main_exercise_name_2` TEXT NOT NULL DEFAULT ''")
  }
}

/** v9→v10: Drop supplement tables */
internal val WorkoutMigration9To10 = object : Migration(9, 10) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("DROP TABLE IF EXISTS `supplement_intake_items`")
    db.execSQL("DROP TABLE IF EXISTS `supplement_intakes`")
    db.execSQL("DROP TABLE IF EXISTS `supplements`")
  }
}
