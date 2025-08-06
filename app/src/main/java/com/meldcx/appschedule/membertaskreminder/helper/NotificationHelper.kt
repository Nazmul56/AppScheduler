package com.meldcx.appschedule.membertaskreminder.helper

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bracits.smartpo.po.SmartMfMainActivity
import com.bumptech.glide.Glide
import com.meldcx.appschedule.MainActivity

object NotificationHelper {
    private val TAG  = "NotificationHelper"
    private const val CHANNEL_ID = "REMINDER_CHANNEL"
    private const val CHANNEL_NAME = "Reminder Notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for reminder notifications"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, reminderId: String?, imagePath: String?) {

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Create a PendingIntent that wraps the Intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set the PendingIntent to open the Activity
            .build()

        if(!imagePath.isNullOrEmpty()){
            // Using Glide to load a bitmap
            val imageBitmap = Glide.with(context)
                .asBitmap()
                .load(imagePath)
                .submit()
                .get()

            notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Set the PendingIntent to open the Activity
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap) // The image to display
                ).build()
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission is missing")
            } else {
                Log.d(TAG, "Notification triggered")
                val notificationId = reminderId.hashCode()
                notify(notificationId, notification)
            }
        }
    }
}
