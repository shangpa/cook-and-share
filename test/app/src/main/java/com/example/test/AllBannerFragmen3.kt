package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.FragmentBannerAll3Binding

//FragmentBannerAll3Binding.kt
class AllBannerFragment3 : Fragment() {
    lateinit var binding: FragmentBannerAll3Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBannerAll3Binding.inflate(inflater,container,false)

        return binding.root
    }
}