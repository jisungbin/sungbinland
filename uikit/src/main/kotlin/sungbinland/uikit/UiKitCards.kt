package sungbinland.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable public fun UiKitSurfaceCard(
  modifier: Modifier = Modifier,
  backgroundColor: Color = UiKitColors.Surface,
  borderColor: Color = UiKitColors.Border,
  contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
  verticalSpacing: androidx.compose.ui.unit.Dp = 12.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(backgroundColor)
      .border(
        width = 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(16.dp),
      )
      .padding(contentPadding),
    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
  ) {
    content()
  }
}

@Composable public fun UiKitSectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  meta: String? = null,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = title,
      modifier = Modifier.align(Alignment.CenterVertically),
      style = UiKitTypography.Title.copy(color = UiKitColors.Text),
    )
    if (meta != null) {
      BasicText(
        text = meta,
        modifier = Modifier.align(Alignment.CenterVertically),
        style = UiKitTypography.Label.copy(color = UiKitColors.MutedText),
      )
    }
  }
}

@Composable public fun UiKitMetricCard(
  title: String,
  value: String,
  meta: String,
  highlighted: Boolean,
  modifier: Modifier = Modifier,
) {
  val containerColor: Color = if (highlighted) UiKitColors.Accent else UiKitColors.Surface
  val borderColor: Color = if (highlighted) UiKitColors.Accent else UiKitColors.Border
  val titleColor: Color = if (highlighted) Color(0xCCFFFFFF) else UiKitColors.MutedText
  val valueColor: Color = if (highlighted) Color.White else UiKitColors.Primary
  val metaColor: Color = if (highlighted) Color(0xE0FFFFFF) else UiKitColors.MutedText

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(containerColor)
      .border(
        width = 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(16.dp),
      )
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    BasicText(
      text = title,
      style = UiKitTypography.Micro.copy(
        color = titleColor,
        fontWeight = FontWeight.SemiBold,
      ),
    )
    BasicText(
      text = value,
      style = UiKitTypography.TitleLarge.copy(color = valueColor),
    )
    BasicText(
      text = meta,
      style = UiKitTypography.Micro.copy(color = metaColor),
    )
  }
}

@Composable public fun UiKitListCardRow(
  title: String,
  subtitle: String,
  trailing: String,
  modifier: Modifier = Modifier,
  titleStyle: TextStyle = UiKitTypography.Value.copy(
    color = UiKitColors.Text,
    fontWeight = FontWeight.Medium,
  ),
  trailingStyle: TextStyle = UiKitTypography.Value.copy(
    color = UiKitColors.BrandBlue,
    fontWeight = FontWeight.Medium,
  ),
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(UiKitColors.Surface)
      .border(
        width = 1.dp,
        color = UiKitColors.Border,
        shape = RoundedCornerShape(16.dp),
      )
      .padding(horizontal = 14.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      BasicText(text = title, style = titleStyle)
      BasicText(
        text = subtitle,
        style = UiKitTypography.Label.copy(color = UiKitColors.MutedText),
      )
    }
    BasicText(text = trailing, style = trailingStyle)
  }
}

@Composable public fun UiKitChecklistRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(UiKitColors.Surface)
      .border(
        width = 1.dp,
        color = UiKitColors.Border,
        shape = RoundedCornerShape(16.dp),
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      BasicText(
        text = title,
        style = UiKitTypography.Title.copy(
          color = UiKitColors.Text,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicText(
        text = subtitle,
        style = UiKitTypography.Value.copy(color = UiKitColors.MutedText),
      )
    }
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(if (checked) Color(0xFFFF9800) else Color.Transparent)
        .border(
          width = 1.dp,
          color = if (checked) Color(0xFFFF9800) else UiKitColors.BorderSoft,
          shape = RoundedCornerShape(12.dp),
        ),
      contentAlignment = Alignment.Center,
    ) {
      if (checked) {
        BasicText(
          text = "✓",
          style = UiKitTypography.Value.copy(color = Color.Black),
        )
      }
    }
  }
}
