package com.meldcx.appschedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling pending alarms...")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = AppDatabase.getInstance(context).appScheduleDao()
                    val scheduler = Scheduler(context)
                    val pendingSchedules = dao.getPendingSchedules(Date())

                    for (schedule in pendingSchedules) {
                        scheduler.scheduleApp(schedule)
                    }

                    Log.d("BootReceiver", "Rescheduled ${pendingSchedules.size} pending alarms.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule alarms", e)
                }
            }
        }
    }
}
