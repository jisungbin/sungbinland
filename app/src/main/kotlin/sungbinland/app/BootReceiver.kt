package sungbinland.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import sungbinland.core.alarm.DailyAlarmScheduler

public class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      DailyAlarmScheduler.schedule(
        context = context.applicationContext,
        hour = 15,
        minute = 0,
        receiverClass = NutritionReminderReceiver::class.java,
      )
    }
  }
}
