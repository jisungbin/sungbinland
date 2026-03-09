package sungbinland.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import sungbinland.uikit.UiKitColors

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun NutritionScreen(
  stateHolder: NutritionStateHolder,
  modifier: Modifier = Modifier,
) {
  val state by stateHolder.state.collectAsState()
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val weightFocusRequester = remember { FocusRequester() }
  val imeVisible: Boolean = WindowInsets.isImeVisible
  val wasImeVisibleState = remember { mutableStateOf(false) }
  val isEditingWeightState = rememberSaveable { mutableStateOf(false) }
  var isEditingWeight by isEditingWeightState
  val weightInputState = rememberSaveable { mutableStateOf("") }
  var weightInput by weightInputState
  val currentWeightInput = remember(state.macroCards) {
    state.macroCards.firstOrNull { item -> item.highlighted }?.value?.toWeightInput().orEmpty()
  }
  val currentWeightInputRef = rememberUpdatedState(currentWeightInput)
  val imeVisibleRef = rememberUpdatedState(imeVisible)

  LaunchedEffect(isEditingWeightState) {
    snapshotFlow { currentWeightInputRef.value to isEditingWeightState.value }.collect { (currentWeight, isEditing) ->
      if (!isEditing) weightInputState.value = currentWeight
    }
  }
  LaunchedEffect(isEditingWeightState) {
    snapshotFlow { isEditingWeightState.value }.collect { isEditing ->
      if (isEditing) {
        weightFocusRequester.requestFocus()
        keyboardController?.show()
      }
    }
  }
  LaunchedEffect(isEditingWeightState) {
    snapshotFlow { imeVisibleRef.value to isEditingWeightState.value }.collect { (imeVis, isEditing) ->
      if (wasImeVisibleState.value && !imeVis && isEditing) {
        isEditingWeightState.value = false
        focusManager.clearFocus(force = true)
        stateHolder.saveBodyWeight(weightInput = weightInputState.value)
      }
      wasImeVisibleState.value = imeVis
    }
  }

  NutritionScreen(
    state = state,
    isEditingWeight = isEditingWeight,
    weightFocusRequester = weightFocusRequester,
    weightInput = weightInput,
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .verticalScroll(rememberScrollState())
      .systemBarsPadding()
      .padding(all = 16.dp),
    onNextDateClick = stateHolder::moveToNextDate,
    onCurrentDateClick = stateHolder::moveToToday,
    onOpenGraphClick = {},
    onPreviousDateClick = stateHolder::moveToPreviousDate,
    onWeightCardClick = { isEditingWeight = true },
    onWeightInputChange = { input -> weightInput = input },
  )
}

@Composable private fun NutritionScreen(
  state: NutritionDashboardState,
  weightInput: String,
  isEditingWeight: Boolean,
  weightFocusRequester: FocusRequester,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onOpenGraphClick: () -> Unit,
  onWeightCardClick: () -> Unit,
  onWeightInputChange: (String) -> Unit,
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
      weightInput = weightInput,
      isEditingWeight = isEditingWeight,
      weightFocusRequester = weightFocusRequester,
      onWeightCardClick = onWeightCardClick,
      onWeightInputChange = onWeightInputChange,
    )
    NutritionFoodTimelineSection(
      modifier = Modifier.fillMaxWidth(),
      state = state.timeline,
    )
  }
}

private fun String.toWeightInput(): String = removeSuffix("kg").filter(Char::isDigit)
