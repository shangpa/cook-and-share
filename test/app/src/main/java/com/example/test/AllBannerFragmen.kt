package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.databinding.FragmentBannerAllBinding

//AllBannerFragment.kt
class AllBannerFragment : Fragment() {
    lateinit var binding: FragmentBannerAllBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBannerAllBinding.inflate(inflater,container,false)

        return binding.root
    }
}