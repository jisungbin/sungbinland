package sungbinland.app

import android.app.Application

public class SungbinlandApp : Application() {
  override fun onCreate() {
    super.onCreate()

    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      val prefs = getSharedPreferences("crash_logs", MODE_PRIVATE)
      val timestamp = System.currentTimeMillis()
      val log = buildString {
        appendLine("Thread: ${thread.name}")
        append(throwable.stackTraceToString())
      }
      prefs.edit().putString("crash_$timestamp", log).commit()
      defaultHandler?.uncaughtException(thread, throwable)
    }
  }
}
