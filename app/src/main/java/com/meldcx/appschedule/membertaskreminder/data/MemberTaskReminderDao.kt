package com.meldcx.appschedule.membertaskreminder.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID

@Dao
interface MemberTaskReminderDao {

    @Query(
        """
            SELECT  * FROM reminder_table pa ORDER BY reminderCreationTime DESC
        """
    )
    fun getMemberTaskReminders(): LiveData<List<MemberTaskReminderEntity>>

    @Query(
        """
            SELECT * FROM reminder_table pa WHERE id = :id
        """
    )
    fun getMemberTaskReminder(id: UUID): MemberTaskReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminder(note: MemberTaskReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllReminders(notes: ArrayList<MemberTaskReminderEntity>)

    @Query("UPDATE reminder_table SET systemReminderUUID = :reminderSystemId WHERE id = :id")
    fun updateReminderSystemId(id: UUID, reminderSystemId: UUID)

    @Query("UPDATE reminder_table SET isSystemReminderTriggered = :systemTriggered WHERE id = :id")
    fun updateReminderPlayed(id: UUID, systemTriggered: Boolean)

    @Query("DELETE FROM reminder_table WHERE id = :id")
    fun deleteReminder(id: UUID)

}