package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.FragmentBannerAll5Binding

//FragmentBannerAll5Binding.kt
class AllBannerFragment5 : Fragment() {
    lateinit var binding: FragmentBannerAll5Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBannerAll5Binding.inflate(inflater,container,false)

        return binding.root
    }
}