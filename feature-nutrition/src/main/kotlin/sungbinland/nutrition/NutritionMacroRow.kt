package sungbinland.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitMetricCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun NutritionMacroRow(
  items: ImmutableList<NutritionMacroCardState>,
  weightDisplayText: String,
  isEditingWeight: Boolean,
  modifier: Modifier = Modifier,
  onWeightCardClick: () -> Unit,
  onWeightInputChange: (String) -> Unit,
  onWeightDone: () -> Unit,
) {
  val defaultItems = items.fastFilter { !it.highlighted }
  val weightItem = items.fastFirstOrNull { it.highlighted }

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    defaultItems.fastForEach { item ->
      UiKitMetricCard(
        title = item.title,
        value = item.value,
        meta = item.meta,
        highlighted = item.highlighted,
        modifier = Modifier.weight(1f),
      )
    }
    if (weightItem != null) {
      NutritionWeightInputCard(
        title = weightItem.title,
        meta = weightItem.meta,
        displayText = weightDisplayText,
        isEditing = isEditingWeight,
        modifier = Modifier.weight(1f),
        onClick = onWeightCardClick,
        onInputChange = onWeightInputChange,
        onDone = onWeightDone,
      )
    }
  }
}

@Composable private fun NutritionWeightInputCard(
  title: String,
  meta: String,
  displayText: String,
  isEditing: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  onInputChange: (String) -> Unit,
  onDone: () -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val valueColor = Color.White

  LaunchedEffect(isEditing) {
    if (isEditing) {
      focusRequester.requestFocus()
      keyboardController?.show()
    }
  }

  Column(
    modifier = modifier
      .background(color = UiKitColors.Accent, shape = RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    BasicText(
      text = title,
      style = UiKitTypography.Micro.copy(color = Color(0xCCFFFFFF)),
    )
    if (isEditing) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        BasicTextField(
          value = displayText,
          onValueChange = { value ->
            onInputChange(value.filter(Char::isDigit).take(3))
          },
          modifier = Modifier
            .focusRequester(focusRequester)
            .widthIn(min = 24.dp, max = 56.dp),
          singleLine = true,
          textStyle = UiKitTypography.TitleLarge.copy(color = valueColor),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
          ),
          keyboardActions = KeyboardActions(onDone = { onDone() }),
        )
        BasicText(
          text = "kg",
          maxLines = 1,
          style = UiKitTypography.Value.copy(color = valueColor),
        )
      }
    } else {
      BasicText(
        text = when {
          displayText.isBlank() -> "--kg"
          else -> "${displayText}kg"
        },
        style = UiKitTypography.TitleLarge.copy(color = valueColor),
      )
    }
    BasicText(
      text = meta,
      style = UiKitTypography.Micro.copy(color = Color(0xE0FFFFFF)),
    )
  }
}
