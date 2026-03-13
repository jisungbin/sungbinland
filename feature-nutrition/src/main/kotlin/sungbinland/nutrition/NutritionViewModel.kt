package sungbinland.nutrition

import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.dao.EatenFoodDao
import sungbinland.core.nutrition.dao.FoodDao
import sungbinland.core.nutrition.entity.BodyInfoEntity
import sungbinland.core.nutrition.entity.EatenFoodEntity
import sungbinland.core.nutrition.entity.FoodEntity

@OptIn(ExperimentalCoroutinesApi::class)
internal class NutritionViewModel(
  private val mapper: NutritionDashboardStateMapper,
  private val bodyInfoDao: BodyInfoDao,
  private val eatenFoodDao: EatenFoodDao,
  private val foodDao: FoodDao,
) : ViewModel() {
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val selectedDateState: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())
  private val refreshState: MutableStateFlow<Long> = MutableStateFlow(0L)

  internal val selectedDate: LocalDate get() = selectedDateState.value

  internal val state: StateFlow<NutritionDashboardState> =
    combine(selectedDateState, refreshState) { date, _ -> date }
      .mapLatest { date -> mapper.createState(selectedDate = date) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = nutritionDashboardLoadingState(selectedDate = selectedDateState.value),
      )

  internal fun moveToNextDate() {
    selectedDateState.update { it.plusDays(1) }
  }

  internal fun moveToPreviousDate() {
    selectedDateState.update { it.minusDays(1) }
  }

  internal fun moveToToday() {
    selectedDateState.update { LocalDate.now() }
  }

  internal fun saveBodyWeight(weightInput: String) {
    val weightKg = weightInput.toIntOrNull() ?: return
    if (weightKg <= 0) return
    viewModelScope.launch {
      val selectedDate = selectedDateState.value
      bodyInfoDao.upsertBodyInfo(
        bodyInfo = BodyInfoEntity(
          recordedAt = selectedDate.toStartOfDayDate(),
          bodyWeightKg = weightKg,
        ),
      )
      refresh()
    }
  }

  internal suspend fun getLatestBodyWeight(): Int? =
    bodyInfoDao.getLatestBodyInfo()?.bodyWeightKg

  internal fun refresh() {
    refreshState.update { it + 1L }
  }

  internal suspend fun getRegisteredFoods(): List<FoodEntity> =
    foodDao.getAllFoods()

  internal fun registerFood(
    foodName: String,
    quantity: Int,
    timeInput: String,
    calories: Int,
    carbohydrateGrams: Int,
    proteinGrams: Int,
  ) {
    viewModelScope.launch {
      foodDao.upsertFood(
        FoodEntity(
          name = foodName,
          calories = calories,
          proteinGrams = proteinGrams,
          carbohydrateGrams = carbohydrateGrams,
        ),
      )
      eatenFoodDao.upsertEatenFood(
        EatenFoodEntity(
          foodName = foodName,
          quantity = quantity,
          consumedAt = parseConsumedAt(timeInput = timeInput),
        ),
      )
      refresh()
    }
  }

  private fun parseConsumedAt(timeInput: String): Date {
    val selectedDate = selectedDateState.value
    val parts = timeInput.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 12
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val dateTime = selectedDate.atTime(hour, minute)
    return Date.from(dateTime.atZone(zoneId).toInstant())
  }

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())
}
