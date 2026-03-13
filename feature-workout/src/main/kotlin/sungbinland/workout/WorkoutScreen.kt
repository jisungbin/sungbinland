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
import android.app.AlertDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sungbinland.uikit.UiKitColors

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun WorkoutScreen(
  viewModel: WorkoutViewModel,
  onOpenRoutineDetailClick: () -> Unit,
  onManageSupplementClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val context = LocalContext.current
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
  val mainExerciseAutocomplete = remember(mainExerciseInput, state.summary.mainExerciseSuggestions) {
    findMainExerciseSuggestion(
      input = mainExerciseInput,
      suggestions = state.summary.mainExerciseSuggestions,
    )
  }
  val currentMainExerciseRef = rememberUpdatedState(currentMainExercise)
  val mainExerciseAutocompleteRef = rememberUpdatedState(mainExerciseAutocomplete)
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
        val nextMainExercise = mainExerciseAutocompleteRef.value ?: mainExerciseInputState.value
        mainExerciseInputState.value = nextMainExercise
        isEditingState.value = false
        focusManager.clearFocus(force = true)
        viewModel.saveSession(
          routineName = state.summary.routineTitle,
          mainExerciseName = nextMainExercise,
        )
      }
      wasImeVisibleState.value = imeVis
    }
  }

  WorkoutScreen(
    state = state,
    mainExerciseInput = mainExerciseInput,
    mainExerciseAutocomplete = mainExerciseAutocomplete,
    isEditingMainExercise = isEditing,
    mainExerciseFocusRequester = mainExerciseFocusRequester,
    modifier = modifier
      .fillMaxSize()
      .background(UiKitColors.Background)
      .verticalScroll(rememberScrollState())
      .systemBarsPadding()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
    onNextDateClick = viewModel::moveToNextDate,
    onCurrentDateClick = viewModel::moveToToday,
    onPreviousDateClick = viewModel::moveToPreviousDate,
    onSupplementIncrement = viewModel::incrementSupplement,
    onSupplementDecrement = viewModel::decrementSupplement,
    onMainExerciseClick = { isEditing = true },
    onMainExerciseInputChange = { input -> mainExerciseInput = input },
    onOpenRoutineDetailClick = onOpenRoutineDetailClick,
    onRoutineSelect = viewModel::selectRoutine,
    onManageSupplementClick = onManageSupplementClick,
    onResetTimerRecords = {
      AlertDialog.Builder(context)
        .setTitle("기록 초기화")
        .setMessage("오늘의 타이머 기록을 모두 삭제하시겠습니까?")
        .setPositiveButton("삭제") { _, _ ->
          viewModel.clearTodayTimerRecords()
        }
        .setNegativeButton("취소", null)
        .show()
    },
  )
}

@Composable private fun WorkoutScreen(
  state: WorkoutDashboardState,
  mainExerciseInput: String,
  mainExerciseAutocomplete: String?,
  isEditingMainExercise: Boolean,
  mainExerciseFocusRequester: FocusRequester,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onSupplementIncrement: (String) -> Unit,
  onSupplementDecrement: (String) -> Unit,
  onMainExerciseClick: () -> Unit,
  onMainExerciseInputChange: (String) -> Unit,
  onOpenRoutineDetailClick: () -> Unit,
  onRoutineSelect: (String) -> Unit,
  onManageSupplementClick: () -> Unit,
  onResetTimerRecords: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    WorkoutSummaryCard(
      state = state.summary,
      mainExerciseInput = mainExerciseInput,
      mainExerciseAutocomplete = mainExerciseAutocomplete,
      isEditingMainExercise = isEditingMainExercise,
      mainExerciseFocusRequester = mainExerciseFocusRequester,
      modifier = Modifier.fillMaxWidth(),
      onPreviousDateClick = onPreviousDateClick,
      onNextDateClick = onNextDateClick,
      onCurrentDateClick = onCurrentDateClick,
      onMainExerciseClick = onMainExerciseClick,
      onMainExerciseInputChange = onMainExerciseInputChange,
      onOpenRoutineDetailClick = onOpenRoutineDetailClick,
      onRoutineSelect = onRoutineSelect,
      onResetTimerRecords = onResetTimerRecords,
    )
    WorkoutSupplementChecklistSection(
      state = state.supplements,
      modifier = Modifier.fillMaxWidth(),
      onIncrement = onSupplementIncrement,
      onDecrement = onSupplementDecrement,
      onManageSupplementClick = onManageSupplementClick,
    )
  }
}

private fun String.toMainExerciseInput(): String =
  if (this == "[...]") "" else this

private fun findMainExerciseSuggestion(input: String, suggestions: List<String>): String? {
  val normalizedInput = input.trim()
  if (normalizedInput.isEmpty()) return null
  val suggestion = suggestions.firstOrNull { candidate ->
    candidate.startsWith(normalizedInput, ignoreCase = true)
  } ?: return null
  return if (suggestion.equals(normalizedInput, ignoreCase = true)) null else suggestion
}
