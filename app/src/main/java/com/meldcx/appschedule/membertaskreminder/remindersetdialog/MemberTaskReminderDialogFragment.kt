package com.meldcx.appschedule.membertaskreminder.remindersetdialog

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.meldcx.appschedule.membertaskreminder.MemberTaskReminderRepository
import com.meldcx.appschedule.membertaskreminder.helper.AudioRecorderEnum
import com.meldcx.appschedule.membertaskreminder.helper.NotificationHelper
import com.meldcx.appschedule.membertaskreminder.worker.MemberTaskReminderWorker
import com.meldcx.appschedule.databinding.MemberTaskReminderDialogFragmentBinding
import com.meldcx.appschedule.membertaskreminder.constant.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MemberTaskReminderDialogFragment : DialogFragment() {
    private var _binding: MemberTaskReminderDialogFragmentBinding? = null
    private val binding get() = _binding!!

    //Fragment initial data
    var memberId: Long = 0
    var poId: Long? = null
    var memberName: String? = null
    var voId: Long? = null
    var mDate: Date? = null

    private val mSimpleDateFormat = SimpleDateFormat(Constant.DATE_FORMAT_REMINDER, Locale.ENGLISH)
    var date: Calendar = Calendar.getInstance()

    @JvmField
    @Inject
    var memberTaskReminderRepository: MemberTaskReminderRepository? = null

    private lateinit var viewModel: MemberTaskReminderDialogFragmentViewModel

    //Audio Recorder and player
    private val recorder by lazy {
        AndroidAudioRecorder(requireContext())
    }

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    private var recordingAmpTimer = Timer()
    private val AMPLITUDE_UPDATE_MS = 75L
    private val MAX_RECORDING_TIME = 31 * 1000
    private val FIVE_MINUTE = 5*60*1000
    private var newRecordFilePath = ""
    private var playerProgressTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberId = requireArguments().getLong(Constant.KEY_MEMBER_ID)
        poId = requireArguments().getLong(Constant.KEY_PO_ID)
        memberName = requireArguments().getString(Constant.KEY_MEMBER_NAME)
        voId = requireArguments().getLong(Constant.KEY_VO_ID)

        //Notification Channel
        NotificationHelper.createNotificationChannel(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showAudioRecorderUi()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)

    }

    override fun fragmentDialogView(
        inflater: LayoutInflater?, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MemberTaskReminderDialogFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MemberTaskReminderDialogFragmentViewModel::class.java]
        binding.textInputEditTextDate.setOnClickListener { showDateTimePicker() }

        binding.textInputDate.setOnClickListener { showDateTimePicker() }
        binding.imgCalender.setOnClickListener { showDateTimePicker() }

        if (!memberName.isNullOrEmpty())
            binding.tvMember.text = String.format("%s %s", "for", memberName)

        val currentTime = TrueTime.now().time + FIVE_MINUTE
        date.time.time = currentTime

        binding.textInputDate.editText!!.setText(
            mSimpleDateFormat.format(currentTime)
        )
        //Audio Recorder View make
        binding.btnDiscard.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {

            val userData = AppHelper.getUserData(requireContext())
            val branchId = userData.branchId ?: 0L


            //Input validation Check Reminder Reason Required
            if (binding.edtComment.text.toString().isNotEmpty()) { //|| newRecordFilePath.isNotEmpty() || viewModel.imagePath.value.toString().isNotEmpty()
                val projectId = SharedPreferenceConfiguration.getInstance(requireActivity())
                    .getLong(Constant.KEY_PROJECT_ID, ProjectEnum.DABI.projectId)
                val reason = binding.edtComment.text.toString()

                /** Database Entry Start */
                setReminderInDatabase(
                    memberId,
                    branchId,
                    poId,
                    projectId,
                    memberName ?: "",
                    voId ?: -1,
                    reason,
                    viewModel.imagePath.value.toString(),
                    newRecordFilePath,
                    date.time
                )

                /** Database Entry End **/
                dismiss()

            } else { //Validation before submit //Validation field
                if (binding.edtComment.text?.isEmpty() == true) {
                    binding.remarksErrorMsg.visibility = View.VISIBLE
                }
            }
        }

        binding.edtComment.addTextChangedListener {
            if (it.toString().isEmpty()) {
                binding.remarksErrorMsg.visibility = View.VISIBLE
            } else {
                binding.remarksErrorMsg.visibility = View.GONE
            }
        }

        binding.toggleRecordingAnimButton.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                audioRecordController()
            }
        }

        binding.btnRecordDelete.setOnClickListener {
            newRecordFilePath = ""
            binding.recorderVisualizer.recreate()
            viewModel.updateAudioRecorderStatus(AudioRecorderEnum.RECORD_STOP)
            showAudioRecorderUi()
        }

        //Image Taking
        binding.ivTakeImage.setOnClickListener {
            showImagePicker()
        }

        binding.ivDeleteImg.setOnClickListener {
            deleteImg()
        }

        listenControllerObserver()

        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun listenControllerObserver() {
        viewModel.audioRecorderStatus.observe(this) { recorderStatus ->
            when (recorderStatus) {
                AudioRecorderEnum.RECORD_STOP -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_mic_anim.json", 0)

                    binding.playerProgressBar.visibility = View.GONE
                    binding.recorderVisualizer.visibility = View.VISIBLE

                    binding.btnRecordDelete.visibility = View.INVISIBLE
                    binding.tvTimer.visibility = View.GONE

                    binding.clProgress.visibility = View.GONE
                }

                AudioRecorderEnum.RECORD_START -> {
                    binding.recorderVisualizer.recreate()
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(
                        binding.toggleRecordingAnimButton,
                        "record_stop_anim.json",
                        LottieDrawable.INFINITE
                    )

                    binding.playerProgressBar.visibility = View.GONE
                    binding.recorderVisualizer.visibility = View.VISIBLE
                    binding.clProgress.visibility = View.VISIBLE

                    binding.btnRecordDelete.visibility = View.INVISIBLE
                    binding.tvTimer.visibility = View.VISIBLE
                }

                AudioRecorderEnum.RECORD_COMPLETED -> {

                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_play_anim.json", 0)

                    binding.playerProgressBar.visibility = View.GONE
                    binding.recorderVisualizer.visibility = View.VISIBLE
                    binding.clProgress.visibility = View.VISIBLE

                    binding.btnRecordDelete.visibility = View.VISIBLE
                    binding.tvTimer.visibility = View.VISIBLE
                }

                AudioRecorderEnum.PLAY_START -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(
                        binding.toggleRecordingAnimButton,
                        "record_stop_anim.json",
                        LottieDrawable.INFINITE
                    )

                    binding.playerProgressBar.visibility = View.VISIBLE
                    binding.recorderVisualizer.visibility = View.GONE
                    binding.clProgress.visibility = View.VISIBLE

                    binding.tvTimer.visibility = View.VISIBLE
                    binding.btnRecordDelete.visibility = View.GONE
                }

                AudioRecorderEnum.PLAY_STOP -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_play_anim.json", 0)

                    binding.playerProgressBar.visibility = View.VISIBLE
                    binding.recorderVisualizer.visibility = View.GONE
                    binding.clProgress.visibility = View.VISIBLE

                    binding.tvTimer.visibility = View.VISIBLE
                    binding.btnRecordDelete.visibility = View.VISIBLE
                }

                else -> {
                    binding.toggleRecordingAnimButton.setVisibility(View.VISIBLE)
                    showLottiAnim(binding.toggleRecordingAnimButton, "record_mic_anim.json", 0)

                    binding.playerProgressBar.visibility = View.GONE
                    binding.recorderVisualizer.visibility = View.VISIBLE
                    binding.clProgress.visibility = View.VISIBLE

                    binding.btnRecordDelete.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun audioRecordController() {
        if (viewModel.audioRecorderStatus.value == AudioRecorderEnum.RECORD_STOP)
            startRecording()
        else if (viewModel.audioRecorderStatus.value == AudioRecorderEnum.RECORD_START)
            stopRecording()
        else if (viewModel.audioRecorderStatus.value == AudioRecorderEnum.RECORD_COMPLETED)
            startPlayer()
        else if (viewModel.audioRecorderStatus.value == AudioRecorderEnum.PLAY_START)
            stopPayer()
    }

    private fun setReminderInDatabase(
        memberId: Long,
        branchId: Long,
        poId: Long?,
        projectId: Long,
        memberName: String,
        voId: Long,
        reason: String,
        imageFilePath: String,
        audioFilePath: String,
        reminderTriggerTime: Date
    ) {
        Log.d(TAG, "memberId: $memberId, branchId: $branchId, poId: $poId" +
                    ", projectId: $projectId, memberName: $memberName, voId: $voId" +
                    "  audio filePath: $audioFilePath reminderTriggerDate: ${reminderTriggerTime.time}")

        val reminderItem =
            MemberTaskReminderEntity(
                UUID.randomUUID(), memberId,
                branchId, poId ?: 0, projectId, memberName, voId,
                reason, imageFilePath, audioFilePath, "", false,
                reminderTriggerTime, UserTrueTime.getCurrentSystemDate()
            )
        reminderItem.syncStatus = false
        try {
            memberTaskReminderRepository?.addNewReminder(reminderItem)

            Toast.makeText(
                requireContext(),
                getString(R.string.member_task_reminder_added_successfully),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.something_went_wrong) + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }

        //Update Reminder
        scheduleReminderAt(requireContext(), reminderItem.id, date.time.time)
    }

    private fun scheduleReminderAt(context: Context, id: UUID, targetTimeInMillis: Long) {
        val currentTimeInMillis = System.currentTimeMillis()
        val delay = targetTimeInMillis - currentTimeInMillis

        if (delay > 0) {
            val inputData = workDataOf(
                Constant.KEY_REMINDER_ID to id.toString(),
                Constant.KEY_REMINDER_TRIGGER_TIME to targetTimeInMillis // Pass trigger time as a long timestamp
            )

            val workRequest = OneTimeWorkRequestBuilder<MemberTaskReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()
            //Schedule Msg in Work manager
            WorkManager.getInstance(context).enqueue(workRequest)

            memberTaskReminderRepository?.updateSystemReminderId(id, workRequest.id)
        } else {
            Log.d(TAG, "Target time is in the past.")
        }
    }

    // Recording Code start
    private fun startRecording() {
        val path =
            requireContext().cacheDir.absolutePath + "/" + memberId + "_" + TrueTime.now().time + "_audio_record.3gp"
        recorder.setRecordDuration(MAX_RECORDING_TIME)
        recorder.startRecording(path)
        Log.d(TAG, "File path of recorded file: $path")
        newRecordFilePath = path
        recordingAmpTimer = Timer()
        recordingAmpTimer.schedule(getRecorderAmplitudeUpdateTask(), 0, AMPLITUDE_UPDATE_MS)
        viewModel.updateAudioRecorderStatus(AudioRecorderEnum.RECORD_START)
    }

    private fun getRecorderAmplitudeUpdateTask() = object : TimerTask() {

        val startTime = System.currentTimeMillis()

        override fun run() {
            try {
                val elapsedTime = System.currentTimeMillis() - startTime

                if (elapsedTime <= MAX_RECORDING_TIME) {
                    try {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.recorderVisualizer.update(recorder.getMaxAmplitude() ?: 0)
                            val seconds = elapsedTime / 1000


                            binding.tvTimer.text =
                                String.format(Locale.getDefault(), "%d:%02d", 0, seconds)
                        }
                    } catch (ignored: Exception) {
                        Log.d(TAG, "Calculation error $ignored")
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        stopRecording()
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception caught: $e")
            }
        }
    }

    private fun stopRecording() {
        try {
            recorder.stop()
        } catch (ignored: Exception) {
            Log.d(TAG, "recorder stop exception: $ignored")

        }
        recordingAmpTimer.cancel()
        viewModel.updateAudioRecorderStatus(AudioRecorderEnum.RECORD_COMPLETED)
    }
    //Recording Code end

    //Player Code start
    private fun startPlayer() {
        val myFile = File(newRecordFilePath)
        if (myFile.exists()) {
            showAudioPlayerUi()
            player.startPlaying(newRecordFilePath)
            viewModel.updateAudioRecorderStatus(AudioRecorderEnum.PLAY_START)
            //Hide Visualizer show Audio Player
            startPlayerProgressTimer()
            showAudioPlayerUi()
        } else {
            Toast.makeText(requireContext(), getString(R.string.not_found), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopPayer() {
        player.stop()
        stopPlayerProgressTimer()
        viewModel.updateAudioRecorderStatus(AudioRecorderEnum.RECORD_COMPLETED)
    }

    private fun startPlayerProgressTimer() {
        playerProgressTimer.cancel()
        playerProgressTimer = Timer()
        binding.playerProgressBar.progress = 0
        binding.playerProgressBar.max = player.getAudioDuration() / 1000
        binding.tvTimer.text = String.format(Locale.getDefault(), "%d:%02d", 0, 0)
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

                Log.d(TAG, "Recorded Audio Current pos: $recAudioCurrentPos recorded duration: $recAudioDuration")
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
                Log.e(TAG, "Exception caught: $e")
            }
        }
    }
    //Player Code End

    private fun showAudioPlayerUi() {
        binding.recorderVisualizer.visibility = View.GONE
        binding.playerProgressBar.visibility = View.VISIBLE
    }

    private fun showAudioRecorderUi() {
        binding.recorderVisualizer.visibility = View.VISIBLE
        binding.playerProgressBar.visibility = View.GONE
    }

    private fun showLottiAnim(view: LottieAnimationView, fileName: String, repeatCount: Int) {
        view.cancelAnimation()
        view.setAnimation(fileName);
        view.repeatCount = repeatCount
        view.playAnimation()
    }

    private fun setImage(file: String) {
        val imgUri = Uri.parse(file)
        binding.takenImg.visibility = View.VISIBLE
        binding.takenImg.setImageURI(imgUri)
        viewModel.updateImagePath(file)
        binding.tvNoImageSelected.visibility = View.GONE
        binding.ivDeleteImg.visibility = View.VISIBLE
        binding.ivTakeImage.visibility = View.GONE
    }

    private fun deleteImg() {
        viewModel.updateImagePath("")
        binding.takenImg.visibility = View.GONE
        binding.tvNoImageSelected.visibility = View.VISIBLE
        binding.ivDeleteImg.visibility = View.GONE
        binding.ivTakeImage.visibility = View.VISIBLE
    }

    private fun showDateTimePicker() {
        val currentDate = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.datepicker,
            { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                date.set(year, monthOfYear, dayOfMonth)
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    R.style.datepicker,
                    { view1: TimePicker?, hourOfDay: Int, minute: Int ->
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        date.set(Calendar.MINUTE, minute)
                        Log.d(TAG, "The chosen one " + date.time)
                        mDate = date.time
                        binding.textInputDate.editText!!.setText(
                            mSimpleDateFormat.format(date.time)
                        )
                        binding.dateTimeErrorMsg.visibility = View.GONE
                    }, 15,
                    0,
                    false
                )
                timePickerDialog.show()
            }, currentDate[Calendar.YEAR], currentDate[Calendar.MONTH],
            currentDate[Calendar.DATE]
        )
        datePickerDialog.datePicker.minDate = currentDate.timeInMillis
        datePickerDialog.show()
    }

    fun showImagePicker() {
        val cameraIntent = Intent(requireContext(), CameraXActivity::class.java).apply {
            putExtra(INTENT_CAMERA_ACTION_TYPE, CameraEnum.IMAGE_FLOW)
        }
        cameraLauncher.launch(cameraIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
        player.stop()
    }

    companion object {
        private const val TAG = "MemberTaskReminderDialogFragment"
    }

    //Callbacks
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result here
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra(CAMERA_IMAGE)?.let { imagePath ->
                setImage(imagePath)
                Log.d(TAG, "Image Picker Task path: $imagePath")
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                audioRecordController()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permissions_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

}
