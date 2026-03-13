package sungbinland.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.ImmutableList
import sungbinland.core.nutrition.entity.FoodEntity
import sungbinland.uikit.IbmPlexSansKr
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitTypography

private val sheetTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmm")

private fun formatTimeDigits(digits: String): String {
  val padded = digits.padEnd(4, '0')
  return "${padded[0]}${padded[1]}:${padded[2]}${padded[3]}"
}

private val TimeVisualTransformation = VisualTransformation { text ->
  val padded = text.text.padEnd(4, '0')
  val formatted = "${padded[0]}${padded[1]}:${padded[2]}${padded[3]}"
  TransformedText(
    text = AnnotatedString(formatted),
    offsetMapping = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int =
        if (offset <= 1) offset else (offset + 1).coerceAtMost(5)

      override fun transformedToOriginal(offset: Int): Int =
        when {
          offset <= 1 -> offset
          offset == 2 -> 2
          else -> offset - 1
        }.coerceAtMost(text.text.length)
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun NutritionFoodRegistrationSheet(
  registeredFoods: ImmutableList<FoodEntity>,
  modifier: Modifier = Modifier,
  onSubmit: (
    foodName: String,
    quantity: Int,
    timeInput: String,
    calories: Int,
    carbohydrateGrams: Int,
    proteinGrams: Int,
  ) -> Unit,
) {
  var selectedFoodName by remember { mutableStateOf<String?>(null) }
  var isCustomInput by remember { mutableStateOf(false) }
  var customFoodName by remember { mutableStateOf("") }
  var quantity by remember { mutableIntStateOf(1) }
  var timeInput by remember { mutableStateOf(LocalTime.now().format(sheetTimeFormatter)) }
  var caloriesInput by remember { mutableStateOf("") }
  var carbohydrateInput by remember { mutableStateOf("") }
  var proteinInput by remember { mutableStateOf("") }

  val timeFocusRequester = remember { FocusRequester() }
  val caloriesFocusRequester = remember { FocusRequester() }
  val carbohydrateFocusRequester = remember { FocusRequester() }
  val proteinFocusRequester = remember { FocusRequester() }

  val isSubmitEnabled = run {
    val hasFoodName = if (isCustomInput) customFoodName.isNotBlank() else selectedFoodName != null
    hasFoodName &&
      timeInput.length == 4 &&
      caloriesInput.toIntOrNull() != null &&
      carbohydrateInput.toIntOrNull() != null &&
      proteinInput.toIntOrNull() != null
  }
  val performSubmit: () -> Unit = submit@{
    if (!isSubmitEnabled) return@submit
    val foodName = if (isCustomInput) customFoodName else selectedFoodName ?: return@submit
    onSubmit(
      foodName,
      quantity,
      formatTimeDigits(timeInput),
      caloriesInput.toIntOrNull() ?: 0,
      carbohydrateInput.toIntOrNull() ?: 0,
      proteinInput.toIntOrNull() ?: 0,
    )
  }

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
      text = "음식 등록",
      style = UiKitTypography.TitleLarge.copy(color = UiKitColors.Text),
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      BasicText(
        text = "음식 이름",
        style = UiKitTypography.Value.copy(
          color = UiKitColors.Text,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        registeredFoods.fastForEach { food ->
          FoodChip(
            text = food.name,
            selected = selectedFoodName == food.name && !isCustomInput,
            onClick = {
              selectedFoodName = food.name
              isCustomInput = false
              caloriesInput = food.calories.toString()
              carbohydrateInput = food.carbohydrateGrams.toString()
              proteinInput = food.proteinGrams.toString()
            },
          )
        }
        FoodChip(
          text = "+ 직접 입력",
          selected = isCustomInput,
          isAdd = true,
          onClick = {
            selectedFoodName = null
            isCustomInput = true
            customFoodName = ""
            caloriesInput = ""
            carbohydrateInput = ""
            proteinInput = ""
          },
        )
      }
      if (isCustomInput) {
        BasicTextField(
          value = customFoodName,
          onValueChange = { customFoodName = it },
          singleLine = true,
          textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { timeFocusRequester.requestFocus() }),
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
          decorationBox = { innerTextField ->
            Box {
              if (customFoodName.isEmpty()) {
                BasicText(
                  text = "음식 이름 입력",
                  style = UiKitTypography.Value.copy(color = Color(0xFFBBBBBB)),
                )
              }
              innerTextField()
            }
          },
        )
      }
    }
    Column(
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      SheetInputRow(label = "수량") {
        QuantityStepper(
          quantity = quantity,
          onMinus = { if (quantity > 1) quantity-- },
          onPlus = { quantity++ },
        )
      }
      SheetTextField(
        label = "시각",
        value = timeInput,
        placeholder = "",
        suffix = "hh:mm",
        keyboardType = KeyboardType.Number,
        focusRequester = timeFocusRequester,
        imeAction = ImeAction.Next,
        onImeAction = { caloriesFocusRequester.requestFocus() },
        visualTransformation = TimeVisualTransformation,
        onValueChange = { if (it.length <= 4) timeInput = it },
      )
      SheetTextField(
        label = "칼로리",
        value = caloriesInput,
        placeholder = "0",
        suffix = "kcal",
        keyboardType = KeyboardType.Number,
        focusRequester = caloriesFocusRequester,
        imeAction = ImeAction.Next,
        onImeAction = { carbohydrateFocusRequester.requestFocus() },
        onValueChange = { caloriesInput = it },
      )
      SheetTextField(
        label = "탄수화물",
        value = carbohydrateInput,
        placeholder = "0",
        suffix = "g",
        keyboardType = KeyboardType.Number,
        focusRequester = carbohydrateFocusRequester,
        imeAction = ImeAction.Next,
        onImeAction = { proteinFocusRequester.requestFocus() },
        onValueChange = { carbohydrateInput = it },
      )
      SheetTextField(
        label = "단백질",
        value = proteinInput,
        placeholder = "0",
        suffix = "g",
        keyboardType = KeyboardType.Number,
        focusRequester = proteinFocusRequester,
        imeAction = ImeAction.Done,
        onImeAction = performSubmit,
        onValueChange = { proteinInput = it },
      )
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(if (isSubmitEnabled) UiKitColors.BrandBlue else Color(0xFFCCCCCC))
        .clickable(enabled = isSubmitEnabled, onClick = performSubmit),
      contentAlignment = Alignment.Center,
    ) {
      BasicText(
        text = "등록하기",
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
  }
}

@Composable private fun FoodChip(
  text: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  isAdd: Boolean = false,
  onClick: () -> Unit,
) {
  val containerColor = when {
    selected -> UiKitColors.BrandBlue
    isAdd -> Color.Transparent
    else -> Color(0xFFF5F5F5)
  }
  val textColor = when {
    selected -> Color.White
    isAdd -> Color(0xFF999999)
    else -> Color(0xFF555555)
  }
  val borderColor = when {
    selected -> UiKitColors.BrandBlue
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
      fontFamily = IbmPlexSansKr,
      color = textColor,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
    ),
  )
}

@Composable private fun SheetInputRow(
  label: String,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = label,
      modifier = Modifier.width(80.dp),
      style = UiKitTypography.Value.copy(
        color = UiKitColors.Text,
        fontWeight = FontWeight.Medium,
      ),
    )
    content()
  }
}

