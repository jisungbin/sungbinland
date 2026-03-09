package sungbinland.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.drewhamilton.poko.Poko
import kotlinx.collections.immutable.ImmutableList

@Composable public fun UiKitSearchField(
  value: String,
  placeholder: String,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit,
) {
  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
    modifier = modifier
      .background(
        color = Color(0xFFFCFBF9),
        shape = RoundedCornerShape(14.dp),
      )
      .border(
        width = 1.dp,
        color = UiKitColors.BorderSoft,
        shape = RoundedCornerShape(14.dp),
      )
      .padding(horizontal = 14.dp, vertical = 10.dp),
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        BasicText(
          text = "⌕",
          style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
        )
        Box(
          modifier = Modifier,
        ) {
          if (value.isEmpty()) {
            BasicText(
              text = placeholder,
              style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
            )
          }
          innerTextField()
        }
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable public fun UiKitCategoryChipRow(
  chips: ImmutableList<UiKitChipState>,
  modifier: Modifier = Modifier,
  onChipClick: (String) -> Unit,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    chips.fastForEach { chip ->
      UiKitCategoryChip(
        state = chip,
        modifier = Modifier,
        onClick = { onChipClick(chip.id) },
      )
    }
  }
}

@Composable public fun UiKitCategoryChip(
  state: UiKitChipState,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val containerColor: Color = if (state.selected) UiKitColors.Accent else Color(0xFFFCFBF9)
  val textColor: Color = if (state.selected) Color.White else UiKitColors.MutedTextStrong

  BasicText(
    text = state.label,
    modifier = modifier
      .background(
        color = containerColor,
        shape = RoundedCornerShape(14.dp),
      )
      .border(
        width = 1.dp,
        color = if (state.selected) UiKitColors.Accent else UiKitColors.BorderSoft,
        shape = RoundedCornerShape(14.dp),
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    style = TextStyle(
      color = textColor,
      fontSize = UiKitTypography.Value.fontSize,
      fontWeight = FontWeight.SemiBold,
    ),
  )
}

@Immutable
@Poko public class UiKitChipState(
  public val id: String,
  public val label: String,
  public val selected: Boolean,
)
