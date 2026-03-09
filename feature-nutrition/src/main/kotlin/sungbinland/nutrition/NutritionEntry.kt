package sungbinland.nutrition

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import java.time.LocalDate
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.uikit.FloatingButtonState
import sungbinland.uikit.LocalFabController

public fun EntryProviderScope<NavKey>.nutritionEntry(
  bodyInfoDao: BodyInfoDao,
  eatenFoodDao: EatenFoodDao,
  foodDao: FoodDao,
  onNavigate: (NavKey) -> Unit,
  onBack: () -> Unit,
) {
  entry<NutritionRoute>(
    metadata = mapOf("label" to "영양"),
  ) {
    val fabController = LocalFabController.current
    var showFoodSheet by rememberSaveable { mutableStateOf(false) }
    val fabState = remember {
      FloatingButtonState(
        icon = Icons.Rounded.Restaurant,
        onClick = { showFoodSheet = true },
      )
    }
    SideEffect { fabController.set(NutritionRoute, fabState) }
    val mapper = retain(bodyInfoDao, eatenFoodDao, foodDao) {
      NutritionDashboardStateMapper(
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
      )
    }
    val stateHolder = retain(mapper, eatenFoodDao, foodDao) {
      NutritionStateHolder(
        mapper = mapper,
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
        nowProvider = { LocalDate.now() },
      )
    }
    RetainedEffect(stateHolder) { onRetire(stateHolder::close) }
    NutritionScreen(
      stateHolder = stateHolder,
      showFoodRegistrationSheet = showFoodSheet,
      onDismissFoodRegistrationSheet = { showFoodSheet = false },
      onOpenGraphClick = { onNavigate(NutritionGraphRoute) },
    )
  }
  entry<NutritionGraphRoute> {
    val stateHolder = retain(bodyInfoDao, eatenFoodDao, foodDao) {
      NutritionGraphStateHolder(
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
        nowProvider = { LocalDate.now() },
      )
    }
    RetainedEffect(stateHolder) { onRetire(stateHolder::close) }
    NutritionGraphScreen(
      stateHolder = stateHolder,
      onBack = onBack,
    )
  }
}
