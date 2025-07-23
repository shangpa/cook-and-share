// ShortsActivity.kt
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

        // 샘플 숏츠 데이터 추가
        mediaObjectList.add(
            ShortObject(
                id = 1,
                userName = "고양이 집사",
                contents = "냥냥펀치 입니다~~",
                url = "https://blog.kakaocdn.net/dna/cPjc1G/btslYxcRuz4/AAAAAAAAAAAAAAAAAAAAAOeZFNw15HM5ck-yjVGRw5Jd8jCwcIZ2bpx3PnUuua_U/KakaoTalk_Video_2023-06-30-17-46-03.mp4"
            )
        )
        mediaObjectList.add(
            ShortObject(
                id = 2,
                userName = "하잉하잉",
                contents = "고양이 영상입니다~ 집앞 고양이~",
                url = "https://blog.kakaocdn.net/dna/bcVeEV/btslZPqcb1L/AAAAAAAAAAAAAAAAAAAAAK83F9Oxwb9AIMQ0pIOzCptgb8PgTFpvD7lsE9T8hZ9i/KakaoTalk_Video_2023-06-30-17-47-22.mp4"
            )
        )
        mediaObjectList.add(
            ShortObject(
                id = 3,
                userName = "아기고양이",
                contents = "아기 고양이 입니다!! 야옹",
                url = "https://blog.kakaocdn.net/dna/riYGP/btslT0e0Zba/AAAAAAAAAAAAAAAAAAAAACoqAjlytXNPzQWFIBMTUCm8yPPUp4c0c1dfNn_ppPXE/KakaoTalk_Video_2023-06-30-14-57-27.mp4"
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
