package sungbinland.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sungbinland.app.navigation.AppNavHost
import sungbinland.core.fixture.DatabaseFixture
import sungbinland.core.nutrition.NutritionDatabase
import sungbinland.core.study.StudyDatabase
import sungbinland.core.workout.WorkoutDatabase

public class MainActivity : ComponentActivity() {
  private val nutritionDatabase by lazy { NutritionDatabase.getOrCreate(context = applicationContext) }
  private val studyDatabase by lazy { StudyDatabase.getOrCreate(context = applicationContext) }
  private val workoutDatabase by lazy { WorkoutDatabase.getOrCreate(context = applicationContext) }

  public override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    MainScope().launch {
      val inserted = withContext(Dispatchers.IO) {
        DatabaseFixture(
          nutritionDatabase = nutritionDatabase,
          studyDatabase = studyDatabase,
          workoutDatabase = workoutDatabase,
        ).populate()
      }
      if (inserted) {
        Toast.makeText(applicationContext, "Fixture 데이터 삽입 완료", Toast.LENGTH_SHORT).show()
      }
    }

    val bodyInfoDao = nutritionDatabase.bodyInfoDao()
    val eatenFoodDao = nutritionDatabase.eatenFoodDao()
    val foodDao = nutritionDatabase.foodDao()

    val studyEntryDao = studyDatabase.studyEntryDao()

    val supplementDao = workoutDatabase.supplementDao()
    val supplementIntakeDao = workoutDatabase.supplementIntakeDao()
    val timerRecordDao = workoutDatabase.timerRecordDao()
    val workoutSessionDao = workoutDatabase.workoutSessionDao()

    setContent {
      AppNavHost(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFFFAF8F5)),
        bodyInfoDao = bodyInfoDao,
        eatenFoodDao = eatenFoodDao,
        foodDao = foodDao,
        studyEntryDao = studyEntryDao,
        supplementDao = supplementDao,
        supplementIntakeDao = supplementIntakeDao,
        timerRecordDao = timerRecordDao,
        workoutSessionDao = workoutSessionDao,
      )
    }
  }
}
