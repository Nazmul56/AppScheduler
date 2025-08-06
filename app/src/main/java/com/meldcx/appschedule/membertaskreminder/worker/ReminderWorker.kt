package com.meldcx.appschedule.membertaskreminder.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.meldcx.appschedule.membertaskreminder.constant.Constant
import com.meldcx.appschedule.membertaskreminder.helper.NotificationHelper

class ReminderWorker (context: Context, params: WorkerParameters) : Worker(context, params) {

    private val TAG = "MemberTaskReminderWorker"

    override fun doWork(): Result {
        // Retrieve the input data
        val reminderId = inputData.getString(Constant.KEY_REMINDER_ID)
        val reminderTriggerTime = inputData.getLong(Constant.KEY_REMINDER_TRIGGER_TIME, -1)
        val reminderTitle = "App Scheduler"

        val notificationMsg = "Message"  //"${reminderModel.reminderReason}${if (reminderModel.reminderAudioFileUri.isEmpty()) "" else " [${ applicationContext.getString(R.string.member_task_reminder_voice_note)}]"} ${if (reminderModel.reminderImageFilePath.isEmpty()) "" else " [${applicationContext.getString(R.string.image)}]"}"
        NotificationHelper.showNotification(
            applicationContext,
            reminderTitle,
            notificationMsg,
            reminderId,
            "")//reminderModel.reminderImageFilePath
        return Result.success()
    }
}
