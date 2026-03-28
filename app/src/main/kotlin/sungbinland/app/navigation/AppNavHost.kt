package sungbinland.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.collections.immutable.persistentListOf
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutExerciseDao
import sungbinland.core.workout.dao.WorkoutRoutineDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.muscle.MuscleRoute
import sungbinland.muscle.muscleEntry
import sungbinland.study.StudyRoute
import sungbinland.study.studyEntry
import sungbinland.uikit.FabController
import sungbinland.uikit.BottomSheetSceneStrategy
import sungbinland.uikit.LocalFabController
import sungbinland.uikit.UiKitFloatingActionButton
import sungbinland.workout.WorkoutRoute
import sungbinland.workout.workoutEntry

@Composable internal fun AppNavHost(
  studyEntryDao: StudyEntryDao,
  timerRecordDao: TimerRecordDao,
  workoutSessionDao: WorkoutSessionDao,
  workoutRoutineDao: WorkoutRoutineDao,
  workoutExerciseDao: WorkoutExerciseDao,
  modifier: Modifier = Modifier,
) {
  val tabs = remember { persistentListOf(WorkoutRoute, StudyRoute, MuscleRoute) }
  val backStack = rememberNavBackStack(WorkoutRoute)
  var muscleOverlayActive by remember { mutableStateOf(false) }
  val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
  val entryProvider = remember {
    entryProvider {
      workoutEntry(
        timerRecordDao = timerRecordDao,
        workoutSessionDao = workoutSessionDao,
        workoutRoutineDao = workoutRoutineDao,
        workoutExerciseDao = workoutExerciseDao,
        onNavigate = { key -> backStack.add(key) },
        onBack = {
          if (backStack.size > 1) {
            backStack.removeLast()
          }
        },
      )
      studyEntry(
        studyEntryDao = studyEntryDao,
        onNavigate = { key -> backStack.add(key) },
        onBack = {
          if (backStack.size > 1) {
            backStack.removeLast()
          }
        },
      )
      muscleEntry(onOverlayChange = { muscleOverlayActive = it })
    }
  }
  val selectedTab by remember(backStack, tabs) { derivedStateOf { backStack.lastOrNull { it in tabs } ?: tabs.first() } }
  val isOverlayActive by remember(backStack, tabs) { derivedStateOf { backStack.lastOrNull()?.let { it !in tabs } == true } }
  val hazeState = remember { HazeState() }
  val fabController = remember { FabController() }
  val labelsByTab = remember(entryProvider) {
    mapOf(
      WorkoutRoute to entryProvider(WorkoutRoute).label(),
      StudyRoute to entryProvider(StudyRoute).label(),
      MuscleRoute to entryProvider(MuscleRoute).label(),
    )
  }
  val iconsByTab = remember {
    mapOf(
      WorkoutRoute to Icons.Rounded.FitnessCenter,
      StudyRoute to Icons.AutoMirrored.Rounded.MenuBook,
      MuscleRoute to Icons.Rounded.Accessibility,
    )
  }
  val accentsByTab = remember {
    mapOf(
      WorkoutRoute to Color(0xFF4A7BFF),
      StudyRoute to Color(0xFF20B07A),
      MuscleRoute to Color(0xFFD4564E),
    )
  }

  Box(modifier = modifier) {
    CompositionLocalProvider(LocalFabController provides fabController) {
      NavDisplay(
        modifier = Modifier
          .fillMaxSize()
          .hazeSource(state = hazeState),
        backStack = backStack,
        entryDecorators = persistentListOf(stateHolderDecorator),
        sceneStrategies = listOf(BottomSheetSceneStrategy()),
        transitionSpec = {
          ContentTransform(
            targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 300)),
            initialContentExit = fadeOut(animationSpec = tween(durationMillis = 300)),
          )
        },
        popTransitionSpec = {
          ContentTransform(
            targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 300)),
            initialContentExit = fadeOut(animationSpec = tween(durationMillis = 300)),
          )
        },
        predictivePopTransitionSpec = {
            ContentTransform(
              targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 300)),
              initialContentExit = fadeOut(animationSpec = tween(durationMillis = 300)),
            )
          },
        entryProvider = entryProvider,
        onBack = {
          if (backStack.size > 1) {
            backStack.removeLast()
          }
        },
      )
    }

    if (!isOverlayActive && !muscleOverlayActive) {
      val hasFab = fabController.get(selectedTab) != null
      Row(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
          .padding(bottom = 16.dp)
          .align(Alignment.BottomCenter)
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
      ) {
        TabBottomBar(
          modifier = Modifier
            .fillMaxWidth(if (hasFab) 0.7f else 0.88f)
            .height(62.dp),
          hazeState = hazeState,
          tabs = tabs,
          selectedTab = selectedTab,
          accentOf = accentsByTab::getValue,
          iconOf = iconsByTab::getValue,
          labelOf = labelsByTab::getValue,
          onTabClick = { tab ->
            if (selectedTab != tab) {
              backStack.add(tab)
            }
          },
        )
        if (hasFab) UiKitFloatingActionButton(
          hazeState = hazeState,
          onClick = { fabController.get(selectedTab)?.onClick?.invoke() },
          onLongClick = { fabController.get(selectedTab)?.onLongClick?.invoke() },
        ) {
          AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
              ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 300)),
                initialContentExit = fadeOut(animationSpec = tween(durationMillis = 300)),
              )
            },
            label = "fab-icon",
          ) { tab ->
            val fabState = fabController.get(tab) ?: return@AnimatedContent
            val progressValue = fabState.progress?.value
            if (progressValue != null && progressValue > 0f) {
              androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(
                  color = Color.White.copy(alpha = 0.3f),
                  style = stroke,
                )
                drawArc(
                  color = Color.White,
                  startAngle = -90f,
                  sweepAngle = 360f * progressValue,
                  useCenter = false,
                  style = stroke,
                )
              }
            } else {
              Image(
                modifier = Modifier.size(24.dp),
                imageVector = fabState.icon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
              )
            }
          }
        }
      }
    }
  }
}

private fun NavEntry<NavKey>.label(): String =
  metadata["label"] as? String ?: error("Missing tab label metadata for entry: $this")
