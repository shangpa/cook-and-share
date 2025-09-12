package com.example.test

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class OtherProfileActivity : AppCompatActivity() {

    private lateinit var tabRecipe: TextView
    private lateinit var tabVideo: TextView
    private lateinit var indicator: View
    private lateinit var followButton: Button

    private enum class Tab { RECIPE, VIDEO }
    private var currentTab = Tab.RECIPE

    /** 상대방 사용자 ID (인텐트로 넘어옴) */
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)

        // 전달받은 상대 유저 ID (없으면 -1)
        userId = intent.getLongExtra("userId", -1)

        findViewById<android.widget.ImageView>(R.id.backButton).setOnClickListener { finish() }

        tabRecipe = findViewById(R.id.tabRecipe)
        tabVideo  = findViewById(R.id.tabVideo)
        indicator = findViewById(R.id.indicator)
        followButton = findViewById(R.id.followButton)

        // 팔로우 버튼 토글 (백엔드 연동은 아래 주석 참고)
        followButton.setOnClickListener {
            followButton.isSelected = !followButton.isSelected
            followButton.text = if (followButton.isSelected) "팔로우중" else "팔로우"
            // TODO: Retrofit 호출로 팔로우 토글 API 연동
            // ex) RetrofitInstance.apiService.toggleFollow("Bearer $token", userId.toInt())
        }

        // ✅ 최초 탭 로드: "타인 레시피"가 보이도록 userId 전달
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tabContainer, RecipeTabFragment.newInstance(userId.toInt()))
                .commit()
        }
        highlightTab(Tab.RECIPE, moveIndicator = false)

        // 탭 클릭
        tabRecipe.setOnClickListener { switchTab(Tab.RECIPE) }
        tabVideo.setOnClickListener  { switchTab(Tab.VIDEO) }

        // onCreate 마지막 부분쯤, initIndicatorWidthAndPosition() 아래나 근처에 붙여넣기
        findViewById<LinearLayout>(R.id.followers).setOnClickListener {
            val intent = Intent(this, FollowActivity::class.java)
            intent.putExtra("targetUserId", userId.toInt())
            intent.putExtra("initialTab", "followers") // 기본: 팔로워 탭
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.followings).setOnClickListener {
            val intent = Intent(this, FollowActivity::class.java)
            intent.putExtra("targetUserId", userId.toInt())
            intent.putExtra("initialTab", "followings") // 기본: 팔로잉 탭
            startActivity(intent)
        }

        // 인디케이터 초기화
        initIndicatorWidthAndPosition()

    }

    private fun switchTab(tab: Tab) {
        if (currentTab == tab) return
        currentTab = tab

        when (tab) {
            Tab.RECIPE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabContainer, RecipeTabFragment.newInstance(userId.toInt()))
                    .commit()
            }
            Tab.VIDEO -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabContainer, VideoTabFragment.newInstance(userId.toInt()))
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

                    // 초기 위치(레시피 탭)
                    moveIndicatorTo(Tab.RECIPE, animate = false)
                }
            }
        )
    }

    /** 인디케이터 중심 = 텍스트 중심 */
    private fun moveIndicatorTo(tab: Tab, animate: Boolean) {
        val target = if (tab == Tab.RECIPE) tabRecipe else tabVideo

        val indicatorHalf = (indicator.layoutParams.width.takeIf { it > 0 } ?: dp(120)) / 2f
        val parent = indicator.parent as View
        val parentLoc = IntArray(2)
        parent.getLocationInWindow(parentLoc)

        val textCenterXInWindow = getTextCenterXInWindow(target)
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
        return tvLeftInWindow + tv.width / 2f
    }

    /** dp → px */
    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()
}
