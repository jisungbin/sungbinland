package sungbinland.nutrition

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import java.time.LocalDate
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.uikit.FloatingButtonState

public fun EntryProviderScope<NavKey>.nutritionEntry(
  bodyInfoDao: BodyInfoDao,
  eatenFoodDao: EatenFoodDao,
  foodDao: FoodDao,
) {
  entry<NutritionRoute>(
    metadata = mapOf(
      "label" to "영양",
      "floatingButtonState" to FloatingButtonState(
        icon = Icons.Rounded.Restaurant,
        onClick = {},
      ),
    ),
  ) {
    val mapper = retain(bodyInfoDao, eatenFoodDao, foodDao) {
      NutritionDashboardStateMapper(
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
      )
    }
    val stateHolder = retain(mapper) {
      NutritionStateHolder(
        mapper = mapper,
        bodyInfoDao = bodyInfoDao,
        nowProvider = { LocalDate.now() },
      )
    }
    RetainedEffect(stateHolder) { onRetire(stateHolder::close) }
    NutritionScreen(
      stateHolder = stateHolder,
    )
  }
}
