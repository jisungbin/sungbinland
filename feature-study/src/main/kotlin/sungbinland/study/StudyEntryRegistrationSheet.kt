package sungbinland.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

private val StudyAccent: Color = Color(0xFFE85A4F)

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun StudyEntryRegistrationSheet(
  categories: ImmutableList<String>,
  modifier: Modifier = Modifier,
  onSubmit: (
    category: String,
    name: String,
    content: String,
  ) -> Unit,
) {
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var isCustomCategory by remember { mutableStateOf(false) }
  var customCategoryInput by remember { mutableStateOf("") }
  var nameInput by remember { mutableStateOf("") }
  var contentInput by remember { mutableStateOf("") }

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
      text = "자료 등록",
      style = UiKitTypography.TitleLarge.copy(color = UiKitColors.Text),
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      BasicText(
        text = "카테고리",
        style = UiKitTypography.Value.copy(
          color = UiKitColors.Text,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        categories.fastForEach { category ->
          StudyCategoryChip(
            text = category,
            selected = selectedCategory == category && !isCustomCategory,
            onClick = {
              selectedCategory = category
              isCustomCategory = false
            },
          )
        }
        StudyCategoryChip(
          text = "+ 직접 입력",
          selected = isCustomCategory,
          isAdd = true,
          onClick = {
            selectedCategory = null
            isCustomCategory = true
            customCategoryInput = ""
          },
        )
      }
      if (isCustomCategory) {
        BasicTextField(
          value = customCategoryInput,
          onValueChange = { customCategoryInput = it },
          singleLine = true,
          textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
          decorationBox = { innerTextField ->
            Box {
              if (customCategoryInput.isEmpty()) {
                BasicText(
                  text = "카테고리 입력",
                  style = UiKitTypography.Value.copy(color = Color(0xFFBBBBBB)),
                )
              }
              innerTextField()
            }
          },
        )
      }
    }
    StudySheetField(
      label = "이름",
      value = nameInput,
      placeholder = "자료 이름을 입력하세요",
      singleLine = true,
      height = 44,
      onValueChange = { nameInput = it },
    )
    StudySheetField(
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
          val category = when {
            isCustomCategory -> customCategoryInput
            else -> selectedCategory ?: return@clickable
          }
          if (category.isBlank() || nameInput.isBlank()) return@clickable
          onSubmit(
            category,
            nameInput,
            contentInput,
          )
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

@Composable private fun StudyCategoryChip(
  text: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  isAdd: Boolean = false,
  onClick: () -> Unit,
) {
  val containerColor = when {
    selected -> StudyAccent
    isAdd -> Color.Transparent
    else -> Color(0xFFF5F5F5)
  }
  val textColor = when {
    selected -> Color.White
    isAdd -> Color(0xFF999999)
    else -> Color(0xFF555555)
  }
  val borderColor = when {
    selected -> StudyAccent
    isAdd -> Color(0xFFBBBBBB)
    else -> Color(0xFFE0E0E0)
  }
  BasicText(
    text = text,
    modifier = modifier
      .clip(CircleShape)
      .background(containerColor)
      .border(1.dp, borderColor, CircleShape)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    style = TextStyle(
      color = textColor,
      fontSize = 13.sp,
      fontWeight = when {
        selected -> FontWeight.SemiBold
        else -> FontWeight.Medium
      },
    ),
  )
}

@Composable private fun StudySheetField(
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
        Box {
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
