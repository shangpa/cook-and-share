package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.FragmentBannerAll3Binding
import com.example.test.databinding.FragmentBannerAll4Binding

//FragmentBannerAll4Binding.kt
class AllBannerFragment4 : Fragment() {
    lateinit var binding: FragmentBannerAll4Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBannerAll4Binding.inflate(inflater,container,false)

        return binding.root
    }
}