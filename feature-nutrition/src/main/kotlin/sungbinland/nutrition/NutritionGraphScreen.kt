package sungbinland.nutrition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import sungbinland.uikit.UiKitColors

@Composable internal fun NutritionGraphScreen(
  viewModel: NutritionGraphViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  NutritionGraphScreen(
    state = state,
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .systemBarsPadding(),
    onBack = onBack,
    onPeriodClick = viewModel::selectPeriod,
  )
}

@Composable private fun NutritionGraphScreen(
  state: NutritionGraphState,
  onBack: () -> Unit,
  onPeriodClick: (NutritionGraphPeriod) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    NutritionGraphNavBar(
      onBack = onBack,
      modifier = Modifier.fillMaxWidth(),
    )
    NutritionGraphPeriodRow(
      selectedPeriod = state.selectedPeriod,
      onPeriodClick = onPeriodClick,
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
    )
    NutritionGraphChartCard(
      chart = state.chart,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
    )
    Column(
      modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      NutritionGraphLegendRow(modifier = Modifier.fillMaxWidth())
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(Color(0xFFEFEFEF)),
      )
      NutritionGraphSummaryHeader(
        periodLabel = state.selectedPeriod.label,
        modifier = Modifier.fillMaxWidth(),
      )
      NutritionGraphStatCardsRow(
        stats = state.stats,
        modifier = Modifier.fillMaxWidth(),
      )
      NutritionGraphDetailTable(
        rows = state.detailRows,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable private fun NutritionGraphNavBar(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .height(48.dp)
      .padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = "‹",
      modifier = Modifier
        .size(24.dp)
        .clickable(onClick = onBack),
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
      ),
    )
    BasicText(
      text = "전체 그래프",
      modifier = Modifier.weight(1f),
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
      ),
    )
    Spacer(modifier = Modifier.size(24.dp))
  }
}

@Composable private fun NutritionGraphPeriodRow(
  selectedPeriod: NutritionGraphPeriod,
  onPeriodClick: (NutritionGraphPeriod) -> Unit,
  modifier: Modifier = Modifier,
) {
  val periods = NutritionGraphPeriod.entries
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    periods.fastForEach { period ->
      val selected = period == selectedPeriod
      BasicText(
        text = period.label,
        modifier = Modifier
          .background(
            color = when {
              selected -> Color(0xFF1E3A5F)
              else -> Color(0xFFF5F4F2)
            },
            shape = RoundedCornerShape(20.dp),
          )
          .clickable { onPeriodClick(period) }
          .padding(horizontal = 16.dp, vertical = 8.dp),
        style = TextStyle(
          color = when {
            selected -> Color.White
            else -> Color(0xFF8A8A8A)
          },
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
  }
}

@Composable private fun NutritionGraphChartCard(
  chart: NutritionGraphChartState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .background(Color.White, RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(16.dp))
      .padding(horizontal = 16.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Column(
        modifier = Modifier.height(280.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End,
      ) {
        chart.yAxisLabels.fastForEach { label ->
          BasicText(
            text = label,
            style = TextStyle(
              color = Color(0xFF9A9A9A),
              fontSize = 10.sp,
              fontWeight = FontWeight.Normal,
            ),
          )
        }
      }
      NutritionGraphCanvas(
        weightPoints = chart.weightPoints,
        caloriesPoints = chart.caloriesPoints,
        carbsPoints = chart.carbsPoints,
        modifier = Modifier
          .weight(1f)
          .height(280.dp),
      )
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 38.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      chart.xAxisLabels.fastForEach { label ->
        BasicText(
          text = label,
          style = TextStyle(
            color = Color(0xFF9A9A9A),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
          ),
        )
      }
    }
  }
}

@Composable private fun NutritionGraphCanvas(
  weightPoints: ImmutableList<Float>,
  caloriesPoints: ImmutableList<Float>,
  carbsPoints: ImmutableList<Float>,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height
    val gridColor = Color(0xFFF0F0F0)

    drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1f)
    drawLine(gridColor, Offset(0f, height / 2f), Offset(width, height / 2f), strokeWidth = 1f)
    drawLine(gridColor, Offset(0f, height), Offset(width, height), strokeWidth = 1f)

    val stroke = Stroke(
      width = 2.5.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round,
    )

    fun drawLinePath(points: List<Float>, color: Color) {
      if (points.size < 2) return
      val validPoints = points.mapIndexedNotNull { index, value ->
        if (value.isNaN()) null else index to value
      }
      if (validPoints.size < 2) return
      val path = Path()
      val stepX = width / (points.size - 1).coerceAtLeast(1)
      validPoints.forEachIndexed { i, (index, value) ->
        val x = index * stepX
        val y = height * (1f - value)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      drawPath(path = path, color = color, style = stroke)
    }

    drawLinePath(weightPoints, Color(0xFFE85A4F))
    drawLinePath(caloriesPoints, Color(0xFF1E3A5F))
    drawLinePath(carbsPoints, Color(0xFF20B07A))
  }
}

@Composable private fun NutritionGraphLegendRow(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    NutritionGraphLegendItem(color = Color(0xFFE85A4F), label = "체중")
    NutritionGraphLegendItem(color = Color(0xFF1E3A5F), label = "칼로리")
    NutritionGraphLegendItem(color = Color(0xFF20B07A), label = "탄수화물")
  }
}

