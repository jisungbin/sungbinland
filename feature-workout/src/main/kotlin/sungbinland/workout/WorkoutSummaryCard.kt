package sungbinland.workout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Popup
import sungbinland.uikit.IbmPlexSansKr
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitDateNavigator
import sungbinland.uikit.UiKitPillButton
import sungbinland.uikit.UiKitSurfaceCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutSummaryCard(
  state: WorkoutSummaryState,
  mainExerciseInput1: String,
  mainExerciseInput2: String,
  mainExerciseAutocomplete1: String?,
  mainExerciseAutocomplete2: String?,
  editingMainExerciseIndex: Int,
  mainExerciseFocusRequester1: FocusRequester,
  mainExerciseFocusRequester2: FocusRequester,
  modifier: Modifier = Modifier,
  onPreviousDateClick: () -> Unit,
  onNextDateClick: () -> Unit,
  onCurrentDateClick: () -> Unit,
  onMainExerciseClick: (Int) -> Unit,
  onMainExerciseInputChange: (Int, String) -> Unit,
  onOpenRoutineDetailClick: () -> Unit,
  onRoutineSelect: (String) -> Unit,
  onResetTimerRecords: () -> Unit,
) {
  var showRoutinePopup by remember { mutableStateOf(false) }
  UiKitSurfaceCard(
    modifier = modifier
      .fillMaxWidth()
      .pointerInput(Unit) {
        var totalDragOffset = 0f
        detectHorizontalDragGestures(
          onDragStart = { totalDragOffset = 0f },
          onDragEnd = {
            val threshold = 40.dp.toPx()
            when {
              totalDragOffset > threshold -> onPreviousDateClick()
              totalDragOffset < -threshold -> onNextDateClick()
            }
          },
          onDragCancel = { totalDragOffset = 0f },
          onHorizontalDrag = { _, dragAmount -> totalDragOffset += dragAmount },
        )
      },
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
      Box {
        BasicText(
          text = "${state.routineTitle} ▾",
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = state.routineNames.isNotEmpty()) {
              showRoutinePopup = true
            }
            .padding(vertical = 4.dp, horizontal = 2.dp),
          style = UiKitTypography.Headline.copy(color = UiKitColors.Primary),
        )
        if (showRoutinePopup) {
          Popup(
            alignment = Alignment.TopStart,
            onDismissRequest = { showRoutinePopup = false },
          ) {
            Column(
              modifier = Modifier
                .widthIn(min = 160.dp, max = 220.dp)
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState())
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
              state.routineNames.fastForEach { name ->
                BasicText(
                  text = name,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      onRoutineSelect(name)
                      showRoutinePopup = false
                    }
                    .background(if (name == state.routineTitle) Color(0xFFF0F4FF) else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                  style = TextStyle(
                    fontFamily = IbmPlexSansKr,
                    color = if (name == state.routineTitle) UiKitColors.BrandBlue else UiKitColors.Primary,
                    fontSize = 15.sp,
                    fontWeight = if (name == state.routineTitle) FontWeight.SemiBold else FontWeight.Normal,
                  ),
                )
              }
            }
          }
        }
      }
      UiKitPillButton(
        text = "루틴 상세",
        onClick = onOpenRoutineDetailClick,
      )
    }
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      BasicText(
        text = "메인 종목",
        style = UiKitTypography.Title.copy(color = UiKitColors.MutedText),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        WorkoutMainExerciseField(
          value = state.mainExerciseValue,
          inputValue = mainExerciseInput1,
          autocompleteValue = mainExerciseAutocomplete1,
          isEditing = editingMainExerciseIndex == 0,
          focusRequester = mainExerciseFocusRequester1,
          modifier = Modifier.weight(1f),
          onClick = { onMainExerciseClick(0) },
          onInputChange = { onMainExerciseInputChange(0, it) },
        )
        WorkoutMainExerciseField(
          value = state.mainExerciseValue2,
          inputValue = mainExerciseInput2,
          autocompleteValue = mainExerciseAutocomplete2,
          isEditing = editingMainExerciseIndex == 1,
          focusRequester = mainExerciseFocusRequester2,
          modifier = Modifier.weight(1f),
          onClick = { onMainExerciseClick(1) },
          onInputChange = { onMainExerciseInputChange(1, it) },
        )
      }
    }
    WorkoutTimerInfoRow(
      firstTimerStartedAt = state.firstTimerStartedAt,
      lastTimerStartedAt = state.lastTimerStartedAt,
      timerSpan = state.timerSpan,
      modifier = Modifier.fillMaxWidth(),
      onFirstTimerLongClick = onResetTimerRecords,
    )
  }
}

@Composable private fun WorkoutMainExerciseField(
  value: String,
  inputValue: String,
  autocompleteValue: String?,
  isEditing: Boolean,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  onInputChange: (String) -> Unit,
) {
  val currentInputValue = rememberUpdatedState(inputValue)
  val currentAutocompleteValue = rememberUpdatedState(autocompleteValue)
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

  Box(
    modifier = modifier
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
        Box(modifier = Modifier.fillMaxWidth()) {
          if (!currentAutocompleteValue.value.isNullOrEmpty()) {
            BasicText(
              text = currentAutocompleteValue.value.orEmpty(),
              style = TextStyle(
                fontFamily = IbmPlexSansKr,
                color = Color(0xFFBDBDBD),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
              ),
            )
          }
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
              fontFamily = IbmPlexSansKr,
              color = UiKitColors.Primary,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          )
        }
      } else {
        BasicText(
          text = value,
          style = TextStyle(
            fontFamily = IbmPlexSansKr,
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

@Composable private fun WorkoutTimerInfoRow(
  firstTimerStartedAt: WorkoutTimerValueState,
  lastTimerStartedAt: WorkoutTimerValueState,
  timerSpan: WorkoutTimerValueState,
  modifier: Modifier = Modifier,
  onFirstTimerLongClick: () -> Unit,
) {
  Row(modifier = modifier) {
    WorkoutTimerColumn(
      label = "첫 시작",
      value = firstTimerStartedAt,
      modifier = Modifier.weight(1f),
      onLongClick = onFirstTimerLongClick,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable private fun WorkoutTimerColumn(
  label: String,
  value: WorkoutTimerValueState,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier.then(
      if (onLongClick != null) Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick)
      else Modifier,
    ),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    BasicText(
      text = label,
      style = TextStyle(
        fontFamily = IbmPlexSansKr,
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
          fontFamily = IbmPlexSansKr,
          color = UiKitColors.Primary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
        ),
      )
      BasicText(
        text = "h",
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = Color(0xFF8A8A8A),
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
      BasicText(
        text = value.minutes,
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = UiKitColors.Primary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
        ),
      )
      BasicText(
        text = "m",
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = Color(0xFF8A8A8A),
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
  }
}
