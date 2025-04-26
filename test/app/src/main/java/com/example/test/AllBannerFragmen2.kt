package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.FragmentBannerAll2Binding

//FragmentBannerAll2Binding.kt
class AllBannerFragment2 : Fragment() {
    lateinit var binding: FragmentBannerAll2Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBannerAll2Binding.inflate(inflater,container,false)

        return binding.root
    }
}