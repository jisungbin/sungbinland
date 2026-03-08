package sungbinland.core.nutrition.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.drewhamilton.poko.Poko

@Entity(tableName = "foods")
@Immutable
@Poko public class FoodEntity(
  @PrimaryKey @ColumnInfo(name = "name") public val name: String,
  @ColumnInfo(name = "calories") public val calories: Int,
  @ColumnInfo(name = "protein_grams") public val proteinGrams: Int,
  @ColumnInfo(name = "carbohydrate_grams") public val carbohydrateGrams: Int,
)
