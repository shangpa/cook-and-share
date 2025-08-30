package com.example.test

import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.test.model.ShortObject

class VideoPlayerAdapter(
    activity: FragmentActivity,
    private val items: MutableList<ShortObject>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = items.size

    @OptIn(UnstableApi::class) override fun createFragment(position: Int): Fragment {
        return VideoPlayerFragment.newInstance(items[position])
    }

    // ViewPager2에서 tag를 "f$position"으로 쓰려면 안정 ID를 position 기반으로:
    override fun getItemId(position: Int): Long = position.toLong()
    override fun containsItem(itemId: Long): Boolean = itemId in 0 until items.size
}
