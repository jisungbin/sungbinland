package sungbinland.nutrition

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

public fun EntryProviderScope<NavKey>.nutritionEntry() {
  entry<NutritionRoute>(metadata = mapOf("label" to "영양")) {
    NutritionScreen()
  }
}
