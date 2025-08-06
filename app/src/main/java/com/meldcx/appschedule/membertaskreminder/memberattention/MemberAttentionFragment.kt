package com.meldcx.appschedule.membertaskreminder.memberattention

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.meldcx.appschedule.membertaskreminder.MemberTaskReminderFragment
import dagger.hilt.android.AndroidEntryPoint

import java.lang.reflect.Field

@AndroidEntryPoint
class MemberAttentionFragment : Fragment() {

    private var binding: FragmentMemberAttentionBinding by viewLifecycleNullable()

    override fun fragmentView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemberAttentionBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun viewRelatedTask() {
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val fragments = mutableListOf<Fragment>()
        val tabTitle = mutableListOf<String>()

        // Set the second tab
        val reminderId = MemberAttentionFragmentArgs.fromBundle(requireArguments()).reminderId

        Log.d(TAG, "Reminder Id: $reminderId ")

        val favouriteMemberFragment = FavouriteMemberFragment.newInstance(
            getString(R.string.favourite_member),
            emptyList()
        )
        fragments.add(favouriteMemberFragment)
        tabTitle.add(getString(R.string.favourite_member))

        val memberTaskReminderFragment = MemberTaskReminderFragment.Companion.newInstance(
            getString(R.string.member_task_reminder_title_txt),
            mutableListOf()
        )
        memberTaskReminderFragment.arguments = requireArguments()
        fragments.add(memberTaskReminderFragment)
        tabTitle.add(getString(R.string.member_task_reminder_txt))

        val viewPager2Adapter = ViewPager2Adapter(
            getChildFragmentManager(),
            getViewLifecycleOwner().lifecycle, fragments
        )

        binding.viewPager.setAdapter(viewPager2Adapter)

        binding.viewPager.isSaveEnabled = true

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.setText(
                tabTitle[position]
            )
        }.attach()


        if (!reminderId.isNullOrEmpty()) {
            binding.viewPager.post {
                binding.viewPager.setCurrentItem(
                    1,
                    false
                ) // `false` disables the smooth scroll animation
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "Current position: $position")
            }
        })

        try {
            val field: Field = ViewPager::class.java.getDeclaredField("mScroller")
            field.isAccessible = true
            val scroller = FixedSpeedScroller(
                requireContext(),
                AccelerateDecelerateInterpolator()
            )
            field[binding.viewPager] = scroller
        } catch (ignored: Exception) {
        }
    }

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    companion object {
        private const val TAG = "MemberAttentionFragment"
    }
}