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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitMetricCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun NutritionMacroRow(
  items: List<NutritionMacroCardState>,
  weightInput: String,
  isEditingWeight: Boolean,
  weightFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onWeightCardClick: () -> Unit,
  onWeightInputChange: (String) -> Unit,
) {
  val defaultItems = mutableListOf<NutritionMacroCardState>()
  var weightItem: NutritionMacroCardState? = null
  items.fastForEach { item ->
    if (item.highlighted) {
      weightItem = item
    } else {
      defaultItems.add(item)
    }
  }

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
        state = weightItem,
        weightInput = weightInput,
        isEditing = isEditingWeight,
        focusRequester = weightFocusRequester,
        modifier = Modifier.weight(1f),
        onClick = onWeightCardClick,
        onWeightInputChange = onWeightInputChange,
      )
    }
  }
}

@Composable private fun NutritionWeightInputCard(
  state: NutritionMacroCardState,
  weightInput: String,
  isEditing: Boolean,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  onWeightInputChange: (String) -> Unit,
) {
  val titleColor: Color = Color(0xCCFFFFFF)
  val valueColor: Color = Color.White
  val metaColor: Color = Color(0xE0FFFFFF)

  Column(
    modifier = modifier
      .background(
        color = UiKitColors.Accent,
        shape = RoundedCornerShape(16.dp),
      )
      .border(
        width = 1.dp,
        color = UiKitColors.Accent,
        shape = RoundedCornerShape(16.dp),
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    BasicText(
      text = state.title,
      style = UiKitTypography.Micro.copy(color = titleColor),
    )
    if (isEditing) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      ) {
        BasicTextField(
          value = weightInput,
          onValueChange = { input ->
            onWeightInputChange(input.filter(Char::isDigit).take(3))
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
        )
        BasicText(
          text = "kg",
          maxLines = 1,
          style = UiKitTypography.Value.copy(color = valueColor),
        )
      }
    } else {
      BasicText(
        text = if (weightInput.isBlank()) "--kg" else "${weightInput}kg",
        style = UiKitTypography.TitleLarge.copy(color = valueColor),
      )
    }
    BasicText(
      text = state.meta,
      style = UiKitTypography.Micro.copy(color = metaColor),
    )
  }
}
