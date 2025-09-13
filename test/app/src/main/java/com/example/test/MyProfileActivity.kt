package com.example.test

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.test.model.profile.ProfileSummaryResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class MyProfileActivity : AppCompatActivity() {

    private lateinit var tabRecipe: TextView
    private lateinit var tabVideo: TextView
    private lateinit var indicator: View

    private lateinit var tvNickname: TextView
    private lateinit var tvFollowerCount: TextView
    private lateinit var tvFollowingCount: TextView
    private lateinit var tvRecipeCount: TextView
    private lateinit var tvVideoCount: TextView

    private enum class Tab { RECIPE, VIDEO }
    private var currentTab = Tab.RECIPE

    // 화면에서 볼 대상 유저 ID
    private var targetUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        tabRecipe = findViewById(R.id.tabRecipe)
        tabVideo  = findViewById(R.id.tabVideo)
        indicator = findViewById(R.id.indicator)

        tvNickname       = findViewById(R.id.profileName)
        tvFollowerCount  = findViewById(R.id.follow)
        tvFollowingCount = findViewById(R.id.following)
        tvRecipeCount    = findViewById(R.id.recipe)
        tvVideoCount     = findViewById(R.id.video)

        targetUserId = intent.getIntExtra("targetUserId", -1)
        if (targetUserId == -1) {
            // fallback: 내 프로필이라면 Prefs에서 가져오기
            targetUserId = App.prefs.userId.toInt()
        }

        // 최초 탭 로드(레시피)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.tabContainer, RecipeTabFragment.newInstance())
                .commit()
        }
        highlightTab(Tab.RECIPE, moveIndicator = false)

        // 클릭 리스너
        tabRecipe.setOnClickListener { switchTab(Tab.RECIPE) }
        tabVideo.setOnClickListener  { switchTab(Tab.VIDEO) }

        // 인디케이터 초기 폭/위치
        initIndicatorWidthAndPosition()

        // backButton 클릭했을 때 MypageActivity 이동
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // 팔로워/팔로잉 클릭 리스너
        findViewById<LinearLayout>(R.id.followers).setOnClickListener {
            val intent = Intent(this, FollowActivity::class.java)
            intent.putExtra("targetUserId", targetUserId)
            intent.putExtra("initialTab", "followers")
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.followings).setOnClickListener {
            val intent = Intent(this, FollowActivity::class.java)
            intent.putExtra("targetUserId", targetUserId)
            intent.putExtra("initialTab", "followings")
            startActivity(intent)
        }

        // 프로필 요약 로드
        loadProfileSummary()
    }

    // ----- 프로필 요약 불러오기
    private fun loadProfileSummary() {
        val token = App.prefs.token
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (targetUserId == -1) {
            Toast.makeText(this, "대상 유저 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.apiService.getProfileSummary(targetUserId, "Bearer $token")
            .enqueue(object : Callback<ProfileSummaryResponse> {
                override fun onResponse(
                    call: Call<ProfileSummaryResponse>,
                    response: Response<ProfileSummaryResponse>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        Toast.makeText(this@MyProfileActivity, "프로필 로딩 실패", Toast.LENGTH_SHORT).show()
                        return
                    }
                    bindSummary(body)
                }

                override fun onFailure(call: Call<ProfileSummaryResponse>, t: Throwable) {
                    Toast.makeText(this@MyProfileActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun bindSummary(data: ProfileSummaryResponse) {
        // 닉네임
        tvNickname.text = data.nickname

        // ✅ 팔로워/팔로잉 수 반영
        tvFollowerCount.text  = data.followersCount.toString()
        tvFollowingCount.text = data.followingCount.toString()

        // 레시피/동영상 수
        tvRecipeCount.text = data.recipeCount.toString()
        tvVideoCount.text  = data.shortsCount.toString()

        // TODO: 타인 프로필이면 팔로우 버튼 추가 시 data.following / data.mine 으로 상태 제어
        // if (!data.mine) { btnFollow.visibility = View.VISIBLE ... }
    }

    private fun switchTab(tab: Tab) {
        if (currentTab == tab) return
        currentTab = tab

        when (tab) {
            Tab.RECIPE -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.tabContainer, RecipeTabFragment.newInstance())
                    .commit()
            }
            Tab.VIDEO -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.tabContainer, VideoTabFragment.newInstance(targetUserId))
                    .commit()
            }
        }
        highlightTab(tab, moveIndicator = false)
        moveIndicatorTo(tab, animate = true)
    }

    private fun highlightTab(tab: Tab, moveIndicator: Boolean = true) {
        val black = ContextCompat.getColor(this, R.color.black)
        val gray  = 0xFF8A8A8A.toInt()

        if (tab == Tab.RECIPE) {
            tabRecipe.setTextColor(black)
            tabRecipe.setTypeface(tabRecipe.typeface, Typeface.BOLD)
            tabVideo.setTextColor(gray)
            tabVideo.setTypeface(tabVideo.typeface, Typeface.NORMAL)
            if (moveIndicator) moveIndicatorTo(Tab.RECIPE, animate = true)
        } else {
            tabVideo.setTextColor(black)
            tabVideo.setTypeface(tabVideo.typeface, Typeface.BOLD)
            tabRecipe.setTextColor(gray)
            tabRecipe.setTypeface(tabRecipe.typeface, Typeface.NORMAL)
            if (moveIndicator) moveIndicatorTo(Tab.VIDEO, animate = true)
        }
    }

    /** 인디케이터 폭, 초기 위치 */
    private fun initIndicatorWidthAndPosition() {
        val tabRow = findViewById<LinearLayout>(R.id.tabRow)
        tabRow.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    tabRow.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // 폭 = 120dp 고정
                    val wantWidth = dp(120)
                    if (indicator.layoutParams.width != wantWidth) {
                        indicator.layoutParams = indicator.layoutParams.apply { width = wantWidth }
                        indicator.requestLayout()
                    }

                    // 초기 위치(레시피 탭 텍스트 중앙 하단)
                    moveIndicatorTo(Tab.RECIPE, animate = false)
                }
            }
        )
    }

    /** 인디케이터 중심 = 텍스트 중심 */
    private fun moveIndicatorTo(tab: Tab, animate: Boolean) {
        val target = if (tab == Tab.RECIPE) tabRecipe else tabVideo

        // 인디케이터 반쪽 폭
        val indicatorHalf = (indicator.layoutParams.width.takeIf { it > 0 } ?: dp(120)) / 2f

        // 부모/자식 좌표계를 윈도우 기준으로 통일
        val parent = indicator.parent as View
        val parentLoc = IntArray(2)
        parent.getLocationInWindow(parentLoc)

        // 텍스트의 실제 중심 X (윈도우 기준)
        val textCenterXInWindow = getTextCenterXInWindow(target)

        // parent 기준 '왼쪽 X' = (텍스트중앙X - 부모왼쪽X - 인디케이터반폭)
        val leftX = textCenterXInWindow - parentLoc[0] - indicatorHalf

        if (animate) {
            indicator.animate().x(leftX).setDuration(200).start()
        } else {
            indicator.x = leftX
        }
    }

    /** 계산 */
    private fun getTextCenterXInWindow(tv: TextView): Float {
        val tvLoc = IntArray(2)
        tv.getLocationInWindow(tvLoc)
        val tvLeftInWindow = tvLoc[0].toFloat()

        val layout = tv.layout
        if (layout != null && layout.lineCount > 0) {
            val line = 0
            val lineLeft = layout.getLineLeft(line)
            val lineRight = layout.getLineRight(line)
            val textWidth = lineRight - lineLeft

            val available = tv.width - tv.paddingLeft - tv.paddingRight
            val textStartInTv = tv.paddingLeft + (available - textWidth) / 2f

            return tvLeftInWindow + textStartInTv + textWidth / 2f
        }
        // 폴백: 뷰 중앙
        return tvLeftInWindow + tv.width / 2f
    }

    /** dp → px */
    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()
}
