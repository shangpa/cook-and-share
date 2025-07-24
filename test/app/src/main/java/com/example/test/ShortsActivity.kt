@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.example.test.databinding.ActivityShortBinding

class ShortsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShortBinding
    private lateinit var videoAdapter: VideoPlayerAdapter
    private val mediaObjectList = ArrayList<ShortObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_short)

        // 샘플 데이터 - filename만 넣도록 변경
        mediaObjectList.add(
            ShortObject(
                id = 1,
                userName = "고양이 집사",
                contents = "냥냥펀치",
                filename = "1.mp4"
            )
        )
        mediaObjectList.add(
            ShortObject(
                id = 2,
                userName = "하잉하잉",
                contents = "고양이 영상입니다",
                filename = "2"
            )
        )
        mediaObjectList.add(
            ShortObject(
                id = 3,
                userName = "아기고양이",
                contents = "야옹",
                filename = "3"
            )
        )

        videoAdapter = VideoPlayerAdapter(this, mediaObjectList)
        binding.shortsVP.adapter = videoAdapter
        binding.shortsVP.orientation = ViewPager2.ORIENTATION_VERTICAL

        // ViewPager2 변경 감지 → 재생/정지 제어
        binding.shortsVP.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentFragment =
                    supportFragmentManager.findFragmentByTag("f$position") as? VideoPlayerFragment
                currentFragment?.playerControl(play = true, reset = false)

                for (i in 0 until videoAdapter.itemCount) {
                    if (i != position) {
                        val f =
                            supportFragmentManager.findFragmentByTag("f$i") as? VideoPlayerFragment
                        f?.playerControl(play = false, reset = true)
                    }
                }
            }
        })
    }
}
