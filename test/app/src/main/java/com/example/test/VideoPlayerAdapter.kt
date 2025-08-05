@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VideoPlayerAdapter(
    fragmentActivity: FragmentActivity,
    private val videoList: List<ShortObject>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = videoList.size

    override fun createFragment(position: Int): Fragment {
        return VideoPlayerFragment.newInstance(videoList[position])
    }
}
