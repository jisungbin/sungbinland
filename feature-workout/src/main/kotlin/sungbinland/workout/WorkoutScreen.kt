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
  val mainExerciseFocusRequester1 = remember { FocusRequester() }
  val mainExerciseFocusRequester2 = remember { FocusRequester() }
  val imeVisible: Boolean = WindowInsets.isImeVisible
  val wasImeVisibleState = remember { mutableStateOf(false) }
  // -1 = not editing, 0 = editing first field, 1 = editing second field
  val editingFieldIndexState = rememberSaveable { mutableStateOf(-1) }
  var editingFieldIndex by editingFieldIndexState
  val mainExerciseInput1State = rememberSaveable { mutableStateOf("") }
  var mainExerciseInput1 by mainExerciseInput1State
  val mainExerciseInput2State = rememberSaveable { mutableStateOf("") }
  var mainExerciseInput2 by mainExerciseInput2State
  val currentMainExercise1 = remember(state.summary.mainExerciseValue) {
    state.summary.mainExerciseValue.toMainExerciseInput()
  }
  val currentMainExercise2 = remember(state.summary.mainExerciseValue2) {
    state.summary.mainExerciseValue2.toMainExerciseInput()
  }
  val mainExerciseAutocomplete1 = remember(mainExerciseInput1, state.summary.mainExerciseSuggestions) {
    findMainExerciseSuggestion(
      input = mainExerciseInput1,
      suggestions = state.summary.mainExerciseSuggestions,
    )
  }
  val mainExerciseAutocomplete2 = remember(mainExerciseInput2, state.summary.mainExerciseSuggestions) {
    findMainExerciseSuggestion(
      input = mainExerciseInput2,
      suggestions = state.summary.mainExerciseSuggestions,
    )
  }
  val currentMainExercise1Ref = rememberUpdatedState(currentMainExercise1)
  val currentMainExercise2Ref = rememberUpdatedState(currentMainExercise2)
  val mainExerciseAutocomplete1Ref = rememberUpdatedState(mainExerciseAutocomplete1)
  val mainExerciseAutocomplete2Ref = rememberUpdatedState(mainExerciseAutocomplete2)
  val imeVisibleRef = rememberUpdatedState(imeVisible)
  LaunchedEffect(editingFieldIndexState) {
    snapshotFlow { currentMainExercise1Ref.value to editingFieldIndexState.value }.collect { (mainExercise, editIndex) ->
      if (editIndex != 0) {
        mainExerciseInput1State.value = mainExercise
      }
    }
  }
  LaunchedEffect(editingFieldIndexState) {
    snapshotFlow { currentMainExercise2Ref.value to editingFieldIndexState.value }.collect { (mainExercise, editIndex) ->
      if (editIndex != 1) {
        mainExerciseInput2State.value = mainExercise
      }
    }
  }
  LaunchedEffect(editingFieldIndexState) {
    snapshotFlow { editingFieldIndexState.value }.collect { editIndex ->
      when (editIndex) {
        0 -> {
          mainExerciseFocusRequester1.requestFocus()
          keyboardController?.show()
        }
        1 -> {
          mainExerciseFocusRequester2.requestFocus()
          keyboardController?.show()
        }
      }
    }
  }
  LaunchedEffect(editingFieldIndexState) {
    snapshotFlow { imeVisibleRef.value to editingFieldIndexState.value }.collect { (imeVis, editIndex) ->
      if (wasImeVisibleState.value && !imeVis && editIndex >= 0) {
        val nextMainExercise1 = if (editIndex == 0) {
          mainExerciseAutocomplete1Ref.value ?: mainExerciseInput1State.value
        } else {
          mainExerciseInput1State.value
        }
        val nextMainExercise2 = if (editIndex == 1) {
          mainExerciseAutocomplete2Ref.value ?: mainExerciseInput2State.value
        } else {
          mainExerciseInput2State.value
        }
        if (editIndex == 0) mainExerciseInput1State.value = nextMainExercise1
        if (editIndex == 1) mainExerciseInput2State.value = nextMainExercise2
        editingFieldIndexState.value = -1
        focusManager.clearFocus(force = true)
        viewModel.saveSession(
          routineName = state.summary.routineTitle,
          mainExerciseName = nextMainExercise1,
          mainExerciseName2 = nextMainExercise2,
        )
      }
      wasImeVisibleState.value = imeVis
    }
  }

  WorkoutScreen(
    state = state,
    mainExerciseInput1 = mainExerciseInput1,
    mainExerciseInput2 = mainExerciseInput2,
    mainExerciseAutocomplete1 = mainExerciseAutocomplete1,
    mainExerciseAutocomplete2 = mainExerciseAutocomplete2,
    editingMainExerciseIndex = editingFieldIndex,
    mainExerciseFocusRequester1 = mainExerciseFocusRequester1,
    mainExerciseFocusRequester2 = mainExerciseFocusRequester2,
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
    onMainExerciseClick = { index -> editingFieldIndex = index },
    onMainExerciseInputChange = { index, input ->
      when (index) {
        0 -> mainExerciseInput1 = input
        1 -> mainExerciseInput2 = input
      }
    },
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
  mainExerciseInput1: String,
  mainExerciseInput2: String,
  mainExerciseAutocomplete1: String?,
  mainExerciseAutocomplete2: String?,
  editingMainExerciseIndex: Int,
  mainExerciseFocusRequester1: FocusRequester,
  mainExerciseFocusRequester2: FocusRequester,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onSupplementIncrement: (String) -> Unit,
  onSupplementDecrement: (String) -> Unit,
  onMainExerciseClick: (Int) -> Unit,
  onMainExerciseInputChange: (Int, String) -> Unit,
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
      mainExerciseInput1 = mainExerciseInput1,
      mainExerciseInput2 = mainExerciseInput2,
      mainExerciseAutocomplete1 = mainExerciseAutocomplete1,
      mainExerciseAutocomplete2 = mainExerciseAutocomplete2,
      editingMainExerciseIndex = editingMainExerciseIndex,
      mainExerciseFocusRequester1 = mainExerciseFocusRequester1,
      mainExerciseFocusRequester2 = mainExerciseFocusRequester2,
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
