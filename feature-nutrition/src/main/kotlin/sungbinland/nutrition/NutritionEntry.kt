package sungbinland.nutrition

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.nutrition.entity.FoodEntity
import sungbinland.uikit.BottomSheetSceneStrategy
import sungbinland.uikit.FloatingButtonState
import sungbinland.uikit.LocalFabController

public fun EntryProviderScope<NavKey>.nutritionEntry(
  bodyInfoDao: BodyInfoDao,
  eatenFoodDao: EatenFoodDao,
  foodDao: FoodDao,
  onNavigate: (NavKey) -> Unit,
  onBack: () -> Unit,
) {
  val mapper = NutritionDashboardStateMapper(
    bodyInfoDao = bodyInfoDao,
    eatenFoodDao = eatenFoodDao,
    foodDao = foodDao,
  )
  val factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return NutritionViewModel(
        mapper = mapper,
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
      ) as T
    }
  }

  entry<NutritionRoute>(
    metadata = mapOf("label" to "영양"),
  ) {
    val viewModel = viewModel<NutritionViewModel>(factory = factory)
    val fabController = LocalFabController.current
    val fabState = remember {
      FloatingButtonState(
        icon = Icons.Rounded.Restaurant,
        onClick = { onNavigate(NutritionFoodRegistrationSheetRoute) },
      )
    }
    SideEffect { fabController.set(NutritionRoute, fabState) }
    NutritionScreen(
      viewModel = viewModel,
      onOpenGraphClick = { onNavigate(NutritionGraphRoute) },
    )
  }
  entry<NutritionFoodRegistrationSheetRoute>(
    metadata = BottomSheetSceneStrategy.bottomSheet(),
  ) {
    val viewModel = viewModel<NutritionViewModel>(factory = factory)
    var registeredFoods: ImmutableList<FoodEntity> by remember { mutableStateOf(persistentListOf()) }
    LaunchedEffect(Unit) {
      registeredFoods = viewModel.getRegisteredFoods().toImmutableList()
    }
    NutritionFoodRegistrationSheet(
      registeredFoods = registeredFoods,
      onSubmit = { foodName, quantity, timeInput, calories, carbohydrateGrams, proteinGrams ->
        viewModel.registerFood(
          foodName = foodName,
          quantity = quantity,
          timeInput = timeInput,
          calories = calories,
          carbohydrateGrams = carbohydrateGrams,
          proteinGrams = proteinGrams,
        )
        onBack()
      },
    )
  }
  val graphFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return NutritionGraphViewModel(
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
      ) as T
    }
  }
  entry<NutritionGraphRoute> {
    val graphViewModel = viewModel<NutritionGraphViewModel>(factory = graphFactory)
    NutritionGraphScreen(
      viewModel = graphViewModel,
      onBack = onBack,
    )
  }
}
