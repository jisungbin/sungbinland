package sungbinland.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public class NutritionReminderActivity : ComponentActivity() {
  public override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    setShowWhenLocked(true)
    setTurnScreenOn(true)
    super.onCreate(savedInstanceState)
    setContent {
      NutritionReminderScreen(onDismiss = ::finish)
    }
  }
}

@Composable private fun NutritionReminderScreen(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAF8F5))
      .systemBarsPadding(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      BasicText(
        text = "섭취 안내",
        style = TextStyle(
          color = Color(0xFF1C1C1C),
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
        ),
      )
      BasicText(
        text = "단백질과 탄수화물을\n섭취할 시간입니다",
        style = TextStyle(
          color = Color(0xFF1C1C1C),
          fontSize = 20.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          lineHeight = 28.sp,
        ),
      )
      BasicText(
        text = "오후 3시 영양소 섭취를 잊지 마세요",
        style = TextStyle(
          color = Color(0xFF999999),
          fontSize = 14.sp,
          textAlign = TextAlign.Center,
        ),
      )
      Box(modifier = Modifier.height(24.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .background(Color(0xFF1E3A5F), RoundedCornerShape(14.dp))
          .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
      ) {
        BasicText(
          text = "확인",
          style = TextStyle(
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          ),
        )
      }
    }
  }
}
