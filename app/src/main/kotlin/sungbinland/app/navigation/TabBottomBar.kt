package sungbinland.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation3.runtime.NavKey
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.collections.immutable.ImmutableList
import sungbinland.uikit.IbmPlexSansKr

@Composable internal fun TabBottomBar(
  hazeState: HazeState,
  tabs: ImmutableList<NavKey>,
  selectedTab: NavKey,
  accentOf: (NavKey) -> Color,
  iconOf: (NavKey) -> ImageVector,
  labelOf: (NavKey) -> String,
  modifier: Modifier = Modifier,
  onTabClick: (NavKey) -> Unit,
) {
  val currentSelectedTab by rememberUpdatedState(selectedTab)
  val selectedIndex by remember(tabs) { derivedStateOf { tabs.indexOf(currentSelectedTab) } }
  val animatedAccent by animateColorAsState(
    targetValue = accentOf(tabs[selectedIndex]),
    animationSpec = spring(
      dampingRatio = 0.82f,
      stiffness = Spring.StiffnessMediumLow,
    ),
  )

  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .clip(CircleShape)
        .hazeEffect(
          state = hazeState,
          style = HazeStyle(
            backgroundColor = Color.Transparent,
            tint = HazeTint(Color.White.copy(alpha = 0.10f)),
            blurRadius = 30.dp,
          ),
        )
        .background(Color(0x2212171C))
    )
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      tabs.fastForEachIndexed { index, tab ->
        val selected = selectedIndex == index
        val itemScale by animateFloatAsState(
          targetValue = if (selected) 1f else 0.96f,
          animationSpec = spring(
            dampingRatio = 0.88f,
            stiffness = Spring.StiffnessMedium,
          ),
        )
        val contentColor by animateColorAsState(if (selected) Color.White else Color.White.copy(alpha = 0.62f))

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(CircleShape)
            .background(color = if (selected) animatedAccent.copy(alpha = 0.26f) else Color.Transparent)
            .clickable { onTabClick(tab) },
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Image(
            modifier = Modifier
              .height(18.dp)
              .fillMaxWidth(itemScale),
            painter = rememberVectorPainter(iconOf(tab)),
            contentDescription = labelOf(tab),
            colorFilter = ColorFilter.tint(contentColor),
          )
          BasicText(
            text = labelOf(tab),
            style = TextStyle(
              fontFamily = IbmPlexSansKr,
              color = contentColor,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.4.sp,
            ),
          )
        }
      }
    }
  }
}
