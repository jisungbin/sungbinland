package sungbinland.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitTypography

@Composable internal fun StudyEntryEditSheet(
  initialCategory: String,
  initialName: String,
  initialContent: String,
  modifier: Modifier = Modifier,
  onSubmit: (
    category: String,
    name: String,
    content: String,
  ) -> Unit,
) {
  var categoryInput by remember { mutableStateOf(initialCategory) }
  var nameInput by remember { mutableStateOf(initialName) }
  var contentInput by remember { mutableStateOf(initialContent) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      )
      .verticalScroll(rememberScrollState())
      .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    BasicText(
      text = "자료 수정",
      style = UiKitTypography.TitleLarge.copy(color = UiKitColors.Text),
    )
    EditSheetField(
      label = "카테고리",
      value = categoryInput,
      placeholder = "카테고리를 입력하세요",
      singleLine = true,
      height = 44,
      onValueChange = { categoryInput = it },
    )
    EditSheetField(
      label = "이름",
      value = nameInput,
      placeholder = "자료 이름을 입력하세요",
      singleLine = true,
      height = 44,
      onValueChange = { nameInput = it },
    )
    EditSheetField(
      label = "콘텐츠",
      value = contentInput,
      placeholder = "콘텐츠 내용을 입력하세요",
      singleLine = false,
      height = 88,
      onValueChange = { contentInput = it },
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(UiKitColors.BrandBlue)
        .clickable {
          if (categoryInput.isBlank() || nameInput.isBlank()) return@clickable
          onSubmit(categoryInput, nameInput, contentInput)
        },
      contentAlignment = Alignment.Center,
    ) {
      BasicText(
        text = "수정하기",
        style = TextStyle(
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
  }
}

@Composable private fun EditSheetField(
  label: String,
  value: String,
  placeholder: String,
  singleLine: Boolean,
  height: Int,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    BasicText(
      text = label,
      style = UiKitTypography.Value.copy(
        color = UiKitColors.Text,
        fontWeight = FontWeight.SemiBold,
      ),
    )
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = singleLine,
      textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
      modifier = Modifier
        .fillMaxWidth()
        .height(height.dp)
        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
        .padding(horizontal = 14.dp, vertical = 12.dp),
      decorationBox = { innerTextField ->
        Box(
          modifier = if (singleLine) Modifier.fillMaxSize() else Modifier,
          contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
        ) {
          if (value.isEmpty()) {
            BasicText(
              text = placeholder,
              style = UiKitTypography.Value.copy(color = Color(0xFFBBBBBB)),
            )
          }
          innerTextField()
        }
      },
    )
  }
}
