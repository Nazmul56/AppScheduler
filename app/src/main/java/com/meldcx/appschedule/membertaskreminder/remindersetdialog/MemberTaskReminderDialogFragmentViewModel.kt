package com.meldcx.appschedule.membertaskreminder.remindersetdialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class AudioRecorderEnum {
    RECORD_START,
    RECORD_STOP,
    RECORD_PAUSE,
    RECORD_RESUME
}

class MemberTaskReminderDialogFragmentViewModel :
    ViewModel() {

    companion object {
        val TAG = "MemberTaskReminderDialogFragmentViewModel"
    }

    // Private mutable LiveData for internal updates
    private val _audioRecorderStatus = MutableLiveData<AudioRecorderEnum>(AudioRecorderEnum.RECORD_STOP)

    //Public audio recorder
    val audioRecorderStatus: LiveData<AudioRecorderEnum> =  _audioRecorderStatus
    fun updateAudioRecorderStatus(newStatus: AudioRecorderEnum) {
        _audioRecorderStatus.value = newStatus
    }

    private val _imagePath = MutableLiveData("")
    val imagePath: LiveData<String> = _imagePath
    fun updateImagePath(newPath: String) {
        _imagePath.value = newPath
    }
    
    //Error Message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
}
