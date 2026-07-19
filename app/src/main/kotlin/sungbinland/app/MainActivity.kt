package sungbinland.app

import android.app.Activity
import android.os.Bundle
import sungbinland.workout.ui.WorkoutViewHandle
import sungbinland.workout.ui.installWorkoutView

public class MainActivity : Activity() {
  private var handle: WorkoutViewHandle? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handle = installWorkoutView(this)
  }

  public override fun onDestroy() {
    handle?.dispose()
    handle = null
    super.onDestroy()
  }
}
