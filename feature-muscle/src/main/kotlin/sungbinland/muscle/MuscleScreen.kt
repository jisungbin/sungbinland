package sungbinland.muscle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.toImmutableList
import sungbinland.uikit.UiKitChipState
import sungbinland.uikit.UiKitColors
import sungbinland.uikit.UiKitSearchField
import sungbinland.uikit.UiKitSurfaceCard
import sungbinland.uikit.UiKitTypography

@Composable internal fun MuscleScreen(
  onOverlayChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedCategory by rememberSaveable { mutableStateOf(MuscleCategory.ALL.name) }
  var fullscreenMuscle by remember { mutableStateOf<MuscleItem?>(null) }
  var showQuiz by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(fullscreenMuscle, showQuiz) {
    onOverlayChange(fullscreenMuscle != null || showQuiz)
  }

  val filteredMuscles by remember(searchQuery, selectedCategory) {
    derivedStateOf {
      val category = MuscleCategory.valueOf(selectedCategory)
      AllMuscles.filter { muscle ->
        val matchesCategory = category == MuscleCategory.ALL || muscle.category == category
        val matchesSearch = searchQuery.isBlank() ||
          muscle.name.contains(searchQuery, ignoreCase = true) ||
          muscle.detail.contains(searchQuery, ignoreCase = true) ||
          muscle.category.label.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
      }.toImmutableList()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Fixed(2),
      modifier = Modifier
        .fillMaxSize()
        .background(UiKitColors.Background)
        .systemBarsPadding()
        .padding(horizontal = 16.dp),
      verticalItemSpacing = 12.dp,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(span = StaggeredGridItemSpan.FullLine) {
        MuscleHeader(
          searchQuery = searchQuery,
          selectedCategory = selectedCategory,
          onSearchQueryChange = { searchQuery = it },
          onCategorySelect = { selectedCategory = it },
          onQuizClick = { showQuiz = true },
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        )
      }
      items(
        items = filteredMuscles,
        key = { it.name },
      ) { muscle ->
        MuscleCard(
          muscle = muscle,
          modifier = Modifier.fillMaxWidth(),
          onClick = { fullscreenMuscle = muscle },
        )
      }
      item(span = StaggeredGridItemSpan.FullLine) {
        Box(modifier = Modifier.padding(bottom = 92.dp))
      }
    }

    val currentFullscreen = fullscreenMuscle
    if (currentFullscreen != null) {
      MuscleFullscreenViewer(
        muscle = currentFullscreen,
        onDismiss = { fullscreenMuscle = null },
      )
    }

    if (showQuiz) {
      MuscleQuizOverlay(onDismiss = { showQuiz = false })
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun MuscleHeader(
  searchQuery: String,
  selectedCategory: String,
  onSearchQueryChange: (String) -> Unit,
  onCategorySelect: (String) -> Unit,
  onQuizClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val chips = remember(selectedCategory) {
    MuscleCategory.entries.map { category ->
      UiKitChipState(
        id = category.name,
        label = category.label,
        selected = category.name == selectedCategory,
      )
    }.toImmutableList()
  }

  UiKitSurfaceCard(
    modifier = modifier,
    verticalSpacing = 16.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        BasicText(
          text = "근육 도감",
          style = UiKitTypography.Headline.copy(color = UiKitColors.Primary),
        )
        BasicText(
          text = "데일리 퀴즈!",
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFD4564E))
            .clickable(onClick = onQuizClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
          style = UiKitTypography.Label.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold,
          ),
        )
      }
      UiKitSearchField(
        value = searchQuery,
        placeholder = "근육 이름 검색",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onSearchQueryChange,
      )
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        chips.forEach { chip ->
          MuscleFilterChip(
            label = chip.label,
            selected = chip.selected,
            onClick = { onCategorySelect(chip.id) },
          )
        }
      }
    }
  }
}

@Composable private fun MuscleFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val accentColor = Color(0xFFD4564E)
  val containerColor by animateColorAsState(
    targetValue = if (selected) accentColor else Color(0xFFFCFBF9),
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) accentColor else UiKitColors.BorderSoft,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
  )
  val textColor by animateColorAsState(
    targetValue = if (selected) Color.White else UiKitColors.MutedTextStrong,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
  )

  BasicText(
    text = label,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(color = containerColor)
      .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    style = UiKitTypography.Value.copy(
      color = textColor,
      fontWeight = FontWeight.SemiBold,
    ),
  )
}

