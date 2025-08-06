package com.meldcx.appschedule.membertaskreminder.reminderaudioplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bracits.smartpo.base.BaseViewModel
import com.bracits.smartpo.po.ui.membertaskreminder.helper.AudioPlayerEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MemberTaskVoiceNotePlayerDialogFragmentViewModel
@Inject constructor() :
    BaseViewModel() {

    companion object {
        val TAG = "MemberTaskVoiceNotePlayerDialogFragmentViewModel"
    }

    // Private mutable LiveData for internal updates
    private val _audioPlayerStatus = MutableLiveData(AudioPlayerEnum.PLAYER_STOP)

    // Public immutable LiveData for observers
    val audioPlayerStatus: LiveData<AudioPlayerEnum> = _audioPlayerStatus

    // Method to update the status
    fun updateAudioPlayerStatus(newStatus: AudioPlayerEnum) {
        _audioPlayerStatus.value = newStatus
    }

    //Error Message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
}
