package com.meldcx.appschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(
    private var schedules: List<AppSchedule>,
    private val onItemClick: (AppSchedule) -> Unit,
    private val onDeleteClick: (AppSchedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameTextView: TextView = itemView.findViewById(R.id.tvAppName)
        val packageNameTextView: TextView = itemView.findViewById(R.id.tvPackageName)
        val launchTimeTextView: TextView = itemView.findViewById(R.id.tvScheduleTime)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        val editButton: Button = itemView.findViewById(R.id.btnEdit)

        fun bind(schedule: AppSchedule) {
            appNameTextView.text = schedule.appName
            packageNameTextView.text = schedule.packageName
            launchTimeTextView.text = schedule.launchTime.toString()

            itemView.setOnClickListener {
                onItemClick(schedule)
            }
            deleteButton.setOnClickListener {
                onDeleteClick(schedule)
            }
            editButton.setOnClickListener {
                onItemClick(schedule)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount(): Int = schedules.size

    fun updateList(newSchedules: List<AppSchedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }
}
