package com.example.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BannerViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity){


    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllBannerFragment()
            1 -> AllBannerFragment2()
            2 -> AllBannerFragment3()
            3 -> AllBannerFragment4()
            4 -> AllBannerFragment5()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
