package com.meldcx.appschedule.membertaskreminder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bracits.mf.common.utils.Constant
import com.bracits.smartpo.R
import com.bracits.smartpo.base.ParentFragment
import com.bracits.smartpo.constant.IntentConstants
import com.bracits.smartpo.databinding.FragmentMemberTaskReminderBinding
import com.bracits.smartpo.enums.ProjectEnum
import com.bracits.smartpo.model.CollectionModel
import com.bracits.smartpo.po.ui.membertaskreminder.memberattention.MemberAttentionFragmentArgs
import com.meldcx.appschedule.membertaskreminder.rememberdetails.MemberTaskReminderDetailsFragment
import com.meldcx.appschedule.membertaskreminder.reminderaudioplayer.MemberTaskVoiceNotePlayerDialogFragment
import com.meldcx.appschedule.membertaskreminder.reminderimageviewer.MemberTaskImageViewerDialogFragment
import com.meldcx.appschedule.membertaskreminder.remindersetdialog.MemberTaskReminderDialogFragment
import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderEntity
import com.meldcx.appschedule.membertaskreminder.data.MemberTaskReminderEnum
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class MemberTaskReminderFragment : ParentFragment() {

    private var TAG = "MemberTaskReminderFragment"

    private var mBinding: FragmentMemberTaskReminderBinding? = null
    private lateinit var reminderAdapter: MemberTaskReminderListAdapter
    private lateinit var memberTaskReminderViewModel: MemberTaskReminderFragmentViewModel
    override fun viewRelatedTask() {
        onLoading(true)
        initAdapter()
        initRecyclerView()
        loadData()
        onLoading(false)
    }

    override fun fragmentView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (mBinding == null) {
            mBinding = FragmentMemberTaskReminderBinding.inflate(inflater, parent, false)
            memberTaskReminderViewModel = ViewModelProvider(this)[MemberTaskReminderFragmentViewModel::class.java]

            //(requireActivity() as SmartMfMainActivity).title = getString(R.string.member_task_reminder_txt)
            val reminderId = MemberAttentionFragmentArgs.fromBundle(requireArguments()).reminderId
            Log.d(TAG, "RememberId: $reminderId")

            if(!reminderId.isNullOrEmpty()){
                val reminderUUID: UUID = UUID.fromString(reminderId)
                Log.d(TAG, "Reminder ID: $reminderId")

               val reminder =  memberTaskReminderViewModel.getMemberTaskReminder(reminderUUID)
                if(reminder != null) {
                    openReminderDetails(reminder)
                }
            }

            mBinding?.btnReminderAdd?.setOnClickListener {
                val memberTaskReminderInputDialogFragment = MemberTaskReminderDialogFragment()
                val bundle1 = Bundle().apply {
                    putLong(Constant.KEY_MEMBER_ID, 1)
                    putLong(Constant.KEY_PO_ID, -1)
                    putString(Constant.KEY_MEMBER_NAME, "")
                    putLong(Constant.KEY_VO_ID, -1)
                }
                memberTaskReminderInputDialogFragment.arguments = bundle1
                memberTaskReminderInputDialogFragment.show(requireActivity().supportFragmentManager, "ReminderPop")
            }
        }
        return mBinding!!.getRoot()
    }

    private fun loadData() {
        mBinding!!.progressBar.visibility = View.VISIBLE
        memberTaskReminderViewModel.getAllReminders(ProjectEnum.DABI.projectId).observe(this) { list ->
            if(list.isNotEmpty()){
                mBinding?.rvMembers?.visibility = View.VISIBLE
                mBinding?.noDatFound?.root?.visibility = View.GONE
                reminderAdapter.setValue(list)
            } else {
                mBinding?.rvMembers?.visibility = View.GONE
                mBinding?.noDatFound?.tvMessage?.text = resources.getString(R.string.member_task_reminder_not_found_txt)
                mBinding?.noDatFound?.root?.visibility = View.VISIBLE
            }
            mBinding?.rvMembers?.visibility = View.VISIBLE
        }
    }

    override fun onLoading(isLoader: Boolean) {
        if (!isLoader) {
            mBinding!!.progressBar.visibility = View.GONE
        } else {
            mBinding!!.progressBar.visibility = View.VISIBLE
        }
    }

    private fun initAdapter(){
        reminderAdapter =
            MemberTaskReminderListAdapter(requireContext(), onItemClicked = this::onItemClicked)
    }

    private fun onItemClicked(position: Int, model : MemberTaskReminderEntity, memberTaskReminderEnum: MemberTaskReminderEnum){
        if (memberTaskReminderEnum == MemberTaskReminderEnum.AUDIO_FILE) {
            val audioFilePath = model.reminderAudioFileUri
            if(audioFilePath.isNotEmpty()){
                val memberTaskViceNoneDialogFragment = MemberTaskVoiceNotePlayerDialogFragment()
                val bundle = Bundle().apply { putString(Constant.KEY_REMINDER_AUDIO_PATH, audioFilePath) }
                memberTaskViceNoneDialogFragment.arguments = bundle
                memberTaskViceNoneDialogFragment.show(requireActivity().supportFragmentManager, Constant.VOICE_PLAYER_FRAGMENT)
            } else {
                showToast(requireContext(), getString(R.string.member_task_reminder_cannot_play_recording_txt))
            }
        } else if (memberTaskReminderEnum == MemberTaskReminderEnum.IMAGE_FILE) {
            val imageFilePath = model.reminderImageFilePath
            if(imageFilePath.isNotEmpty()){
                val memberImageDialogFragment = MemberTaskImageViewerDialogFragment()
                val bundle = Bundle().apply { putString(Constant.KEY_REMINDER_IMAGE_PATH, imageFilePath) }
                memberImageDialogFragment.arguments = bundle
                memberImageDialogFragment.show(requireActivity().supportFragmentManager, Constant.IMAGE_VIEWER_RAGMENT)
            } else {
                showToast(requireContext(), getString(R.string.member_task_reminder_cannot_show_img_txt))
            }
        } else if (memberTaskReminderEnum == MemberTaskReminderEnum.REMINDER_DETAILS) {
            openReminderDetails(model)
        } else if (memberTaskReminderEnum == MemberTaskReminderEnum.DELETE_REMINDER) {
            if(position < reminderAdapter.getSize()) {
                reminderAdapter[position].let {

                    //Delete Audio File
                    val audioPath = it.reminderAudioFileUri
                    deleteFile(audioPath)

                    //Delete Image File
                    val imagePath = it.reminderImageFilePath
                    deleteFile(imagePath)

                    //Image From View Model
                    memberTaskReminderViewModel.deleteReminder(it.id)
                }
                if(position == 0 && reminderAdapter.getSize() == 1)
                    reminderAdapter.remove(position, true)

            } else {
                showToast(requireContext(), getString(R.string.member_task_reminder_not_found_txt))
            }
        }
    }

    private fun initRecyclerView(){
        mBinding?.rvMembers?.overScrollMode = View.OVER_SCROLL_NEVER
        mBinding?.rvMembers?.setHasFixedSize(true)
        mBinding?.rvMembers?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding?.rvMembers?.adapter = reminderAdapter

        //Last Item Visible Listener
        mBinding?.rvMembers?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                Log.d(TAG, "Last item: $lastVisibleItemPosition total item: $totalItemCount")
                // Hide the button when the first visible item is not the first page (position 0)
                if (firstVisibleItemPosition > 0) {
                    mBinding?.btnReminderAdd?.hide()
                } else {
                    mBinding?.btnReminderAdd?.show()
                }
            }
        })
    }

    private fun deleteFile(path: String){
        val file = File(path)
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "File deleted successfully: $path")
            } else {
                Log.e(TAG, "Failed to delete file: $path")
            }
        } else {
            Log.e(TAG, "File not found: $path")
        }
    }

    private fun openReminderDetails(model: MemberTaskReminderEntity){
        val memberTaskReminderDetailsFragment = MemberTaskReminderDetailsFragment()
        val bundle = Bundle().apply {
            putLong(Constant.KEY_MEMBER_ID, model.memberId)
            putString(Constant.KEY_MEMBER_NAME, model.memberName)
            putString(Constant.KEY_REMINDER_REASON, model.reminderReason)
            putString(Constant.KEY_REMINDER_AUDIO_PATH,  model.reminderAudioFileUri)
            putString(Constant.KEY_REMINDER_IMAGE_PATH, model.reminderImageFilePath)
            if (model.reminderTriggerAt != null) {
                putString(Constant.KEY_REMINDER_SET_TIME, SimpleDateFormat(Constant.DATE_FORMAT_REMINDER, Locale.getDefault()).format(model.reminderTriggerAt))
            }
        }
        memberTaskReminderDetailsFragment.arguments = bundle
        memberTaskReminderDetailsFragment.show(requireActivity().supportFragmentManager, Constant.REMINDER_DETAILS_FRAGMENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    companion object {
        fun newInstance(
            title: String?,
            dataList: MutableList<CollectionModel>
        ): MemberTaskReminderFragment {
            val collectionCurrentFragment = MemberTaskReminderFragment()
            val bundle = Bundle()
            bundle.putSerializable(
                IntentConstants.COLLECTION_TRANSACTION_DATA,
                dataList as Serializable?
            )
            collectionCurrentFragment.setArguments(bundle)
            return collectionCurrentFragment
        }
    }
}
