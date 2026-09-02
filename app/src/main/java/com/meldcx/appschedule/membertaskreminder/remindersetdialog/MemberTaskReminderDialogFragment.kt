package com.meldcx.appschedule.membertaskreminder.remindersetdialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.meldcx.appschedule.R
import com.meldcx.appschedule.membertaskreminder.helper.NotificationHelper
import com.meldcx.appschedule.membertaskreminder.worker.ReminderWorker
import com.meldcx.appschedule.databinding.MemberTaskReminderDialogFragmentBinding
import com.meldcx.appschedule.membertaskreminder.constant.Constant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class MemberTaskReminderDialogFragment : DialogFragment() {
    private var _binding: MemberTaskReminderDialogFragmentBinding? = null
    private val binding get() = _binding!!

    var memberId: Long = 0
    var poId: Long? = null
    var memberName: String? = null
    var voId: Long? = null
    var mDate: Date? = null

    private val mSimpleDateFormat = SimpleDateFormat(Constant.DATE_FORMAT_REMINDER, Locale.ENGLISH)
    var date: Calendar = Calendar.getInstance()
    private lateinit var viewModel: MemberTaskReminderDialogFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberId = requireArguments().getLong(Constant.KEY_MEMBER_ID)
        poId = requireArguments().getLong(Constant.KEY_PO_ID)
        memberName = requireArguments().getString(Constant.KEY_MEMBER_NAME)
        voId = requireArguments().getLong(Constant.KEY_VO_ID)

        NotificationHelper.createNotificationChannel(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MemberTaskReminderDialogFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MemberTaskReminderDialogFragmentViewModel::class.java]
        binding.textInputEditTextDate.setOnClickListener { showDateTimePicker() }

        binding.textInputDate.setOnClickListener { showDateTimePicker() }
        binding.imgCalender.setOnClickListener { showDateTimePicker() }

        if (!memberName.isNullOrEmpty())
            binding.tvMember.text = String.format("%s %s", "for", memberName)

        val FIVE_MINUTE = 5 * 60 * 1000L  // 5 minutes in milliseconds

        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + FIVE_MINUTE
        val futureDate = Date(futureTime)

        val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(futureDate)

        date.time.time = currentTime

        binding.textInputDate.editText!!.setText(
            mSimpleDateFormat.format(currentTime)
        )
        //Audio Recorder View make
        binding.btnDiscard.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {

            //Input validation Check Reminder Reason Required
            if (binding.edtComment.text.toString()
                    .isNotEmpty()
            ) { //|| newRecordFilePath.isNotEmpty() || viewModel.imagePath.value.toString().isNotEmpty()
                val reason = binding.edtComment.text.toString()
                scheduleReminderAt(requireContext(), UUID.randomUUID(), reason, date.time.time)
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
        return binding.root
    }

    private fun scheduleReminderAt(context: Context, id: UUID, text: String, targetTimeInMillis: Long) {
        val currentTimeInMillis = System.currentTimeMillis()
        val delay = targetTimeInMillis - currentTimeInMillis

        if (delay > 0) {
            val inputData = workDataOf(
                Constant.KEY_REMINDER_ID to id.toString(),
                Constant.KEY_REMINDER_TRIGGER_TIME to targetTimeInMillis // Pass trigger time as a long timestamp
            )

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } else {
            Log.d(TAG, "Target time is in the past.")
        }
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

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MemberTaskReminderDialogFragment"
    }
}
