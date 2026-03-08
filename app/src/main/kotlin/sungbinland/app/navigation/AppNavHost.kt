package sungbinland.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import sungbinland.nutrition.NutritionRoute
import sungbinland.nutrition.nutritionEntry
import sungbinland.study.StudyRoute
import sungbinland.study.studyEntry
import sungbinland.workout.WorkoutRoute
import sungbinland.workout.workoutEntry

@Composable internal fun AppNavHost(modifier: Modifier = Modifier) {
  val tabs = remember { listOf(NutritionRoute, WorkoutRoute, StudyRoute) }
  val backStack = rememberNavBackStack(NutritionRoute)
  val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
  val entryProvider = remember {
    entryProvider {
      nutritionEntry()
      workoutEntry()
      studyEntry()
    }
  }
  val selectedTab by remember(backStack) { derivedStateOf { backStack.last() } }
  val labelsByTab: Map<NavKey, String> = remember(entryProvider) {
    mapOf(
      NutritionRoute to entryProvider(NutritionRoute).label(),
      WorkoutRoute to entryProvider(WorkoutRoute).label(),
      StudyRoute to entryProvider(StudyRoute).label(),
    )
  }
  val iconsByTab: Map<NavKey, ImageVector> = remember {
    mapOf(
      NutritionRoute to Icons.Rounded.Restaurant,
      WorkoutRoute to Icons.Rounded.FitnessCenter,
      StudyRoute to Icons.AutoMirrored.Rounded.MenuBook,
    )
  }

  Scaffold(
    modifier = modifier,
    bottomBar = {
      TabBottomBar(
        modifier = Modifier.fillMaxWidth(),
        tabs = tabs,
        selectedTab = selectedTab,
        iconOf = iconsByTab::getValue,
        labelOf = labelsByTab::getValue,
        onTabClick = { tab ->
          if (selectedTab != tab) {
            backStack.add(tab)
          }
        },
      )
    },
  ) { padding ->
    NavDisplay(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      backStack = backStack,
      entryDecorators = listOf(stateHolderDecorator),
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
      entryProvider = entryProvider,
      onBack = {
        if (backStack.size > 1) {
          backStack.removeLast()
        }
      },
    )
  }
}

private fun NavEntry<NavKey>.label(): String =
  metadata["label"] as? String ?: error("Missing tab label metadata for entry: $this")
