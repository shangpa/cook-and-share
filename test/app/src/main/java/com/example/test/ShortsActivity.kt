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
import com.example.test.model.shorts.ShortVideoDto
import com.example.test.model.shorts.ShortVideoListResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class ShortsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShortBinding
    private lateinit var adapter: VideoPlayerAdapter
    private val items = mutableListOf<ShortObject>()


    private var mode: String? = null        // "random" | "user"
    private var userIdArg: Int = -1
    private var sortArg: String = "latest"
    private var startIndexArg: Int = 0

    private var page = 0
    private var isLoading = false
    private val pageSize = 10
    private val seed: String by lazy { UUID.randomUUID().toString() }

    private val focusId: Int by lazy { intent.getIntExtra("focusId", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_short)

        // ✅ 인텐트 값 파싱 (이게 없어 랜덤 모드로 갔던 것)
        mode = intent.getStringExtra("mode")                  // "user" | "random"
        userIdArg = intent.getIntExtra("userId", -1)
        sortArg = intent.getStringExtra("sort") ?: "latest"
        startIndexArg = intent.getIntExtra("startIndex", 0)

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

        if (mode == "user" && userIdArg > 0) {
            // ✅ 유저 숏츠 모드
            RetrofitInstance.apiService.getUserShorts(
                bearer = bearer,
                userId = userIdArg,
                sort = sortArg,
                page = page,
                size = pageSize
            ).enqueue(object : Callback<ShortVideoListResponse> {
                override fun onResponse(
                    call: Call<ShortVideoListResponse>,
                    response: Response<ShortVideoListResponse>
                ) {
                    val list = response.body()?.videos.orEmpty()

                    if (list.isEmpty() && page == 0) {
                        Toast.makeText(this@ShortsActivity, "영상이 없습니다.", Toast.LENGTH_SHORT).show()
                    } else if (list.isNotEmpty()) {
                        val start = items.size
                        val mapped = list.map { it.toShortObject() }
                        items.addAll(mapped)
                        adapter.notifyItemRangeInserted(start, mapped.size)
                        page += 1

                        if (start == 0) {
                            // 사용자가 누른 카드부터 재생
                            val initial = startIndexArg.coerceIn(0, items.lastIndex)
                            binding.shortsVP.post {
                                binding.shortsVP.setCurrentItem(initial, false)
                                playOnly(initial)
                            }
                        }
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<ShortVideoListResponse>, t: Throwable) {
                    t.printStackTrace()
                    isLoading = false
                }
            })
            return
        }
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

                    if (start == 0) {
                        binding.shortsVP.post {
                            val initial = if (focusId > 0) {
                                items.indexOfFirst { it.id == focusId }.takeIf { it >= 0 } ?: 0
                            } else 0
                            binding.shortsVP.setCurrentItem(initial, false)
                            playOnly(initial)
                        }
                    }
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
    // ShortVideoDto -> ShortObject 변환
    private fun ShortVideoDto.toShortObject(): ShortObject {
        return ShortObject(
            id        = this.id.toInt(),
            userName  = "",
            title  = this.title,
            videoUrl  = this.videoUrl ?: "",
            viewCount = this.viewCount,
            likeCount = this.likeCount,
            userId = this.userId.toInt()
        )
    }


}