@Composable private fun MuscleCard(
  muscle: MuscleItem,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  var expanded by remember { mutableStateOf(false) }
  val cardShape = RoundedCornerShape(16.dp)

  Column(
    modifier = modifier
      .shadow(
        elevation = 2.dp,
        shape = cardShape,
        ambientColor = Color.Black.copy(alpha = 0.06f),
        spotColor = Color.Black.copy(alpha = 0.08f),
      )
      .clip(cardShape)
      .background(UiKitColors.Surface)
      .border(width = 1.dp, color = UiKitColors.Border, shape = cardShape)
      .clickable { onClick() },
  ) {
    Box {
      AsyncImage(
        model = muscle.imageRes,
        contentDescription = muscle.name,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentScale = ContentScale.FillWidth,
      )
      // Category badge
      BasicText(
        text = muscle.category.label,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
          .background(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(8.dp),
          )
          .padding(horizontal = 8.dp, vertical = 4.dp),
        style = UiKitTypography.Micro.copy(color = Color.White),
      )
    }
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      BasicText(
        text = muscle.name,
        style = UiKitTypography.Title.copy(
          color = UiKitColors.Text,
          fontWeight = FontWeight.SemiBold,
        ),
      )
      BasicText(
        text = muscle.detail,
        style = UiKitTypography.Label.copy(color = UiKitColors.MutedTextStrong),
      )
    }
  }
}

@Composable private fun MuscleFullscreenViewer(
  muscle: MuscleItem,
  onDismiss: () -> Unit,
) {
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.92f))
      .pointerInput(Unit) {
        detectTapGestures(onTap = { onDismiss() })
      },
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Title bar
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        BasicText(
          text = muscle.name,
          style = UiKitTypography.TitleLarge.copy(color = Color.White),
        )
        BasicText(
          text = muscle.detail,
          style = UiKitTypography.Value.copy(color = Color.White.copy(alpha = 0.7f)),
        )
      }

      // Zoomable image
      AsyncImage(
        model = muscle.imageRes,
        contentDescription = muscle.name,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
              scale = (scale * zoom).coerceIn(1f, 5f)
              if (scale > 1f) {
                offset = Offset(offset.x + pan.x, offset.y + pan.y)
              } else {
                offset = Offset.Zero
              }
            }
          }
          .pointerInput(Unit) {
            detectTapGestures(
              onDoubleTap = {
                if (scale > 1f) {
                  scale = 1f
                  offset = Offset.Zero
                } else {
                  scale = 2.5f
                }
              },
            )
          }
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offset.x
            translationY = offset.y
          },
        contentScale = ContentScale.FillWidth,
      )

      BasicText(
        text = "탭하여 닫기",
        style = UiKitTypography.Label.copy(color = Color.White.copy(alpha = 0.5f)),
      )
    }
  }
}

private class QuizQuestion(
  val answer: MuscleItem,
  val options: List<MuscleItem>,
)

private class QuizSession {
  private val pool = AllMuscles.shuffled().toMutableList()

  fun next(): QuizQuestion {
    if (pool.isEmpty()) pool.addAll(AllMuscles.shuffled())
    val answer = pool.removeFirst()
    val distractors = mutableListOf<MuscleItem>()
    // One same-category distractor if available
    val sameCategory = AllMuscles
      .filter { it.name != answer.name && it.category == answer.category }
      .shuffled()
      .firstOrNull()
    if (sameCategory != null) distractors.add(sameCategory)
    // Fill rest from different categories
    AllMuscles
      .filter { it.name != answer.name && it !in distractors }
      .shuffled()
      .take(3 - distractors.size)
      .let(distractors::addAll)
    return QuizQuestion(answer = answer, options = (distractors.take(3) + answer).shuffled())
  }
}

private const val QUIZ_MAX = 5

