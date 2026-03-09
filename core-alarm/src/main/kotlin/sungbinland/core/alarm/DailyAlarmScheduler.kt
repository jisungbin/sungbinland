package sungbinland.core.alarm

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import java.util.Calendar

public object DailyAlarmScheduler {
  public fun schedule(
    context: Context,
    hour: Int,
    minute: Int,
    receiverClass: Class<out BroadcastReceiver>,
  ) {
    requestBatteryOptimizationExemption(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, receiverClass)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val triggerAtMillis = nextTriggerMillis(hour = hour, minute = minute)
    alarmManager.setAlarmClock(
      AlarmClockInfo(triggerAtMillis, pendingIntent),
      pendingIntent,
    )
  }

  private fun nextTriggerMillis(hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (before(Calendar.getInstance())) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }
    return calendar.timeInMillis
  }

  private fun requestBatteryOptimizationExemption(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
      val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        .setData(Uri.parse("package:${context.packageName}"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    }
  }
}
