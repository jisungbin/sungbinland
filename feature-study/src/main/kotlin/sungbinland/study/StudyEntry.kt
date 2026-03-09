package sungbinland.study

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
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
import sungbinland.core.study.dao.StudyEntryDao
import sungbinland.core.study.entity.StudyEntryEntity
import sungbinland.uikit.BottomSheetSceneStrategy
import sungbinland.uikit.FloatingButtonState
import sungbinland.uikit.LocalFabController

public fun EntryProviderScope<NavKey>.studyEntry(
  studyEntryDao: StudyEntryDao,
  onNavigate: (NavKey) -> Unit,
  onBack: () -> Unit,
) {
  val mapper = StudyDashboardStateMapper(studyEntryDao = studyEntryDao)
  val factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return StudyViewModel(mapper = mapper, studyEntryDao = studyEntryDao) as T
    }
  }

  entry<StudyRoute>(
    metadata = mapOf("label" to "스터디"),
  ) {
    val viewModel = viewModel<StudyViewModel>(factory = factory)
    val fabController = LocalFabController.current
    val fabState = remember {
      FloatingButtonState(icon = Icons.AutoMirrored.Rounded.MenuBook, onClick = { onNavigate(StudyEntryRegistrationSheetRoute) })
    }
    SideEffect { fabController.set(StudyRoute, fabState) }
    StudyScreen(
      viewModel = viewModel,
      onEntryClick = { category, name ->
        onNavigate(StudyEntryEditSheetRoute(category = category, name = name))
      },
    )
  }
  entry<StudyEntryRegistrationSheetRoute>(
    metadata = BottomSheetSceneStrategy.bottomSheet(),
  ) {
    val viewModel = viewModel<StudyViewModel>(factory = factory)
    var categories: ImmutableList<String> by remember { mutableStateOf(persistentListOf()) }
    LaunchedEffect(Unit) {
      categories = viewModel.getCategories().toImmutableList()
    }
    StudyEntryRegistrationSheet(
      categories = categories,
      onSubmit = { category, name, content ->
        viewModel.registerEntry(category = category, name = name, content = content)
        onBack()
      },
    )
  }
  entry<StudyEntryEditSheetRoute>(
    metadata = BottomSheetSceneStrategy.bottomSheet(),
  ) { route ->
    val viewModel = viewModel<StudyViewModel>(factory = factory)
    var entry: StudyEntryEntity? by remember { mutableStateOf(null) }
    LaunchedEffect(route.category, route.name) {
      entry = viewModel.getEntry(category = route.category, name = route.name)
    }
    val loadedEntry = entry
    if (loadedEntry != null) {
      StudyEntryEditSheet(
        initialCategory = loadedEntry.category,
        initialName = loadedEntry.name,
        initialContent = loadedEntry.content,
        onSubmit = { newCategory, newName, newContent ->
          viewModel.updateEntry(
            oldCategory = route.category,
            oldName = route.name,
            newCategory = newCategory,
            newName = newName,
            content = newContent,
          )
          onBack()
        },
      )
    }
  }
}
