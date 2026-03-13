package sungbinland.core.alarm

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

public object HapticFeedback {
  public fun vibrateHeavy(context: Context) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    val vibrator = vibratorManager.defaultVibrator
    vibrator.vibrate(
      VibrationEffect.createWaveform(
        longArrayOf(0, 400, 100, 400, 100, 400, 100, 400),
        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
        -1,
      ),
    )
  }
}
