package com.meldcx.appschedule.membertaskreminder.remindersetdialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MemberTaskReminderDialogFragmentViewModel :
    ViewModel() {

    companion object {
        val TAG = "MemberTaskReminderDialogFragmentViewModel"
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
