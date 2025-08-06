package com.meldcx.appschedule.membertaskreminder

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bracits.mf.common.utils.Constant.DATE_FORMAT_REMINDER
import com.bracits.smartpo.R
import com.bracits.smartpo.base.BaseRecyclerAdapterWithViewBinding
import com.bracits.smartpo.databinding.ItemMemberTaskReminderBinding
import com.bumptech.glide.Glide
import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderEntity
import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderEnum
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.get

class MemberTaskReminderListAdapter(private val context: Context, private val onItemClicked : (Int, MemberTaskReminderEntity, MemberTaskReminderEnum) -> Unit) : BaseRecyclerAdapterWithViewBinding<ItemMemberTaskReminderBinding, MemberTaskReminderEntity>() {

    companion object{
        const val TAG = "MemberNoteListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(ItemMemberTaskReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data[position].apply {
            if(reminderReason.isNotEmpty())
                holder.binding.tvReminder.text = reminderReason
            else
                holder.binding.tvReminder.text = ""
            if(reminderTriggerAt!= null)
                holder.binding.reminderSetTime.text = SimpleDateFormat(DATE_FORMAT_REMINDER, Locale.getDefault()).format(reminderTriggerAt)

            if(isSystemReminderTriggered){
                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)) // Create a ColorStateList
                ImageViewCompat.setImageTintList(holder.binding.reminderTimeIcon, colorStateList)
            } else {
                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_primary_text)) // Create a ColorStateList
                ImageViewCompat.setImageTintList(holder.binding.reminderTimeIcon, colorStateList)
            }

            val menuRemove = PopupMenu(context, holder.binding.ivOptionRemove)
            menuRemove.menu.add(0, 1, 0, context.resources.getString(R.string.delete))
            holder.binding.ivOptionRemove.setOnClickListener{menuRemove.show()}
            //registering popup with OnMenuItemClickListener
            menuRemove.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        // Handle delete action
                        onItemClicked(position, this, MemberTaskReminderEnum.DELETE_REMINDER)
                        true
                    }
                    else -> false
                }
            }

            if(reminderAudioFileUri.isEmpty()){
                holder.binding.togglePlayButton.setVisibility(View.GONE)
            } else {
                holder.binding.togglePlayButton.setVisibility(View.VISIBLE)
            }

            if(memberId == 1L){
                holder.binding.clTaggedMember.visibility = View.GONE
            } else {
                holder.binding.clTaggedMember.visibility = View.VISIBLE
                holder.binding.tvTaggedMemberName.text = memberName
            }

            holder.binding.togglePlayButton.setOnClickListener{
                onItemClicked(position, this, MemberTaskReminderEnum.AUDIO_FILE)
            }

            holder.binding.clReminderNote.setOnClickListener{
                onItemClicked(position, this, MemberTaskReminderEnum.REMINDER_DETAILS)
            }

            if(reminderImageFilePath.isEmpty()){
                holder.binding.cvImage.visibility = View.GONE
            } else {
                holder.binding.cvImage.visibility = View.VISIBLE

                val imgUri = Uri.parse(reminderImageFilePath)
                Glide.with(context)
                    .load(imgUri)
                    .into(holder.binding.ivImageButton)
            }

            holder.binding.cvImage.setOnClickListener{
                onItemClicked(position, this, MemberTaskReminderEnum.IMAGE_FILE)
                Log.d(TAG, "Click Send")
            }
        }
    }
}
