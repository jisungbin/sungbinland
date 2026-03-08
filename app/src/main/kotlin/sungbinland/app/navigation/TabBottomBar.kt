package sungbinland.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.graphics.vector.ImageVector

@Composable internal fun TabBottomBar(
  tabs: List<NavKey>,
  selectedTab: NavKey,
  iconOf: (NavKey) -> ImageVector,
  labelOf: (NavKey) -> String,
  modifier: Modifier = Modifier,
  onTabClick: (NavKey) -> Unit,
) {
  NavigationBar(modifier = modifier) {
    tabs.fastForEach { tab ->
      NavigationBarItem(
        selected = selectedTab == tab,
        onClick = { onTabClick(tab) },
        icon = { Icon(imageVector = iconOf(tab), contentDescription = labelOf(tab)) },
        label = { Text(text = labelOf(tab)) },
      )
    }
  }
}
