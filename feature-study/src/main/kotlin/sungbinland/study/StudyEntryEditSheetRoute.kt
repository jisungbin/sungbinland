package sungbinland.study

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable public data class StudyEntryEditSheetRoute(
  public val category: String,
  public val name: String,
) : NavKey
