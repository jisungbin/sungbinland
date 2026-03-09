package sungbinland.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import sungbinland.core.workout.dao.WorkoutRoutineWithExercises
import sungbinland.core.workout.entity.WorkoutSessionEntity
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun WorkoutRoutineDetailSheet(
  routines: ImmutableList<WorkoutRoutineWithExercises>,
  recentSessions: ImmutableList<WorkoutSessionEntity>,
  modifier: Modifier = Modifier,
  onAddRoutine: (name: String) -> Unit,
  onAddExercise: (routineName: String, exerciseName: String) -> Unit,
) {
  var selectedRoutineName by remember(routines) {
    mutableStateOf(routines.firstOrNull()?.routine?.name)
  }
  var showRoutineInput by remember { mutableStateOf(false) }
  var routineInput by remember { mutableStateOf("") }
  var showExerciseInput by remember { mutableStateOf(false) }
  var exerciseInput by remember { mutableStateOf("") }
  var selectedExerciseName by remember { mutableStateOf<String?>(null) }
  val selectedExercises = remember(routines, selectedRoutineName) {
    routines.firstOrNull { it.routine.name == selectedRoutineName }?.exercises.orEmpty()
  }
  val filteredSessions = remember(recentSessions, selectedExerciseName) {
    val filter = selectedExerciseName
    when {
      filter == null -> recentSessions
      else -> recentSessions.filter { it.mainExerciseName == filter }
    }
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
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    BasicText(
      text = "루틴 상세",
      style = UiKitTypography.TitleLarge.copy(color = UiKitColors.Text),
    )
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        routines.fastForEach { routineWithExercises ->
          val name = routineWithExercises.routine.name
          RoutineChip(
            text = name,
            selected = selectedRoutineName == name,
            onClick = {
              selectedRoutineName = name
              selectedExerciseName = null
            },
          )
        }
        RoutineChip(
          text = "+ 추가",
          selected = false,
          isAdd = true,
          onClick = {
            showRoutineInput = true
            routineInput = ""
          },
        )
      }
      if (showRoutineInput) {
        SheetInput(
          value = routineInput,
          placeholder = "루틴 이름 입력",
          onValueChange = { routineInput = it },
          onSubmit = {
            if (routineInput.isNotBlank()) {
              onAddRoutine(routineInput)
              showRoutineInput = false
              routineInput = ""
            }
          },
        )
      }
    }
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      BasicText(
        text = "등록된 종목",
        style = TextStyle(
          color = UiKitColors.Text,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        selectedExercises.fastForEach { exercise ->
          RoutineChip(
            text = exercise.name,
            selected = selectedExerciseName == exercise.name,
            onClick = {
              selectedExerciseName = when (selectedExerciseName) {
                exercise.name -> null
                else -> exercise.name
              }
            },
          )
        }
        if (selectedRoutineName != null) {
          RoutineChip(
            text = "+ 종목 추가",
            selected = false,
            isAdd = true,
            onClick = {
              showExerciseInput = true
              exerciseInput = ""
            },
          )
        }
      }
      if (showExerciseInput && selectedRoutineName != null) {
        SheetInput(
          value = exerciseInput,
          placeholder = "종목 이름 입력",
          onValueChange = { exerciseInput = it },
          onSubmit = {
            val routine = selectedRoutineName
            if (exerciseInput.isNotBlank() && routine != null) {
              onAddExercise(routine, exerciseInput)
              showExerciseInput = false
              exerciseInput = ""
            }
          },
        )
      }
    }
    if (recentSessions.isNotEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(Color(0xFFEFEFEF)),
      )
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        val sectionTitle = when (val name = selectedExerciseName) {
          null -> "최근 일주일 메인 종목"
          else -> "최근 일주일 $name"
        }
        BasicText(
          text = sectionTitle,
          style = TextStyle(
            color = UiKitColors.Text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          ),
        )
        if (filteredSessions.isEmpty()) {
          BasicText(
            text = "기록 없음",
            style = TextStyle(
              color = Color(0xFF999999),
              fontSize = 14.sp,
            ),
          )
        } else {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            filteredSessions.fastForEach { session ->
              RecentSessionItem(session = session)
            }
          }
        }
      }
    }
  }
}

@Composable private fun RecentSessionItem(
  session: WorkoutSessionEntity,
  modifier: Modifier = Modifier,
) {
  val date = session.performedAt.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
  val dayOfWeek = date.dayOfWeek.getDisplayName(
    JavaTextStyle.SHORT,
    Locale.KOREA,
  )
  val dateText = "${date.monthValue}/${date.dayOfMonth} ($dayOfWeek)"
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    BasicText(
      text = dateText,
      style = TextStyle(
        color = Color(0xFF999999),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      ),
    )
    BasicText(
      text = session.mainExerciseName,
      style = TextStyle(
        color = UiKitColors.Text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
      ),
    )
  }
}

@Composable private fun RoutineChip(
  text: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  isAdd: Boolean = false,
  onClick: () -> Unit,
) {
  val containerColor = when {
    selected -> Color(0xFF1E2432)
    isAdd -> Color.Transparent
    else -> Color(0xFFF5F5F5)
  }
  val textColor = when {
    selected -> Color.White
    isAdd -> Color(0xFF999999)
    else -> Color(0xFF555555)
  }
  val borderColor = when {
    selected -> Color(0xFF1E2432)
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
      fontWeight = FontWeight.Medium,
    ),
  )
}

@Composable private fun SheetInput(
  value: String,
  placeholder: String,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit,
  onSubmit: () -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      textStyle = UiKitTypography.Value.copy(color = UiKitColors.Text),
      modifier = Modifier
        .fillMaxWidth()
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
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(UiKitColors.BrandBlue)
        .clickable(onClick = onSubmit),
      contentAlignment = Alignment.Center,
    ) {
      BasicText(
        text = "등록하기",
        style = TextStyle(
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
        ),
      )
    }
  }
}
