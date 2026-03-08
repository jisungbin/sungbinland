package sungbinland.workout

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import sungbinland.uikit.UiKitColors

@OptIn(ExperimentalLayoutApi::class) @Composable internal fun WorkoutScreen(
  stateHolder: WorkoutStateHolder,
  modifier: Modifier = Modifier,
) {
  val state by stateHolder.state.collectAsState()
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val mainExerciseFocusRequester = remember { FocusRequester() }
  val maxWeightFocusRequester = remember { FocusRequester() }
  val imeVisible: Boolean = WindowInsets.isImeVisible
  var wasImeVisible by remember { mutableStateOf(false) }
  var mainExerciseInput by rememberSaveable { mutableStateOf("") }
  var maxWeightInput by rememberSaveable { mutableStateOf("") }
  var editingField by rememberSaveable { mutableStateOf<WorkoutEditingField?>(null) }
  val currentMainExercise = remember(state.summary.mainExerciseValue) {
    state.summary.mainExerciseValue.toMainExerciseInput()
  }
  val currentMaxWeight = remember(state.summary.maxWeightValue) {
    state.summary.maxWeightValue.toWeightInput()
  }

  LaunchedEffect(currentMainExercise, currentMaxWeight) {
    if (editingField == null) {
      mainExerciseInput = currentMainExercise
      maxWeightInput = currentMaxWeight
    }
  }
  LaunchedEffect(editingField) {
    when (editingField) {
      WorkoutEditingField.MainExercise -> {
        mainExerciseFocusRequester.requestFocus()
        keyboardController?.show()
      }

      WorkoutEditingField.MaxWeight -> {
        maxWeightFocusRequester.requestFocus()
        keyboardController?.show()
      }

      null -> Unit
    }
  }
  LaunchedEffect(imeVisible, editingField, mainExerciseInput, maxWeightInput) {
    if (wasImeVisible && !imeVisible && editingField != null) {
      editingField = null
      focusManager.clearFocus(force = true)
      stateHolder.saveSession(
        routineName = state.summary.routineTitle,
        mainExerciseName = mainExerciseInput,
        heaviestWeightInput = maxWeightInput,
      )
    }
    wasImeVisible = imeVisible
  }

  WorkoutScreen(
    state = state,
    mainExerciseInput = mainExerciseInput,
    maxWeightInput = maxWeightInput,
    editingField = editingField,
    mainExerciseFocusRequester = mainExerciseFocusRequester,
    maxWeightFocusRequester = maxWeightFocusRequester,
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .systemBarsPadding()
      .padding(all = 16.dp),
    onNextDateClick = stateHolder::moveToNextDate,
    onCurrentDateClick = stateHolder::moveToToday,
    onPreviousDateClick = stateHolder::moveToPreviousDate,
    onSupplementClick = stateHolder::toggleSupplement,
    onMainExerciseClick = {
      editingField = WorkoutEditingField.MainExercise
    },
    onMainExerciseInputChange = { input ->
      mainExerciseInput = input
    },
    onMaxWeightClick = {
      editingField = WorkoutEditingField.MaxWeight
    },
    onMaxWeightInputChange = { input ->
      maxWeightInput = input.filter(Char::isDigit).take(3)
    },
  )
}

@Composable private fun WorkoutScreen(
  state: WorkoutDashboardState,
  mainExerciseInput: String,
  maxWeightInput: String,
  editingField: WorkoutEditingField?,
  mainExerciseFocusRequester: FocusRequester,
  maxWeightFocusRequester: FocusRequester,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onSupplementClick: (String) -> Unit,
  onMainExerciseClick: () -> Unit,
  onMainExerciseInputChange: (String) -> Unit,
  onMaxWeightClick: () -> Unit,
  onMaxWeightInputChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    WorkoutSummaryCard(
      state = state.summary,
      mainExerciseInput = mainExerciseInput,
      maxWeightInput = maxWeightInput,
      isEditingMainExercise = editingField == WorkoutEditingField.MainExercise,
      isEditingMaxWeight = editingField == WorkoutEditingField.MaxWeight,
      mainExerciseFocusRequester = mainExerciseFocusRequester,
      maxWeightFocusRequester = maxWeightFocusRequester,
      modifier = Modifier.fillMaxWidth(),
      onPreviousDateClick = onPreviousDateClick,
      onNextDateClick = onNextDateClick,
      onCurrentDateClick = onCurrentDateClick,
      onMainExerciseClick = onMainExerciseClick,
      onMaxWeightClick = onMaxWeightClick,
      onMainExerciseInputChange = onMainExerciseInputChange,
      onMaxWeightInputChange = onMaxWeightInputChange,
      onOpenRoutineDetailClick = {},
      onOpenTrendClick = {},
    )
    WorkoutSupplementChecklistSection(
      state = state.supplements,
      modifier = Modifier.fillMaxWidth(),
      onItemClick = onSupplementClick,
      onManageSupplementClick = {},
    )
  }
}

private enum class WorkoutEditingField {
  MainExercise,
  MaxWeight,
}

private fun String.toMainExerciseInput(): String =
  if (this == "[...]") "" else this

private fun String.toWeightInput(): String =
  removeSuffix("kg").filter(Char::isDigit)
