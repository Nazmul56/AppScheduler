package com.meldcx.appschedule.membertaskreminder.rememberdetails

import android.net.Uri
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
import com.bracits.mf.common.utils.Constant
import com.bracits.smartpo.R
import com.bracits.smartpo.base.BaseDialogFragment
import com.bracits.smartpo.databinding.FragmentMemberTaskReminderDetailsBinding
import com.bracits.smartpo.po.ui.membertaskreminder.helper.AudioPlayerEnum
import com.bracits.smartpo.service.audioplayer.playback.AndroidAudioPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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
class MemberTaskReminderDetailsFragment : BaseDialogFragment() {
    private var _binding: FragmentMemberTaskReminderDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MemberTaskReminderDetailsFragmentViewModel

    var date: Calendar = Calendar.getInstance()

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    private val MAX_RECORDING_TIME = 31 * 1000

    private var memberId: Long = 0
    private var memberName: String = ""
    private var audioFilePath: String = ""
    private var imageFilePath: String = ""
    private var reminderReason: String = ""
    private var reminderSetTime: String = ""
    private var playerProgressTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberId = requireArguments().getLong(Constant.KEY_MEMBER_ID)
        memberName = requireArguments().getString(Constant.KEY_MEMBER_NAME) ?: ""
        imageFilePath = requireArguments().getString(Constant.KEY_REMINDER_IMAGE_PATH) ?: ""
        audioFilePath = requireArguments().getString(Constant.KEY_REMINDER_AUDIO_PATH) ?: ""
        reminderReason = requireArguments().getString(Constant.KEY_REMINDER_REASON) ?: ""
        reminderSetTime = requireArguments().getString(Constant.KEY_REMINDER_SET_TIME) ?: ""
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun fragmentDialogView(
        inflater: LayoutInflater?, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberTaskReminderDetailsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MemberTaskReminderDetailsFragmentViewModel::class.java]

        binding.tvReminderReason.text = reminderReason

        if (!imageFilePath.isEmpty()) {
            binding.clImageViewer.visibility = View.VISIBLE
            setImage(imageFilePath)
        } else {
            binding.clImageViewer.visibility = View.GONE
        }

        if (!audioFilePath.isEmpty()) {
            binding.clReminderAudioPlayer.visibility = View.VISIBLE
        } else {
            binding.clReminderAudioPlayer.visibility = View.GONE
        }

        if (!reminderSetTime.isEmpty()) {
            binding.tvReminderSetTime.text = reminderSetTime
        }

        binding.btnDiscard.setOnClickListener { dismiss() }

        if (memberId > 1) { // If Reminder not tagged to any member than member id will 1
            binding.btnMemberDetails.visibility = View.VISIBLE
        } else {
            binding.btnMemberDetails.visibility = View.GONE
        }

        if (!memberName.isEmpty()) {
            binding.tvMember.visibility = View.VISIBLE
            val title = "for $memberName"
            binding.tvMember.text = title
        } else {
            binding.tvMember.visibility = View.GONE
        }

        binding.btnMemberDetails.setOnClickListener {
            dismiss()
            navController.navigate(
                MemberTaskReminderDetailsFragmentDirections
                    .actionFavouriteMemberFragmentToMemberDetailsFragmentV3(memberId), navOptions
            )
        }

        binding.toggleRecordingAnimButton.setOnClickListener {
            if (viewModel.audioPlayerStatus.value == AudioPlayerEnum.PLAYER_STOP)
                startPlayer()
            else if (viewModel.audioPlayerStatus.value == AudioPlayerEnum.PLAYER_START)
                stopPayer()
        }

        listenControllerObserver()
        return binding.root
    }

    private fun listenControllerObserver() {
        viewModel.audioPlayerStatus.observe(this) { playerStatus ->
            when (playerStatus) {
                AudioPlayerEnum.PLAYER_START -> {
                    binding.clProgress.visibility = View.VISIBLE
                    binding.tvTimer.visibility = View.VISIBLE
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_pause_anim.json", LottieDrawable.INFINITE)
                }

                AudioPlayerEnum.PLAYER_STOP -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    binding.clProgress.visibility = View.GONE
                    binding.tvTimer.visibility = View.GONE
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_play_anim.json", 0)
                }

                else -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    binding.clProgress.visibility = View.GONE
                    binding.tvTimer.visibility = View.GONE
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_play_anim.json", 0)
                }
            }
        }
    }

    private fun setImage(file: String) {
        val imgUri = Uri.parse(file)
        Glide.with(this)
            .load(imgUri)
            .placeholder(R.drawable.ic_image)
            .transform(RoundedCorners(20)) // 20 is the corner radius in pixels
            .into(binding.ivReminderImageView)
    }

    //Player Code start
    private fun startPlayer() {
        val myFile = File(audioFilePath)
        if (myFile.exists()) {
            player.startPlaying(audioFilePath)
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
