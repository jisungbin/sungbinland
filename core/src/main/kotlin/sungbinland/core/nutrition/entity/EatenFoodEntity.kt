package sungbinland.core.nutrition.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko
import java.util.Date

@Entity(
  tableName = "eaten_foods",
  foreignKeys = [
    ForeignKey(
      entity = FoodEntity::class,
      parentColumns = ["name"],
      childColumns = ["food_name"],
    ),
  ],
  indices = [
    Index("food_name"),
    Index("consumed_at"),
  ],
)
@Immutable
@Poko public class EatenFoodEntity(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") public val id: Long = 0L,
  @ColumnInfo(name = "food_name") public val foodName: String,
  @ColumnInfo(name = "quantity") public val quantity: Int,
  @ColumnInfo(name = "consumed_at") public val consumedAt: Date,
)
