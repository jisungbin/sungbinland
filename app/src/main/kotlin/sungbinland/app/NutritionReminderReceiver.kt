package sungbinland.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import sungbinland.core.alarm.DailyAlarmScheduler
import sungbinland.core.alarm.HapticFeedback

public class NutritionReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    HapticFeedback.vibrateHeavy(context)
    val activityIntent = Intent(context, NutritionReminderActivity::class.java)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(activityIntent)
    DailyAlarmScheduler.schedule(
      context = context,
      hour = 15,
      minute = 0,
      receiverClass = NutritionReminderReceiver::class.java,
    )
  }
}
