package sungbinland.nutrition

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sungbinland.core.nutrition.dao.BodyInfoDao
import sungbinland.core.nutrition.entity.BodyInfoEntity

internal class NutritionStateHolder(
  private val mapper: NutritionDashboardStateMapper,
  private val bodyInfoDao: BodyInfoDao,
  private val nowProvider: () -> LocalDate,
) {
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val refreshState: MutableStateFlow<Long> = MutableStateFlow(0L)
  private val selectedDateState: MutableStateFlow<LocalDate> = MutableStateFlow(nowProvider())
  private val zoneId: ZoneId = ZoneId.systemDefault()

  internal val state: StateFlow<NutritionDashboardState> = scope.launchMolecule(
    mode = RecompositionMode.Immediate,
  ) {
    val selectedDate by selectedDateState.collectAsState()
    val refreshKey by refreshState.collectAsState()
    val dashboardState by produceState(
      initialValue = nutritionDashboardLoadingState(selectedDate = selectedDate),
      selectedDate,
      refreshKey,
    ) {
      value = mapper.createState(selectedDate = selectedDate)
    }
    dashboardState
  }

  internal fun moveToNextDate() {
    selectedDateState.update { selectedDate -> selectedDate.plusDays(1) }
  }

  internal fun moveToPreviousDate() {
    selectedDateState.update { selectedDate -> selectedDate.minusDays(1) }
  }

  internal fun moveToToday() {
    selectedDateState.update { nowProvider() }
  }

  internal fun saveBodyWeight(weightInput: String) {
    val weightKg = weightInput.toIntOrNull() ?: return
    if (weightKg <= 0) {
      return
    }
    scope.launch {
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

  internal fun refresh() {
    refreshState.update { key -> key + 1L }
  }

  internal fun close() {
    scope.cancel()
  }

  private fun LocalDate.toStartOfDayDate(): Date =
    Date.from(atStartOfDay(zoneId).toInstant())
}
