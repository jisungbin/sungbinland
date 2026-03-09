package sungbinland.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitTypography

@Composable internal fun WorkoutSupplementManagementSheet(
  supplements: ImmutableList<String>,
  modifier: Modifier = Modifier,
  onDelete: (name: String) -> Unit,
  onRegister: (name: String) -> Unit,
) {
  var nameInput by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      )
      .verticalScroll(rememberScrollState())
      .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    BasicText(
      text = "보충제 관리",
      style = TextStyle(
        color = Color(0xFF1C1C1C),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
      ),
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      BasicText(
        text = "등록된 보충제",
        style = TextStyle(
          color = Color(0xFF1C1C1C),
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      supplements.fastForEach { name ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BasicText(
            text = name,
            style = UiKitTypography.Value.copy(color = UiKitColors.Text),
          )
          BasicText(
            text = "✕",
            modifier = Modifier.clickable { onDelete(name) },
            style = TextStyle(
              color = Color(0xFFE85A4F),
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color(0xFFEEEEEE)),
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      BasicText(
        text = "이름",
        style = TextStyle(
          color = Color(0xFF1C1C1C),
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        singleLine = true,
        textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
          .padding(horizontal = 14.dp),
        decorationBox = { innerTextField ->
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (nameInput.isEmpty()) {
              BasicText(
                text = "보충제 이름을 입력해주세요",
                style = UiKitTypography.Value.copy(color = Color(0xFF999999)),
              )
            }
            innerTextField()
          }
        },
      )
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF1E3A5F))
        .clickable {
          if (nameInput.isNotBlank()) {
            onRegister(nameInput)
            nameInput = ""
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      BasicText(
        text = "등록하기",
        style = TextStyle(
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
  }
}
