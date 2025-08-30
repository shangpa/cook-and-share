@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.test

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.example.test.databinding.ActivityShortBinding
import com.example.test.model.ShortObject
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class ShortsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShortBinding
    private lateinit var adapter: VideoPlayerAdapter
    private val items = mutableListOf<ShortObject>()

    private var page = 0
    private var isLoading = false
    private val pageSize = 10
    // ✅ 안정 랜덤용 seed (앱 켜진 동안/액티비티 생명주기 동안 유지)
    private val seed: String by lazy { UUID.randomUUID().toString() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_short)

        // 전체화면 immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableImmersiveMode()

        adapter = VideoPlayerAdapter(this, items)
        binding.shortsVP.adapter = adapter
        binding.shortsVP.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.shortsVP.offscreenPageLimit = 2

        // 첫 페이지 로드
        loadMore()

        // 페이지 변경 시: 현재 페이지만 play
        binding.shortsVP.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                playOnly(position)

                // 바닥 근처에서 다음 페이지 로드
                if (!isLoading && position >= items.size - 3) {
                    loadMore()
                }
            }
        })
    }

    private fun playOnly(position: Int) {
        // 현재만 재생
        (supportFragmentManager.findFragmentByTag("f$position") as? VideoPlayerFragment)
            ?.playerControl(play = true, reset = false)

        // 나머지 정지
        for (i in 0 until adapter.itemCount) {
            if (i == position) continue
            (supportFragmentManager.findFragmentByTag("f$i") as? VideoPlayerFragment)
                ?.playerControl(play = false, reset = true)
        }
    }

    private fun loadMore() {
        isLoading = true

        val bearer = App.prefs.token?.let { "Bearer $it" } ?: ""

        RetrofitInstance.apiService.getShorts(
            seed = seed,
            page = page,
            size = pageSize,
            bearer = bearer
        ).enqueue(object : Callback<List<ShortObject>> {
            override fun onResponse(
                call: Call<List<ShortObject>>,
                response: Response<List<ShortObject>>
            ) {
                val data = response.body().orEmpty()

                if (data.isEmpty()) {
                    Toast.makeText(
                        this@ShortsActivity,
                        "랜덤 결과 0개 (필터/매핑 확인)",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val start = items.size
                    items.addAll(data)
                    adapter.notifyItemRangeInserted(start, data.size)
                    page += 1

                    if (start == 0) binding.shortsVP.post { playOnly(0) }
                }

                isLoading = false
            }
            override fun onFailure(call: Call<List<ShortObject>>, t: Throwable) {
                t.printStackTrace()
                isLoading = false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
        // 복귀 시 현재 페이지만 재생
        playOnly(binding.shortsVP.currentItem)
    }

    override fun onPause() {
        super.onPause()
        // 전체 일시정지
        for (i in 0 until adapter.itemCount) {
            (supportFragmentManager.findFragmentByTag("f$i") as? VideoPlayerFragment)
                ?.playerControl(play = false, reset = false)
        }
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(
                    android.view.WindowInsets.Type.statusBars() or
                            android.view.WindowInsets.Type.navigationBars()
                )
                it.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}
