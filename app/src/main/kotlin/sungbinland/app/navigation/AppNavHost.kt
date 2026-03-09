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
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.workout.dao.SupplementDao
import sungbinland.core.workout.dao.SupplementIntakeDao
import sungbinland.core.workout.dao.TimerRecordDao
import sungbinland.core.workout.dao.WorkoutSessionDao
import sungbinland.nutrition.NutritionRoute
import sungbinland.nutrition.nutritionEntry
import sungbinland.study.StudyRoute
import sungbinland.study.studyEntry
import sungbinland.uikit.FabController
import sungbinland.uikit.LocalFabController
import sungbinland.uikit.UiKitFloatingActionButton
import sungbinland.workout.WorkoutRoute
import sungbinland.workout.workoutEntry

@Composable internal fun AppNavHost(
  bodyInfoDao: BodyInfoDao,
  eatenFoodDao: EatenFoodDao,
  foodDao: FoodDao,
  studyEntryDao: StudyEntryDao,
  supplementDao: SupplementDao,
  supplementIntakeDao: SupplementIntakeDao,
  timerRecordDao: TimerRecordDao,
  workoutSessionDao: WorkoutSessionDao,
  modifier: Modifier = Modifier,
) {
  val tabs = remember { persistentListOf(NutritionRoute, WorkoutRoute, StudyRoute) }
  val backStack = rememberNavBackStack(NutritionRoute)
  val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
  val entryProvider = remember {
    entryProvider {
      nutritionEntry(
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
        onNavigate = { key -> backStack.add(key) },
        onBack = {
          if (backStack.size > 1) {
            backStack.removeLast()
          }
        },
      )
      workoutEntry(
        supplementDao = supplementDao,
        supplementIntakeDao = supplementIntakeDao,
        timerRecordDao = timerRecordDao,
        workoutSessionDao = workoutSessionDao,
      )
      studyEntry(
        studyEntryDao = studyEntryDao,
      )
    }
  }
  val selectedTab by remember(backStack, tabs) { derivedStateOf { backStack.lastOrNull { it in tabs } ?: tabs.first() } }
  val hazeState = remember { HazeState() }
  val fabController = remember { FabController() }
  val labelsByTab = remember(entryProvider) {
    mapOf(
      NutritionRoute to entryProvider(NutritionRoute).label(),
      WorkoutRoute to entryProvider(WorkoutRoute).label(),
      StudyRoute to entryProvider(StudyRoute).label(),
    )
  }
  val iconsByTab = remember {
    mapOf(
      NutritionRoute to Icons.Rounded.Restaurant,
      WorkoutRoute to Icons.Rounded.FitnessCenter,
      StudyRoute to Icons.AutoMirrored.Rounded.MenuBook,
    )
  }
  val accentsByTab = remember {
    mapOf(
      NutritionRoute to Color(0xFFE85A4F),
      WorkoutRoute to Color(0xFF4A7BFF),
      StudyRoute to Color(0xFF20B07A),
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
          .fillMaxWidth(0.7f)
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
      UiKitFloatingActionButton(
        hazeState = hazeState,
        onClick = { fabController.get(selectedTab)?.onClick?.invoke() },
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
          val icon = fabController.get(tab)?.icon ?: return@AnimatedContent
          Image(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
          )
        }
      }
    }
  }
}

private fun NavEntry<NavKey>.label(): String =
  metadata["label"] as? String ?: error("Missing tab label metadata for entry: $this")
