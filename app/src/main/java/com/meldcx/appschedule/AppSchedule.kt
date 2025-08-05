package com.meldcx.appschedule

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")

data class AppSchedule(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,      // The package name of the app to launch
    val appLabel: String,         // The display name of the app
    val launchTime: String,         // Scheduled launch time in milliseconds (epoch time)
    val executed: Boolean = false, // Whether the app was successfully launched
    val appName: String
)
