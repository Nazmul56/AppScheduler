package com.meldcx.appschedule.membertaskreminder.worker

import android.R
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.meldcx.appschedule.AppDatabase
import com.meldcx.appschedule.membertaskreminder.constant.Constant
import com.meldcx.appschedule.membertaskreminder.helper.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class MemberTaskReminderWorker (context: Context, params: WorkerParameters) : Worker(context, params) {

    private val TAG = "MemberTaskReminderWorker"
    //var masterDatabase: MasterDatabase = MasterDatabase.getDatabase(context)
    val masterDatabase = AppDatabase.getInstance(context)

    override fun doWork(): Result {

        // Retrieve the input data
        val reminderId = inputData.getString(Constant.KEY_REMINDER_ID)
        val reminderTriggerTime = inputData.getLong(Constant.KEY_REMINDER_TRIGGER_TIME, -1)
        val reminderTitle = applicationContext.resources.getString(R.string.member_task_reminder_txt)

        //Convert String to UUID
        val reminderUUID: UUID = UUID.fromString(reminderId)
        Log.d(TAG, "Reminder ID: $reminderId Trigger Time: $reminderTriggerTime")

        val reminderModel = masterDatabase.memberTaskReminderDao().getMemberTaskReminder(reminderUUID)

        if(reminderModel == null){
            Log.d(TAG, "Reminder Deleted from db")
            return Result.success()
        }

        masterDatabase.memberTaskReminderDao().updateReminderPlayed(reminderUUID, true)
        Log.d(TAG, "Reminder From DB: ${reminderModel.id} reminder reason: ${reminderModel.reminderReason} Trigger Time: ${reminderModel.reminderTriggerAt}")

        var reminderTriggeredAt = SimpleDateFormat(DATE_FORMAT_ISO, Locale.getDefault()).format(TrueTime.now())
        if(reminderModel.reminderTriggerAt != null)
            reminderTriggeredAt = SimpleDateFormat(DATE_FORMAT_ISO, Locale.getDefault()).format(reminderModel.reminderTriggerAt)
        val randomId = Random.nextLong(100000, 1000000)

        val notification = NotificationModel(randomId , reminderModel.reminderReason, applicationContext.getString(R.string.member_task_reminder_txt), reminderModel.reminderImageFilePath, null , null, false, null, APP_TYPE, null, "", reminderId,
            PushNotificationTypeLocal.LOCAL_REMINDER.toString(), null,
            reminderTriggeredAt
        )
        masterDatabase.notificationDao().insertCentralAlert(notification)

        val notificationMsg = "${reminderModel.reminderReason}${if (reminderModel.reminderAudioFileUri.isEmpty()) "" else " [${ applicationContext.getString(R.string.member_task_reminder_voice_note)}]"} ${if (reminderModel.reminderImageFilePath.isEmpty()) "" else " [${applicationContext.getString(R.string.image)}]"}"
        NotificationHelper.showNotification(
            applicationContext,
            reminderTitle,
            notificationMsg,
            reminderId,
            reminderModel.reminderImageFilePath)
        return Result.success()
    }
}
