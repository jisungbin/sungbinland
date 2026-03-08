package sungbinland.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitDateNavigator
import sungbinland.uikit.UiKitDeltaBadge
import sungbinland.uikit.UiKitPillButton
import sungbinland.uikit.UiKitSurfaceCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutSummaryCard(
  state: WorkoutSummaryState,
  mainExerciseInput: String,
  maxWeightInput: String,
  isEditingMainExercise: Boolean,
  isEditingMaxWeight: Boolean,
  mainExerciseFocusRequester: FocusRequester,
  maxWeightFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onMainExerciseClick: () -> Unit,
  onMaxWeightClick: () -> Unit,
  onMainExerciseInputChange: (String) -> Unit,
  onMaxWeightInputChange: (String) -> Unit,
  onOpenRoutineDetailClick: () -> Unit,
  onOpenTrendClick: () -> Unit,
) {
  UiKitSurfaceCard(
    modifier = modifier.fillMaxWidth(),
    borderColor = UiKitColors.BorderStrong,
    verticalSpacing = 14.dp,
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
        modifier = Modifier,
        onClick = onOpenRoutineDetailClick,
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.Top,
    ) {
      WorkoutMetricField(
        label = "메인 종목",
        value = state.mainExerciseValue,
        inputValue = mainExerciseInput,
        isEditing = isEditingMainExercise,
        focusRequester = mainExerciseFocusRequester,
        suffix = null,
        keyboardType = KeyboardType.Text,
        modifier = Modifier.weight(1f),
        onClick = onMainExerciseClick,
        onInputChange = onMainExerciseInputChange,
      )
      WorkoutMetricField(
        label = "최고 중량",
        value = state.maxWeightValue,
        inputValue = maxWeightInput,
        isEditing = isEditingMaxWeight,
        focusRequester = maxWeightFocusRequester,
        suffix = "kg",
        keyboardType = KeyboardType.Number,
        modifier = Modifier.weight(0.5f),
        onClick = onMaxWeightClick,
        onInputChange = onMaxWeightInputChange,
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = "최근 무게 트렌드",
        style = UiKitTypography.Title.copy(color = UiKitColors.MutedText),
      )
      UiKitDeltaBadge(
        delta = state.trendDelta,
        meta = null,
        modifier = Modifier.align(Alignment.CenterVertically),
      )
    }
    WorkoutTrendValueBoxes(
      trendValues = state.trendValues,
      modifier = Modifier.fillMaxWidth(),
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      WorkoutTimerInfoPanel(
        firstTimerStartedAt = state.firstTimerStartedAt,
        lastTimerStartedAt = state.lastTimerStartedAt,
        timerSpan = state.timerSpan,
        modifier = Modifier.weight(1f),
      )
      UiKitPillButton(
        text = "전체 트렌드 확인",
        modifier = Modifier,
        onClick = onOpenTrendClick,
      )
    }
  }
}

@Composable private fun WorkoutTimerInfoPanel(
  firstTimerStartedAt: String,
  lastTimerStartedAt: String,
  timerSpan: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFFCFBF9))
      .border(
        width = 1.dp,
        color = UiKitColors.BorderSoft,
        shape = RoundedCornerShape(12.dp),
      )
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    WorkoutTimerInfoRow(
      label = "첫 시작",
      value = firstTimerStartedAt,
      modifier = Modifier.fillMaxWidth(),
    )
    WorkoutTimerInfoRow(
      label = "마지막 시작",
      value = lastTimerStartedAt,
      modifier = Modifier.fillMaxWidth(),
    )
    WorkoutTimerInfoRow(
      label = "운동 시간",
      value = timerSpan,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable private fun WorkoutTimerInfoRow(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = label,
      style = UiKitTypography.Micro.copy(color = UiKitColors.MutedText),
    )
    BasicText(
      text = value,
      style = UiKitTypography.Value.copy(
        color = UiKitColors.Primary,
        fontWeight = FontWeight.SemiBold,
      ),
    )
  }
}