@Composable private fun NutritionGraphLegendItem(
  color: Color,
  label: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .background(color, CircleShape),
    )
    BasicText(
      text = label,
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      ),
    )
  }
}

@Composable private fun NutritionGraphSummaryHeader(
  periodLabel: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = "기간 요약",
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
      ),
    )
    BasicText(
      text = periodLabel,
      style = TextStyle(
        color = Color(0xFF9A9A9A),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      ),
    )
  }
}

@Composable private fun NutritionGraphStatCardsRow(
  stats: ImmutableList<NutritionGraphStatCardState>,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    stats.fastForEach { stat ->
      NutritionGraphStatCard(
        stat = stat,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable private fun NutritionGraphStatCard(
  stat: NutritionGraphStatCardState,
  modifier: Modifier = Modifier,
) {
  val accentColor = Color(stat.accentColor)
  Column(
    modifier = modifier
      .background(Color.White, RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(16.dp))
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .width(3.dp)
          .height(14.dp)
          .background(accentColor, RoundedCornerShape(2.dp)),
      )
      BasicText(
        text = stat.name,
        style = TextStyle(
          color = Color(0xFF8A8A8A),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
    BasicText(
      text = stat.averageValue,
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      BasicText(
        text = stat.rangeText,
        style = TextStyle(
          color = Color(0xFF9A9A9A),
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
      val deltaColor = when {
        stat.deltaPositive -> Color(0xFF2E7D32)
        else -> Color(0xFFE85A4F)
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        BasicText(
          text = stat.deltaArrow,
          style = TextStyle(
            color = deltaColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
          ),
        )
        BasicText(
          text = stat.deltaText,
          style = TextStyle(
            color = deltaColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
          ),
        )
      }
    }
  }
}

@Composable private fun NutritionGraphDetailTable(
  rows: ImmutableList<NutritionGraphDetailRowState>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White)
      .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(16.dp)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFFFAFAF8))
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = "항목",
        modifier = Modifier.weight(1f),
        style = TextStyle(
          color = Color(0xFF9A9A9A),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicText(
        text = "평균",
        modifier = Modifier.width(60.dp),
        style = TextStyle(
          color = Color(0xFF9A9A9A),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.End,
        ),
      )
      BasicText(
        text = "최저",
        modifier = Modifier.width(60.dp),
        style = TextStyle(
          color = Color(0xFF9A9A9A),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.End,
        ),
      )
      BasicText(
        text = "최고",
        modifier = Modifier.width(60.dp),
        style = TextStyle(
          color = Color(0xFF9A9A9A),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.End,
        ),
      )
    }
    rows.fastForEachIndexed { index, row ->
      if (index > 0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF5F4F2)),
        )
      }
      NutritionGraphDetailRow(
        row = row,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable private fun NutritionGraphDetailRow(
  row: NutritionGraphDetailRowState,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(Color(row.dotColor), CircleShape),
      )
      BasicText(
        text = row.name,
        style = TextStyle(
          color = Color(0xFF1C1C1C),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
    BasicText(
      text = row.average,
      modifier = Modifier.width(60.dp),
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.End,
      ),
    )
    BasicText(
      text = row.min,
      modifier = Modifier.width(60.dp),
      style = TextStyle(
        color = Color(0xFF666666),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.End,
      ),
    )
    BasicText(
      text = row.max,
      modifier = Modifier.width(60.dp),
      style = TextStyle(
        color = Color(0xFF666666),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.End,
      ),
    )
  }
}
