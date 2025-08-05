package com.meldcx.appschedule
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LaunchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val packageName = intent?.getStringExtra("packageName")
        val scheduleId = intent?.getLongExtra("scheduleId", -1L) ?: -1L

        if (packageName.isNullOrBlank() || scheduleId == -1L) {
            Log.e("LaunchReceiver", "Invalid package name or schedule ID")
            return
        }

        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Log.d("LaunchReceiver", "App launched: $packageName")
            } else {
                Log.e("LaunchReceiver", "App not found: $packageName")
            }

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                db.appScheduleDao().markAsExecuted(scheduleId)
                Log.d("LaunchReceiver", "Marked as executed: $scheduleId")
            }

        } catch (e: Exception) {
            Log.e("LaunchReceiver", "Failed to launch app", e)
        }
    }
}
