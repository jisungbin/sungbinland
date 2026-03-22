package sungbinland.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import sungbinland.core.alarm.HapticFeedback

public class RestTimerAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakeLock = powerManager.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      "sungbinland:rest_timer",
    )
    wakeLock.acquire(10_000L)
    try {
      HapticFeedback.vibrateHeavy(context)
      val activityIntent = Intent(context, RestTimerCompleteActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(activityIntent)
    } finally {
      wakeLock.release()
    }
  }
}
