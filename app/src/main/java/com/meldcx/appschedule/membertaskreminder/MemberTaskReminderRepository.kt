package com.meldcx.appschedule.membertaskreminder

import androidx.lifecycle.LiveData
import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderDao
import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderEntity
import java.util.UUID
import javax.inject.Inject

class MemberTaskReminderRepository @Inject constructor(private val memberNoteDao: MemberTaskReminderDao) : SafeApiCall {

    companion object{
        val TAG = MemberTaskReminderRepository::class.simpleName
    }

    fun getMemberTaskReminders(projectId: Long): LiveData<List<MemberTaskReminderEntity>> {
        return memberNoteDao.getMemberTaskReminders()
    }

    fun getMemberTaskReminder(reminderId: UUID): MemberTaskReminderEntity? {
        return memberNoteDao.getMemberTaskReminder(reminderId)
    }

    fun addNewReminder(note: MemberTaskReminderEntity): Long {
        return memberNoteDao.insertReminder(note)
    }

    fun updateSystemReminderId(id: UUID, systemReminderId: UUID) {
        return memberNoteDao.updateReminderSystemId(id, systemReminderId)
    }

    fun deleteReminder(id: UUID) {
        return memberNoteDao.deleteReminder(id)
    }
}