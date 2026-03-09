package sungbinland.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitDateNavigator
import sungbinland.uikit.UiKitPillButton
import sungbinland.uikit.UiKitSurfaceCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutSummaryCard(
  state: WorkoutSummaryState,
  mainExerciseInput: String,
  isEditingMainExercise: Boolean,
  mainExerciseFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onMainExerciseClick: () -> Unit,
  onMainExerciseInputChange: (String) -> Unit,
  onOpenRoutineDetailClick: () -> Unit,
) {
  UiKitSurfaceCard(
    modifier = modifier.fillMaxWidth(),
    borderColor = UiKitColors.BorderStrong,
    verticalSpacing = 15.dp,
  ) {
    UiKitDateNavigator(
      dayTag = state.dayTag,
      displayDate = state.displayDate,
      modifier = Modifier.fillMaxWidth(),
      onPreviousClick = onPreviousDateClick,
      onNextClick = onNextDateClick,
      onCurrentDateClick = onCurrentDateClick,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = state.routineTitle,
        style = UiKitTypography.Headline.copy(color = UiKitColors.Primary),
      )
      UiKitPillButton(
        text = "루틴 상세",
        onClick = onOpenRoutineDetailClick,
      )
    }
    WorkoutMainExerciseField(
      value = state.mainExerciseValue,
      inputValue = mainExerciseInput,
      isEditing = isEditingMainExercise,
      focusRequester = mainExerciseFocusRequester,
      modifier = Modifier.fillMaxWidth(),
      onClick = onMainExerciseClick,
      onInputChange = onMainExerciseInputChange,
    )
    WorkoutTimerInfoRow(
      firstTimerStartedAt = state.firstTimerStartedAt,
      lastTimerStartedAt = state.lastTimerStartedAt,
      timerSpan = state.timerSpan,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable private fun WorkoutMainExerciseField(
  value: String,
  inputValue: String,
  isEditing: Boolean,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  onInputChange: (String) -> Unit,
) {
  val currentInputValue = rememberUpdatedState(inputValue)
  val currentIsEditing = rememberUpdatedState(isEditing)
  val textFieldValueState = remember {
    mutableStateOf(
      TextFieldValue(
        text = inputValue,
        selection = TextRange(inputValue.length),
      ),
    )
  }
  var textFieldValue by textFieldValueState

  LaunchedEffect(textFieldValueState) {
    snapshotFlow { currentInputValue.value }.collect { input ->
      if (input != textFieldValueState.value.text) {
        textFieldValueState.value = TextFieldValue(
          text = input,
          selection = TextRange(input.length),
        )
      }
    }
  }
  LaunchedEffect(textFieldValueState) {
    snapshotFlow { currentIsEditing.value }.collect { editing ->
      if (editing && textFieldValueState.value.text.isNotEmpty()) {
        textFieldValueState.value = textFieldValueState.value.copy(
          selection = TextRange(0, textFieldValueState.value.text.length),
        )
      }
    }
  }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    BasicText(
      text = "메인 종목",
      style = UiKitTypography.Title.copy(color = UiKitColors.MutedText),
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .background(
          color = Color(0xFFFCFBF9),
          shape = RoundedCornerShape(10.dp),
        )
        .border(
          width = 1.dp,
          color = Color(0xFFE8E8E8),
          shape = RoundedCornerShape(10.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      if (isEditing) {
        BasicTextField(
          value = textFieldValue,
          onValueChange = { fieldValue ->
            textFieldValue = fieldValue
            onInputChange(fieldValue.text)
          },
          modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
          singleLine = true,
          textStyle = TextStyle(
            color = UiKitColors.Primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
      } else {
        BasicText(
          text = value,
          style = TextStyle(
            color = when (value) {
              "[...]" -> Color(0xFF9A9A9A)
              else -> UiKitColors.Primary
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          ),
        )
      }
    }
  }
}

@Composable private fun WorkoutTimerInfoRow(
  firstTimerStartedAt: WorkoutTimerValueState,
  lastTimerStartedAt: WorkoutTimerValueState,
  timerSpan: WorkoutTimerValueState,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier) {
    WorkoutTimerColumn(
      label = "첫 시작",
      value = firstTimerStartedAt,
      modifier = Modifier.weight(1f),
    )
    WorkoutTimerColumn(
      label = "마지막 시작",
      value = lastTimerStartedAt,
      modifier = Modifier.weight(1f),
    )
    WorkoutTimerColumn(
      label = "운동 시간",
      value = timerSpan,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable private fun WorkoutTimerColumn(
  label: String,
  value: WorkoutTimerValueState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    BasicText(
      text = label,
      style = TextStyle(
        color = Color(0xFF8A8A8A),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
      ),
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      BasicText(
        text = value.hours,
        style = TextStyle(
          color = UiKitColors.Primary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
        ),
      )
      BasicText(
        text = "h",
        style = TextStyle(
          color = Color(0xFF8A8A8A),
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
      BasicText(
        text = value.minutes,
        style = TextStyle(
          color = UiKitColors.Primary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
        ),
      )
      BasicText(
        text = "m",
        style = TextStyle(
          color = Color(0xFF8A8A8A),
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
  }
}
