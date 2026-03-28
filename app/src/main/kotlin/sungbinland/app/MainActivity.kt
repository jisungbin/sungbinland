package sungbinland.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import sungbinland.app.navigation.AppNavHost
import sungbinland.core.study.StudyDatabase
import sungbinland.core.workout.WorkoutDatabase

public class MainActivity : ComponentActivity() {
  private val studyDatabase by lazy { StudyDatabase.getOrCreate(context = applicationContext) }
  private val workoutDatabase by lazy { WorkoutDatabase.getOrCreate(context = applicationContext) }

  public override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val studyEntryDao = studyDatabase.studyEntryDao()

    val timerRecordDao = workoutDatabase.timerRecordDao()
    val workoutSessionDao = workoutDatabase.workoutSessionDao()
    val workoutRoutineDao = workoutDatabase.workoutRoutineDao()
    val workoutExerciseDao = workoutDatabase.workoutExerciseDao()

    setContent {
      AppNavHost(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFFFAF8F5)),
        studyEntryDao = studyEntryDao,
        timerRecordDao = timerRecordDao,
        workoutSessionDao = workoutSessionDao,
        workoutRoutineDao = workoutRoutineDao,
        workoutExerciseDao = workoutExerciseDao,
      )
    }
  }
}
