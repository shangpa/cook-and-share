package com.example.test

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.model.profile.FollowUserResponse
import com.example.test.model.profile.FollowUserUi
import com.example.test.model.profile.ProfileSummaryResponse
import com.example.test.network.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class FollowActivity : AppCompatActivity() {

    private enum class Tab { FOLLOWERS, FOLLOWINGS }

    private lateinit var tabFollowers: TextView
    private lateinit var tabFollowings: TextView
    private lateinit var indicator: View
    private lateinit var rv: RecyclerView
    private lateinit var tvTitle: TextView

    private val adapter = FollowUserAdapter(::onFollowToggle)

    private var currentTab = Tab.FOLLOWERS
    private var targetUserId: Int = -1

    private val followers = mutableListOf<FollowUserUi>()
    private val followings = mutableListOf<FollowUserUi>()

    // ✅ 탭 숫자 표시용
    private var followerCount: Int = 0
    private var followingCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)

        targetUserId = intent.getIntExtra("targetUserId", App.prefs.userId.toInt())
        val initial = intent.getStringExtra("initialTab")

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        tvTitle = findViewById(R.id.tvTitle)
        tabFollowers = findViewById(R.id.tabFollowers)
        tabFollowings = findViewById(R.id.tabFollowings)
        indicator = findViewById(R.id.indicator)

        rv = findViewById(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        tabFollowers.setOnClickListener { switchTab(Tab.FOLLOWERS) }
        tabFollowings.setOnClickListener { switchTab(Tab.FOLLOWINGS) }

        initIndicator()

        currentTab = if (initial == "followings") Tab.FOLLOWINGS else Tab.FOLLOWERS
        highlightTab(currentTab, moveIndicator = false)

        // 초기엔 0으로 세팅
        updateTabTitles()

        // 닉네임 타이틀
        loadProfileNickname()

        // 목록 로드
        loadBothLists {
            bindCurrentList()
            moveIndicatorTo(currentTab, animate = false)
        }
    }

    /** 닉네임만 불러와 타이틀에 표시 */
    private fun loadProfileNickname() {
        val token = App.prefs.token ?: return
        RetrofitInstance.apiService.getProfileSummary(targetUserId, "Bearer $token")
            .enqueue(object : Callback<ProfileSummaryResponse> {
                override fun onResponse(
                    call: Call<ProfileSummaryResponse>,
                    response: Response<ProfileSummaryResponse>
                ) {
                    response.body()?.let { tvTitle.text = it.nickname }
                }
                override fun onFailure(call: Call<ProfileSummaryResponse>, t: Throwable) {}
            })
    }

    /** 팔로워/팔로잉 둘 다 로드 */
    private fun loadBothLists(onDone: () -> Unit) {
        val token = App.prefs.token ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        var done = 0
        fun oneDone() { if (++done == 2) onDone() }

        RetrofitInstance.apiService.getFollowers(targetUserId, "Bearer $token")
            .enqueue(object : Callback<List<FollowUserResponse>> {
                override fun onResponse(
                    call: Call<List<FollowUserResponse>>,
                    response: Response<List<FollowUserResponse>>
                ) {
                    val list = response.body().orEmpty()
                    followers.clear()
                    followers.addAll(list.map { it.toUi() })
                    followerCount = followers.size
                    updateTabTitles()          // ← 숫자 반영
                    oneDone()
                }
                override fun onFailure(call: Call<List<FollowUserResponse>>, t: Throwable) {
                    oneDone()
                }
            })

        RetrofitInstance.apiService.getFollowings(targetUserId, "Bearer $token")
            .enqueue(object : Callback<List<FollowUserResponse>> {
                override fun onResponse(
                    call: Call<List<FollowUserResponse>>,
                    response: Response<List<FollowUserResponse>>
                ) {
                    val list = response.body().orEmpty()
                    followings.clear()
                    followings.addAll(list.map { it.toUi() })
                    followingCount = followings.size
                    updateTabTitles()          // ← 숫자 반영
                    oneDone()
                }
                override fun onFailure(call: Call<List<FollowUserResponse>>, t: Throwable) {
                    oneDone()
                }
            })
    }

    private fun FollowUserResponse.toUi() = FollowUserUi(
        userId = userId,
        name = (name ?: "").ifBlank { username ?: "" }, // name 비면 username로 대체
        username = username ?: "",
        profileImageUrl = profileImageUrl,
        isFollowingByMe = followingByMe,
        isFollowingMe = followingMe
    )

    /** 탭 타이틀 숫자 갱신 */
    private fun updateTabTitles() {
        tabFollowers.text = "${followerCount} 팔로워"
        tabFollowings.text = "${followingCount} 팔로잉"
    }

    private fun initIndicator() {
        val tabRow = findViewById<View>(R.id.tabRow)
        tabRow.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    tabRow.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val want = dp(120)
                    if (indicator.layoutParams.width != want) {
                        indicator.layoutParams = indicator.layoutParams.apply { width = want }
                        indicator.requestLayout()
                    }
                }
            }
        )
    }

    private fun switchTab(tab: Tab) {
        if (currentTab == tab) return
        currentTab = tab
        highlightTab(tab, moveIndicator = true)
        bindCurrentList()
    }

    private fun highlightTab(tab: Tab, moveIndicator: Boolean) {
        val black = ContextCompat.getColor(this, R.color.black)
        val gray = 0xFF8A8A8A.toInt()
        if (tab == Tab.FOLLOWERS) {
            tabFollowers.setTextColor(black)
            tabFollowers.setTypeface(tabFollowers.typeface, Typeface.BOLD)
            tabFollowings.setTextColor(gray)
            tabFollowings.setTypeface(tabFollowings.typeface, Typeface.NORMAL)
        } else {
            tabFollowings.setTextColor(black)
            tabFollowings.setTypeface(tabFollowings.typeface, Typeface.BOLD)
            tabFollowers.setTextColor(gray)
            tabFollowers.setTypeface(tabFollowers.typeface, Typeface.NORMAL)
        }
        if (moveIndicator) moveIndicatorTo(tab, animate = true)
    }

    private fun moveIndicatorTo(tab: Tab, animate: Boolean) {
        val target = if (tab == Tab.FOLLOWERS) tabFollowers else tabFollowings
        val parent = indicator.parent as View
        val parentLoc = IntArray(2).apply { parent.getLocationInWindow(this) }
        val tvLoc = IntArray(2).apply { target.getLocationInWindow(this) }
        val tvCenterX = tvLoc[0] + target.width / 2f
        val indicatorHalf = (indicator.layoutParams.width.takeIf { it > 0 } ?: dp(120)) / 2f
        val leftX = tvCenterX - parentLoc[0] - indicatorHalf
        if (animate) indicator.animate().x(leftX).setDuration(200).start()
        else indicator.x = leftX
    }

    private fun bindCurrentList() {
        when (currentTab) {
            Tab.FOLLOWERS  -> adapter.submit(followers)
            Tab.FOLLOWINGS -> adapter.submit(followings)
        }
    }

    /** 팔로우 토글 시 팔로잉 수 반영 */
    // 기존 onFollowToggle 전체를 아래로 교체
    private fun onFollowToggle(pos: Int, item: FollowUserUi) {
        val token = App.prefs.token ?: return
        RetrofitInstance.apiService.toggleFollow(item.userId, "Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@FollowActivity, "처리 실패", Toast.LENGTH_SHORT).show()
                        return
                    }
                    // 서버 본문 없이 200 OK → 로컬 상태 반전
                    val nowFollowing = !item.isFollowingByMe
                    val changed = item.copy(isFollowingByMe = nowFollowing)
                    adapter.updateItem(pos, changed)

                    fun sync(list: MutableList<FollowUserUi>) {
                        val idx = list.indexOfFirst { it.userId == item.userId }
                        if (idx != -1) list[idx] = list[idx].copy(isFollowingByMe = nowFollowing)
                    }
                    sync(followers)
                    sync(followings)

                    // 팔로잉 리스트/숫자 업데이트
                    if (nowFollowing) {
                        if (followings.none { it.userId == item.userId }) {
                            followings.add(0, changed)
                            followingCount += 1
                            if (currentTab == Tab.FOLLOWINGS) {
                                adapter.addIfAbsentForFollowings(changed)
                            }
                        }
                    } else {
                        val removed = followings.removeAll { it.userId == item.userId }
                        if (removed) {
                            followingCount = (followingCount - 1).coerceAtLeast(0)
                            if (currentTab == Tab.FOLLOWINGS) {
                                adapter.removeByUserId(item.userId)
                            }
                        }
                    }
                    updateTabTitles()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@FollowActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).roundToInt()
}
