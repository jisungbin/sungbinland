package sungbinland.core.alarm

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

public object RestTimerAlarmScheduler {
  private const val REQUEST_CODE = 7777

  public fun schedule(
    context: Context,
    delayMillis: Long,
    receiverClass: Class<out BroadcastReceiver>,
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, receiverClass)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQUEST_CODE,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val triggerAtMillis = System.currentTimeMillis() + delayMillis
    alarmManager.setAlarmClock(
      AlarmClockInfo(triggerAtMillis, pendingIntent),
      pendingIntent,
    )
  }

  public fun cancel(
    context: Context,
    receiverClass: Class<out BroadcastReceiver>,
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, receiverClass)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQUEST_CODE,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }
}
