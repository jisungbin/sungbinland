package sungbinland.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import sungbinland.uikit.UiKitColors

@Composable internal fun NutritionScreen(
  viewModel: NutritionViewModel,
  onOpenGraphClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()
  var isEditingWeight by rememberSaveable { mutableStateOf(false) }
  var weightEditText by rememberSaveable { mutableStateOf("") }
  val stateWeight = state.macroCards.fastFirstOrNull { it.highlighted }?.value?.toWeightDigits().orEmpty()

  NutritionScreen(
    state = state,
    isEditingWeight = isEditingWeight,
    weightDisplayText = when {
      isEditingWeight -> weightEditText
      stateWeight.isNotBlank() -> stateWeight
      else -> ""
    },
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .verticalScroll(rememberScrollState())
      .systemBarsPadding()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
    onNextDateClick = viewModel::moveToNextDate,
    onCurrentDateClick = viewModel::moveToToday,
    onOpenGraphClick = onOpenGraphClick,
    onPreviousDateClick = viewModel::moveToPreviousDate,
    onWeightCardClick = {
      weightEditText = stateWeight
      isEditingWeight = true
    },
    onWeightInputChange = { weightEditText = it },
    onWeightDone = {
      isEditingWeight = false
      viewModel.saveBodyWeight(weightEditText)
    },
  )
}

@Composable private fun NutritionScreen(
  state: NutritionDashboardState,
  isEditingWeight: Boolean,
  weightDisplayText: String,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onOpenGraphClick: () -> Unit,
  onWeightCardClick: () -> Unit,
  onWeightInputChange: (String) -> Unit,
  onWeightDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    NutritionSummaryCard(
      modifier = Modifier.fillMaxWidth(),
      state = state.summary,
      onNextDateClick = onNextDateClick,
      onCurrentDateClick = onCurrentDateClick,
      onOpenGraphClick = onOpenGraphClick,
      onPreviousDateClick = onPreviousDateClick,
    )
    NutritionMacroRow(
      modifier = Modifier.fillMaxWidth(),
      items = state.macroCards,
      weightDisplayText = weightDisplayText,
      isEditingWeight = isEditingWeight,
      onWeightCardClick = onWeightCardClick,
      onWeightInputChange = onWeightInputChange,
      onWeightDone = onWeightDone,
    )
    NutritionFoodTimelineSection(
      modifier = Modifier.fillMaxWidth(),
      state = state.timeline,
    )
  }
}

private fun String.toWeightDigits(): String = removeSuffix("kg").filter(Char::isDigit)
