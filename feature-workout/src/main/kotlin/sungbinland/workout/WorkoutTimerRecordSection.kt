package sungbinland.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import sungbinland.uikit.IbmPlexSansKr
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutTimerRecordSection(
  timerRecords: ImmutableList<String>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    BasicText(
      text = "타이머 기록",
      style = UiKitTypography.Title.copy(color = UiKitColors.Text),
    )
    timerRecords.fastForEach { time ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White, RoundedCornerShape(12.dp))
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(UiKitColors.BrandBlue, CircleShape),
        )
        BasicText(
          text = time,
          style = TextStyle(
            fontFamily = IbmPlexSansKr,
            color = UiKitColors.Text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
          ),
        )
      }
    }
  }
}
