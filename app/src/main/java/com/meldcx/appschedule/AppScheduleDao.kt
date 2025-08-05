package com.meldcx.appschedule

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface AppScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY launchTime ASC")
    fun getAllSchedules(): LiveData<List<AppSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: AppSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: AppSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: AppSchedule)

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): AppSchedule?

    @Query("SELECT * FROM schedules WHERE executed = 0 AND launchTime > :currentTime ORDER BY launchTime ASC")
    suspend fun getPendingSchedules(currentTime: Date): List<AppSchedule>

    @Query("UPDATE schedules SET executed = 1 WHERE id = :id")
    suspend fun markAsExecuted(id: Long)
}