@Composable private fun WorkoutTrendValueBoxes(
  trendValues: List<WorkoutTrendValueState>,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  LaunchedEffect(scrollState.maxValue) {
    scrollState.scrollTo(scrollState.maxValue)
  }
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFFAFAF8))
      .horizontalScroll(state = scrollState)
      .padding(10.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val previousDayIndex: Int = trendValues.lastIndex - 1
    val selectedDayIndex: Int = trendValues.lastIndex
    trendValues.fastForEachIndexed { index, trendValue ->
      val containerColor: Color = when (index) {
        selectedDayIndex -> UiKitColors.Accent
        previousDayIndex -> UiKitColors.Primary
        else -> UiKitColors.Background
      }
      val valueColor = if (index >= previousDayIndex) {
        UiKitColors.Surface
      } else {
        UiKitColors.Primary
      }
      val labelColor = if (index >= previousDayIndex) {
        UiKitColors.Surface
      } else {
        UiKitColors.MutedText
      }
      val showBorder: Boolean = index != selectedDayIndex && index != previousDayIndex
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(containerColor)
          .then(
            if (showBorder) {
              Modifier.border(
                width = 1.dp,
                color = UiKitColors.BorderSoft,
                shape = RoundedCornerShape(10.dp),
              )
            } else {
              Modifier
            },
          )
          .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        BasicText(
          text = trendValue.label,
          style = UiKitTypography.Micro.copy(color = labelColor),
        )
        BasicText(
          text = trendValue.value,
          style = UiKitTypography.Value.copy(color = valueColor),
        )
      }
    }
  }
}

@Composable private fun WorkoutMetricField(
  label: String,
  value: String,
  inputValue: String,
  isEditing: Boolean,
  focusRequester: FocusRequester,
  suffix: String?,
  keyboardType: KeyboardType,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  onInputChange: (String) -> Unit,
) {
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = inputValue,
        selection = TextRange(inputValue.length),
      ),
    )
  }

  LaunchedEffect(inputValue) {
    if (inputValue != textFieldValue.text) {
      textFieldValue = TextFieldValue(
        text = inputValue,
        selection = TextRange(inputValue.length),
      )
    }
  }
  LaunchedEffect(isEditing) {
    if (isEditing && textFieldValue.text.isNotEmpty()) {
      textFieldValue = textFieldValue.copy(
        selection = TextRange(0, textFieldValue.text.length),
      )
    }
  }

  val inputFieldModifier = if (suffix == null) {
    Modifier
      .focusRequester(focusRequester)
      .fillMaxWidth()
  } else {
    Modifier
      .focusRequester(focusRequester)
      .widthIn(min = 24.dp, max = 56.dp)
  }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    BasicText(
      text = label,
      style = UiKitTypography.Title.copy(color = UiKitColors.MutedText),
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          color = Color(0xFFFCFBF9),
          shape = RoundedCornerShape(14.dp),
        )
        .border(
          width = 1.dp,
          color = UiKitColors.BorderSoft,
          shape = RoundedCornerShape(14.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      if (isEditing) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BasicTextField(
            value = textFieldValue,
            onValueChange = { value ->
              textFieldValue = value
              onInputChange(value.text)
            },
            modifier = inputFieldModifier,
            singleLine = true,
            textStyle = UiKitTypography.Title.copy(
              color = UiKitColors.MutedText,
              fontWeight = FontWeight.Medium,
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = keyboardType,
              imeAction = ImeAction.Done,
            ),
          )
          if (suffix != null) {
            BasicText(
              text = suffix,
              maxLines = 1,
              style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
            )
          }
        }
      } else {
        BasicText(
          text = value,
          style = UiKitTypography.Title.copy(
            color = UiKitColors.MutedText,
            fontWeight = FontWeight.Medium,
          ),
        )
      }
    }
  }
}