@Composable private fun SheetTextField(
  label: String,
  value: String,
  placeholder: String,
  suffix: String,
  keyboardType: KeyboardType,
  modifier: Modifier = Modifier,
  focusRequester: FocusRequester? = null,
  imeAction: ImeAction = ImeAction.Default,
  onImeAction: (() -> Unit)? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onValueChange: (String) -> Unit,
) {
  SheetInputRow(label = label, modifier = modifier) {
    BasicTextField(
      value = value,
      onValueChange = { input ->
        when (keyboardType) {
          KeyboardType.Number -> onValueChange(input.filter(Char::isDigit))
          else -> onValueChange(input)
        }
      },
      singleLine = true,
      textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
      modifier = Modifier
        .weight(1f)
        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        .height(44.dp)
        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
        .padding(horizontal = 14.dp),
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
      keyboardActions = when (imeAction) {
        ImeAction.Next -> KeyboardActions(onNext = { onImeAction?.invoke() })
        ImeAction.Done -> KeyboardActions(onDone = { onImeAction?.invoke() })
        else -> KeyboardActions.Default
      },
      visualTransformation = visualTransformation,
      decorationBox = { innerTextField ->
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
              BasicText(
                text = placeholder,
                style = UiKitTypography.Value.copy(color = Color(0xFFBBBBBB)),
              )
            }
            innerTextField()
          }
          BasicText(
            text = suffix,
            style = TextStyle(
              fontFamily = IbmPlexSansKr,
              color = Color(0xFF999999),
              fontSize = 13.sp,
            ),
          )
        }
      },
    )
  }
}

@Composable private fun QuantityStepper(
  quantity: Int,
  modifier: Modifier = Modifier,
  onMinus: () -> Unit,
  onPlus: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFFF0F0F0))
        .clickable(onClick = onMinus),
      contentAlignment = Alignment.Center,
    ) {
      BasicText(
        text = "−",
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = Color(0xFF555555),
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicText(
        text = quantity.toString(),
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = UiKitColors.Text,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicText(
        text = "개",
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = Color(0xFF999999),
          fontSize = 14.sp,
        ),
      )
    }
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFFF0F0F0))
        .clickable(onClick = onPlus),
      contentAlignment = Alignment.Center,
    ) {
      BasicText(
        text = "+",
        style = TextStyle(
          fontFamily = IbmPlexSansKr,
          color = Color(0xFF555555),
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
  }
}
