package com.meldcx.appschedule.membertaskreminder.reminderimageviewer

import android.R
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.meldcx.appschedule.databinding.MemberTaskImageViewerDialogFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemberTaskImageViewerDialogFragment : BaseDialogFragment() {
    private var _binding: MemberTaskImageViewerDialogFragmentBinding? = null
    private val binding get() = _binding!!

    var memberId: Long = 0
    private var imageFilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberId = requireArguments().getLong(Constant.KEY_MEMBER_ID/*"memberId"*/)
        imageFilePath = requireArguments().getString(Constant.KEY_REMINDER_IMAGE_PATH/*"imageFilePath"*/).toString()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       setImage(imageFilePath)
    }

    private fun setImage(file: String){
        val imgUri = Uri.parse(file)
        Glide.with(this)
            .load(imgUri)
            .transform(RoundedCorners(20)) // 20 is the corner radius in pixels
            .placeholder(R.drawable.ic_image)
            .into(binding.ivReminderImage)
    }

    override fun fragmentDialogView(
        inflater: LayoutInflater?, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MemberTaskImageViewerDialogFragmentBinding.inflate(layoutInflater)

        binding.btnDiscard.setOnClickListener { dismiss() }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        // Function to trigger when the fragment is destroyed
    }

    companion object {
        private const val TAG = "MemberTaskImageViewerDialogFragment"
    }
}
