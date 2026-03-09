package sungbinland.core.alarm

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

public object HapticFeedback {
  public fun vibrateHeavy(context: Context) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    val vibrator = vibratorManager.defaultVibrator
    vibrator.vibrate(VibrationEffect.createOneShot(300L, 255))
  }
}
