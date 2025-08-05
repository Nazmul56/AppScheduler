package com.meldcx.appschedule

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScheduleListFragment : Fragment() {

    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.scheduleRecyclerView)
        val fabAddSchedule = view.findViewById<Button>(R.id.addScheduleButton)

        scheduleAdapter = ScheduleAdapter(
            schedules = emptyList(),
            onItemClick = { schedule ->
                // Open edit dialog for the selected schedule
                showEditDialog(schedule)
            },
            onDeleteClick = { schedule ->
                viewModel.delete(schedule)
            },

        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
        }

        fabAddSchedule.setOnClickListener {
            showAddDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allSchedules.observe(viewLifecycleOwner, Observer { schedules ->
            scheduleAdapter.updateList(schedules)
        })
    }

    private fun showAddDialog() {
        // You need to provide the list of installed app names here
        val installedApps = getInstalledApps(requireContext())

       // val appList = listOf("App 1", "App 2", "App 3") // Replace with actual app list
        val dialog = ScheduleDialog(appList = installedApps, onSave = { schedule ->
            viewModel.insert(schedule)
        })
        dialog.show(parentFragmentManager, "ScheduleDialog")
    }

    private fun showEditDialog(schedule: AppSchedule) {
        val installedApps = getInstalledApps(requireContext())

        val dialog = ScheduleDialog(appList = installedApps, initialSchedule = schedule, onSave = { updatedSchedule ->
            viewModel.update(updatedSchedule)
        })
        dialog.show(parentFragmentManager, "ScheduleDialog")
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApps(context: Context): List<String> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appNames = mutableListOf<String>()

        for (appInfo in packages) {
            // Filter out system apps if you want only user apps
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                appNames.add(appName)
            }
        }

        return appNames.sorted()
    }



}
