package com.example.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BannerPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var fragmentList = listOf<Fragment>()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun submitList(list: List<Fragment>) {
        fragmentList = list
        notifyDataSetChanged()
    }
}
