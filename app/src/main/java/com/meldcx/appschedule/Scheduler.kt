package com.meldcx.appschedule
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class Scheduler(private val context: Context) {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleApp(appSchedule: AppSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LaunchReceiver::class.java).apply {
            putExtra("packageName", appSchedule.packageName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appSchedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(appSchedule.launchTime.toString())
            if (date != null) {
                val now = Calendar.getInstance()
                val schedule = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.YEAR, now.get(Calendar.YEAR))
                    set(Calendar.MONTH, now.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

                    if (before(now)) {
                        add(Calendar.DAY_OF_MONTH, 1) // next day if time already passed
                    }
                }
                set(Calendar.HOUR_OF_DAY, schedule.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, schedule.get(Calendar.MINUTE))
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d("Scheduler", "Scheduled ${appSchedule.appLabel} at ${appSchedule.launchTime}")
    }

    fun cancelSchedule(appSchedule: AppSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LaunchReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appSchedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        Log.d("Scheduler", "Cancelled schedule for ${appSchedule.appLabel}")
    }

    fun updateSchedule(oldSchedule: AppSchedule, newSchedule: AppSchedule) {
        cancelSchedule(oldSchedule)
        scheduleApp(newSchedule)
    }

    fun logExecution(appSchedule: AppSchedule) {
        val prefs = context.getSharedPreferences("execution_logs", Context.MODE_PRIVATE)
        val timestamp = System.currentTimeMillis()
        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        prefs.edit().putString(appSchedule.id.toString(), "Launched at $formattedTime").apply()
        Log.d("Scheduler", "Logged execution of ${appSchedule.appLabel}")
    }

    fun getExecutionLog(appId: Int): String? {
        val prefs = context.getSharedPreferences("execution_logs", Context.MODE_PRIVATE)
        return prefs.getString(appId.toString(), null)
    }
}
