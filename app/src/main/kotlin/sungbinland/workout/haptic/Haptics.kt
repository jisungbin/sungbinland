package sungbinland.workout.haptic

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

internal object Haptics {
  // 아래 웨이브폼 총 길이(400·5 + 100·4 = 2400ms). 콘페티 지속시간과 공유.
  const val VIBRATION_MILLIS: Long = 2400L

  fun vibrateHeavy(context: Context) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibratorManager.defaultVibrator.vibrate(
      VibrationEffect.createWaveform(
        longArrayOf(0, 400, 100, 400, 100, 400, 100, 400, 100, 400),
        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255),
        -1,
      ),
    )
  }
}