@Composable private fun MuscleQuizOverlay(onDismiss: () -> Unit) {
  val session = remember { QuizSession() }
  var question by remember { mutableStateOf(session.next()) }
  var selectedAnswer by remember { mutableStateOf<MuscleItem?>(null) }
  var score by rememberSaveable { mutableIntStateOf(0) }
  var total by rememberSaveable { mutableIntStateOf(0) }
  val quizFinished = total >= QUIZ_MAX && selectedAnswer != null

  val answered = selectedAnswer != null
  val isCorrect = selectedAnswer?.name == question.answer.name

  val shakeOffset = remember { Animatable(0f) }
  LaunchedEffect(selectedAnswer) {
    if (selectedAnswer != null && !isCorrect) {
      repeat(3) {
        shakeOffset.animateTo(8f, animationSpec = tween(durationMillis = 50))
        shakeOffset.animateTo(-8f, animationSpec = tween(durationMillis = 50))
      }
      shakeOffset.animateTo(0f, animationSpec = tween(durationMillis = 50))
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.92f))
      .pointerInput(Unit) { detectTapGestures { /* consume */ } },
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .systemBarsPadding()
        .padding(horizontal = 24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      // Score
      BasicText(
        text = "$score / $total (총 $QUIZ_MAX 문제)",
        style = UiKitTypography.Title.copy(color = Color.White.copy(alpha = 0.6f)),
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Role description card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .graphicsLayer { translationX = shakeOffset.value }
          .clip(RoundedCornerShape(16.dp))
          .background(Color.White.copy(alpha = 0.07f))
          .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
          )
          .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          BasicText(
            text = "이 역할을 하는 근육은?",
            style = UiKitTypography.Label.copy(
              color = Color.White.copy(alpha = 0.5f),
              fontWeight = FontWeight.Medium,
            ),
          )
          BasicText(
            text = "\"${question.answer.role}\"",
            style = UiKitTypography.Title.copy(
              color = Color.White,
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center,
              lineHeight = 28.sp,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Options
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        question.options.forEach { option ->
          val optionSelected = selectedAnswer?.name == option.name
          val isThisCorrect = option.name == question.answer.name

          val bgColor = when {
            !answered -> Color.White.copy(alpha = 0.08f)
            isThisCorrect -> Color(0xFF2E7D32).copy(alpha = 0.85f)
            optionSelected -> Color(0xFFD4564E).copy(alpha = 0.85f)
            else -> Color.White.copy(alpha = 0.05f)
          }
          val borderColor = when {
            !answered -> Color.White.copy(alpha = 0.15f)
            isThisCorrect -> Color(0xFF4CAF50)
            optionSelected -> Color(0xFFE85A4F)
            else -> Color.White.copy(alpha = 0.08f)
          }
          val textColor = when {
            !answered -> Color.White
            isThisCorrect || optionSelected -> Color.White
            else -> Color.White.copy(alpha = 0.35f)
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(bgColor)
              .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
              .then(
                if (!answered) Modifier.clickable {
                  selectedAnswer = option
                  total++
                  if (option.name == question.answer.name) score++
                } else Modifier,
              )
              .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            BasicText(
              text = option.name,
              modifier = Modifier.weight(1f),
              style = UiKitTypography.Title.copy(
                color = textColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
              ),
            )
            if (answered) {
              AsyncImage(
                model = option.imageRes,
                contentDescription = null,
                modifier = Modifier
                  .height(44.dp)
                  .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.FillHeight,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Result + Actions
      if (answered) {
        if (isCorrect) {
          BasicText(
            text = "정답!",
            style = UiKitTypography.Title.copy(
              color = Color(0xFF4CAF50),
              fontWeight = FontWeight.Bold,
            ),
          )
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            BasicText(
              text = "오답! 정답: ${question.answer.name}",
              style = UiKitTypography.Title.copy(
                color = Color(0xFFE85A4F),
                fontWeight = FontWeight.Bold,
              ),
            )
            // Wrong answer's role explanation
            BasicText(
              text = "\"${selectedAnswer!!.name}\"${selectedAnswer!!.name.eunNeun()}:",
              style = UiKitTypography.Label.copy(
                color = Color.White.copy(alpha = 0.5f),
              ),
            )
            BasicText(
              text = selectedAnswer!!.role,
              style = UiKitTypography.Value.copy(
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
              ),
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (quizFinished) {
          // Final result
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            BasicText(
              text = "최종 점수: $score / $QUIZ_MAX",
              style = UiKitTypography.TitleLarge.copy(color = Color.White),
            )
            BasicText(
              text = "닫기",
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD4564E))
                .clickable {
                  score = 0
                  total = 0
                  onDismiss()
                }
                .padding(horizontal = 24.dp, vertical = 12.dp),
              style = UiKitTypography.Value.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
              ),
            )
          }
        } else {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            BasicText(
              text = "다음 문제",
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD4564E))
                .clickable {
                  question = session.next()
                  selectedAnswer = null
                }
                .padding(horizontal = 20.dp, vertical = 10.dp),
              style = UiKitTypography.Value.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
              ),
            )
            BasicText(
              text = "그만하기",
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .clickable {
                  score = 0
                  total = 0
                  onDismiss()
                }
                .padding(horizontal = 20.dp, vertical = 10.dp),
              style = UiKitTypography.Value.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
              ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
