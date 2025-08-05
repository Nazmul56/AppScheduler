package com.meldcx.appschedule

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.widget.ArrayAdapter

class ScheduleDialog(
    private val appList: List<String>,
    private val initialSchedule: AppSchedule? = null,
    private val onSave: (AppSchedule) -> Unit
) : DialogFragment() {

    private lateinit var appSpinner: Spinner
    private lateinit var timeEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_schedule_app, null)

        appSpinner = view.findViewById(R.id.spinnerApp)
        timeEditText = view.findViewById(R.id.editTextTime)
        saveButton = view.findViewById(R.id.btnSave)
        cancelButton = view.findViewById(R.id.btnCancel)

        // Setup spinner adapter for app list
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, appList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        appSpinner.adapter = adapter

        // If editing existing schedule, populate fields
        initialSchedule?.let { schedule ->
            val position = appList.indexOf(schedule.appLabel)
            if (position >= 0) appSpinner.setSelection(position)
            timeEditText.setText(schedule.launchTime.toString())
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        saveButton.setOnClickListener {
            val selectedApp = appSpinner.selectedItem as String
            val time = timeEditText.text.toString().trim()

            if (time.isEmpty()) {
                timeEditText.error = "Please enter time"
                return@setOnClickListener
            }

            val schedule = initialSchedule?.copy(appName = selectedApp, launchTime = time)
                ?: AppSchedule(
                    id =0,
                    launchTime = time,
                    appName = selectedApp,
                    executed = false,
                    appLabel = selectedApp,
                    packageName = ""
                ) // id=0 for new schedule

            onSave(schedule)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
