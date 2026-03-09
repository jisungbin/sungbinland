package sungbinland.nutrition

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal enum class NutritionGraphPeriod(internal val label: String, internal val days: Long) {
  WEEK("1주", 7),
  MONTH("1개월", 30),
  THREE_MONTHS("3개월", 90),
  SIX_MONTHS("6개월", 180),
  YEAR("1년", 365),
}

@Immutable
@Poko internal class NutritionGraphState(
  internal val selectedPeriod: NutritionGraphPeriod,
  internal val chart: NutritionGraphChartState,
  internal val stats: ImmutableList<NutritionGraphStatCardState>,
  internal val detailRows: ImmutableList<NutritionGraphDetailRowState>,
)

@Immutable
@Poko internal class NutritionGraphChartState(
  internal val weightPoints: ImmutableList<Float>,
  internal val caloriesPoints: ImmutableList<Float>,
  internal val carbsPoints: ImmutableList<Float>,
  internal val yAxisLabels: ImmutableList<String>,
  internal val xAxisLabels: ImmutableList<String>,
)

@Immutable
@Poko internal class NutritionGraphStatCardState(
  internal val name: String,
  internal val accentColor: Long,
  internal val averageValue: String,
  internal val rangeText: String,
  internal val deltaArrow: String,
  internal val deltaText: String,
  internal val deltaPositive: Boolean,
)

@Immutable
@Poko internal class NutritionGraphDetailRowState(
  internal val name: String,
  internal val dotColor: Long,
  internal val average: String,
  internal val min: String,
  internal val max: String,
)

internal val nutritionGraphLoadingState = NutritionGraphState(
  selectedPeriod = NutritionGraphPeriod.THREE_MONTHS,
  chart = NutritionGraphChartState(
    weightPoints = persistentListOf(),
    caloriesPoints = persistentListOf(),
    carbsPoints = persistentListOf(),
    yAxisLabels = persistentListOf(),
    xAxisLabels = persistentListOf(),
  ),
  stats = persistentListOf(),
  detailRows = persistentListOf(),
)
