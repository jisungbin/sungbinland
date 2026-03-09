package sungbinland.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import sungbinland.uikit.UiKitColors

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun WorkoutScreen(
  stateHolder: WorkoutStateHolder,
  showTimerSheet: Boolean,
  onDismissTimerSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by stateHolder.state.collectAsState()
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val mainExerciseFocusRequester = remember { FocusRequester() }
  val imeVisible: Boolean = WindowInsets.isImeVisible
  val wasImeVisibleState = remember { mutableStateOf(false) }
  val isEditingState = rememberSaveable { mutableStateOf(false) }
  var isEditing by isEditingState
  val mainExerciseInputState = rememberSaveable { mutableStateOf("") }
  var mainExerciseInput by mainExerciseInputState
  val currentMainExercise = remember(state.summary.mainExerciseValue) {
    state.summary.mainExerciseValue.toMainExerciseInput()
  }
  val currentMainExerciseRef = rememberUpdatedState(currentMainExercise)
  val imeVisibleRef = rememberUpdatedState(imeVisible)
  LaunchedEffect(isEditingState) {
    snapshotFlow { currentMainExerciseRef.value to isEditingState.value }.collect { (mainExercise, editing) ->
      if (!editing) {
        mainExerciseInputState.value = mainExercise
      }
    }
  }
  LaunchedEffect(isEditingState) {
    snapshotFlow { isEditingState.value }.collect { editing ->
      if (editing) {
        mainExerciseFocusRequester.requestFocus()
        keyboardController?.show()
      }
    }
  }
  LaunchedEffect(isEditingState) {
    snapshotFlow { imeVisibleRef.value to isEditingState.value }.collect { (imeVis, editing) ->
      if (wasImeVisibleState.value && !imeVis && editing) {
        isEditingState.value = false
        focusManager.clearFocus(force = true)
        stateHolder.saveSession(
          routineName = state.summary.routineTitle,
          mainExerciseName = mainExerciseInputState.value,
        )
      }
      wasImeVisibleState.value = imeVis
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    WorkoutScreen(
      state = state,
      mainExerciseInput = mainExerciseInput,
      isEditingMainExercise = isEditing,
      mainExerciseFocusRequester = mainExerciseFocusRequester,
      modifier = Modifier
        .fillMaxSize()
        .background(UiKitColors.Background)
        .systemBarsPadding()
        .padding(all = 16.dp),
      onNextDateClick = stateHolder::moveToNextDate,
      onCurrentDateClick = stateHolder::moveToToday,
      onPreviousDateClick = stateHolder::moveToPreviousDate,
      onSupplementClick = stateHolder::toggleSupplement,
      onMainExerciseClick = { isEditing = true },
      onMainExerciseInputChange = { input -> mainExerciseInput = input },
    )
    if (showTimerSheet) {
      Popup(properties = PopupProperties(focusable = true)) {
        WorkoutTimerSheet(
          visible = true,
          onDismiss = onDismissTimerSheet,
          onStart = stateHolder::startTimer,
        )
      }
    }
  }
}

@Composable private fun WorkoutScreen(
  state: WorkoutDashboardState,
  mainExerciseInput: String,
  isEditingMainExercise: Boolean,
  mainExerciseFocusRequester: FocusRequester,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onSupplementClick: (String) -> Unit,
  onMainExerciseClick: () -> Unit,
  onMainExerciseInputChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 92.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    WorkoutSummaryCard(
      state = state.summary,
      mainExerciseInput = mainExerciseInput,
      isEditingMainExercise = isEditingMainExercise,
      mainExerciseFocusRequester = mainExerciseFocusRequester,
      modifier = Modifier.fillMaxWidth(),
      onPreviousDateClick = onPreviousDateClick,
      onNextDateClick = onNextDateClick,
      onCurrentDateClick = onCurrentDateClick,
      onMainExerciseClick = onMainExerciseClick,
      onMainExerciseInputChange = onMainExerciseInputChange,
      onOpenRoutineDetailClick = {},
    )
    WorkoutSupplementChecklistSection(
      state = state.supplements,
      modifier = Modifier.fillMaxWidth(),
      onItemClick = onSupplementClick,
      onManageSupplementClick = {},
    )
  }
}

private fun String.toMainExerciseInput(): String =
  if (this == "[...]") "" else this
