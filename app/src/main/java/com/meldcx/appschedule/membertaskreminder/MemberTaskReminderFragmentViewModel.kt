package com.meldcx.appschedule.membertaskreminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MemberTaskReminderFragmentViewModel
@Inject constructor(
    private val memberTaskReminderRepository: MemberTaskReminderRepository
) :
    BaseViewModel() {

    companion object {
        val TAG = "MemberTaskReminderRepository"
    }

    fun getAllReminders(projectId: Long): LiveData<List<MemberTaskReminderEntity>> {
        return memberTaskReminderRepository.getMemberTaskReminders(projectId)
    }

    fun getMemberTaskReminder(reminderId: UUID): MemberTaskReminderEntity? {
        return memberTaskReminderRepository.getMemberTaskReminder(reminderId)
    }
    fun deleteReminder(id: UUID) {
        return memberTaskReminderRepository.deleteReminder(id)
    }

    //Error Message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
}
