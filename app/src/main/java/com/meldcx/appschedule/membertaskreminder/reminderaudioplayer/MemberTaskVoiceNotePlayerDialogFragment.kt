package com.meldcx.appschedule.membertaskreminder.reminderaudioplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bracits.smartpo.po.ui.membertaskreminder.helper.AudioPlayerEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

@AndroidEntryPoint
class MemberTaskVoiceNotePlayerDialogFragment : BaseDialogFragment() {
    private var _binding: MemberTaskVoiceNotePlayerDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MemberTaskVoiceNotePlayerDialogFragmentViewModel

    var memberId: Long = 0
    var date: Calendar = Calendar.getInstance()

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    private val MAX_RECORDING_TIME = 31 * 1000
    private var newRecordFilePath: String = ""
    private var playerProgressTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberId = requireArguments().getLong(Constant.KEY_MEMBER_ID)
        newRecordFilePath = requireArguments().getString(Constant.KEY_REMINDER_AUDIO_PATH).toString()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPlayer()
    }

    override fun fragmentDialogView(
        inflater: LayoutInflater?, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MemberTaskVoiceNotePlayerDialogFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MemberTaskVoiceNotePlayerDialogFragmentViewModel::class.java]

        binding.btnDiscard.setOnClickListener { dismiss() }

        binding.toggleRecordingAnimButton.setOnClickListener {
            if (viewModel.audioPlayerStatus.value == AudioPlayerEnum.PLAYER_STOP)
                startPlayer()
            else if (viewModel.audioPlayerStatus.value  == AudioPlayerEnum.PLAYER_START)
                stopPayer()
        }

        listenControllerObserver()
        return binding.root
    }

    private fun listenControllerObserver() {
        viewModel.audioPlayerStatus.observe(this) { playerStatus ->
            when (playerStatus) {
                AudioPlayerEnum.PLAYER_START -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_pause_anim.json", LottieDrawable.INFINITE)
                }
                AudioPlayerEnum.PLAYER_STOP -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_play_anim.json", 0)
                } else ->{
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_play_anim.json", 0)
                }
            }
        }
    }

    //Player Code start
    private fun startPlayer() {
        val myFile = File(newRecordFilePath)
        if (myFile.exists()) {
            player.startPlaying(newRecordFilePath)
            viewModel.updateAudioPlayerStatus(AudioPlayerEnum.PLAYER_START)
            startPlayerProgressTimer()
        } else {
            Toast.makeText(requireContext(), getString(R.string.not_found), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopPayer() {
        player.stop()
        viewModel.updateAudioPlayerStatus(AudioPlayerEnum.PLAYER_STOP)
        stopPlayerProgressTimer()
    }

    private fun startPlayerProgressTimer() {
        playerProgressTimer.cancel()
        playerProgressTimer = Timer()
        binding.playerProgressBar.progress = 0
        binding.playerProgressBar.max = player.getAudioDuration() / 1000
        playerProgressTimer.schedule(getPlayerProgressUpdateTask(), 500, 500)
    }

    private fun stopPlayerProgressTimer() {
        playerProgressTimer.cancel()
    }

    private fun getPlayerProgressUpdateTask() = object : TimerTask() {
        val startTime = System.currentTimeMillis()
        override fun run() {
            try {
                val elapsedTime = System.currentTimeMillis() - startTime
                val recAudioDuration = player.getAudioDuration()
                val recAudioCurrentPos = player.getCurrentPosition()

                Log.d(
                    TAG,
                    "Recorded Audio Current pos: $recAudioCurrentPos recorded duration: $recAudioDuration"
                )
                Handler(Looper.getMainLooper()).post {
                    if (elapsedTime <= MAX_RECORDING_TIME && recAudioCurrentPos < recAudioDuration) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val progress =
                                (recAudioCurrentPos / 1000.toDouble()).roundToInt().toInt()
                            binding.playerProgressBar.progress = progress

                            val seconds = elapsedTime / 1000
                            val formattedTime =
                                String.format(Locale.getDefault(), "%d:%02d", 0, seconds)
                            binding.tvTimer.text = formattedTime
                        }
                    } else {
                        stopPayer()
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaPlayer", "Illegal state: ${e.message}")
            }
        }
    }
    //Player Code End

    private fun showLottiAnim(view: LottieAnimationView, fileName: String, repeatCount: Int) {
        view.cancelAnimation()
        view.setAnimation(fileName);
        view.repeatCount = repeatCount
        view.playAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }

    companion object {
        private const val TAG = "MemberTaskReminderDialogFragment"
    }
}
