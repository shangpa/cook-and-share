package com.example.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.test.databinding.ActivityMainBinding
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.test.network.RetrofitInstance
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.test.model.recipeDetail.RecipeMainSearchResponseDTO
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.model.IngredientRecipeGroup
import com.example.test.model.TradePost.TradePostSimpleResponse
import android.Manifest
import androidx.recyclerview.widget.GridLayoutManager
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.LikedRecipeAdapter
import com.example.test.adapter.LikedVideoRecipeAdapter
import com.example.test.adapter.TradePostSimpleAdapter
import com.example.test.model.LoginInfoResponse
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.adapter.ExpiringIngredientAdapter
import com.example.test.ui.toExpiringUi
import com.example.test.ui.ExpiringItemUi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.example.test.model.pantry.PantryStockDto
import com.example.test.ui.fridge.PantryListActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

lateinit var binding: ActivityMainBinding
private var currentPage = 0
private lateinit var sliderHandler: Handler
private lateinit var sliderRunnable: Runnable
private lateinit var bannerPagerAdapter: BannerPagerAdapter
private lateinit var hotTradeRecyclerView: RecyclerView
private lateinit var hotTradeAdapter: TradePostSimpleAdapter
private lateinit var expiringAdapter: ExpiringIngredientAdapter

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
data class RecipeCardViewIds(
    val imageId: Int,
    val nameId: Int,
    val difficultyId: Int,
    val timeId: Int,
    val playBtnId: Int
)

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "기본 알림 채널"
        val descriptionText = "앱 기본 알림 채널입니다."
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("default", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 알림 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // 알림 채널
        createNotificationChannel(this)

        // 배너
        binding.homeBannerVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        bannerPagerAdapter = BannerPagerAdapter(this)
        setupBanner()
        setupArrowButtons()
        setupAutoSlide()

        TabBarUtils.setupTabBar(this)

        // 상단 아이콘 이동
        findViewById<ImageView>(R.id.searchIcon).setOnClickListener {
            startActivity(Intent(this, SearchMainActivity::class.java))
        }
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, NoticeActivity::class.java))
        }
        findViewById<ImageView>(R.id.mypageIcon).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }

        // 카테고리 3박스
        findViewById<LinearLayout>(R.id.materialExchange).setOnClickListener {
            startActivity(Intent(this, FridgeIngredientActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.recipeWrite).setOnClickListener {
            startActivity(Intent(this, RecipeWriteMain::class.java))
        }
        findViewById<LinearLayout>(R.id.villageKitchenChat).setOnClickListener {
            startActivity(Intent(this, MaterialChatActivity::class.java))
        }

        // 섹션 이동
        findViewById<TextView>(R.id.seeMoreTwo).setOnClickListener {
            startActivity(Intent(this, MaterialActivity::class.java))
        }
        findViewById<TextView>(R.id.seeMore).setOnClickListener {
            startActivity(Intent(this, PantryListActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.moreSeeRecipe).setOnClickListener {
            startActivity(Intent(this, RecipeTapActivity::class.java))
        }
        findViewById<TextView>(R.id.themeRecipe).setOnClickListener {
            startActivity(Intent(this, RecipeTapActivity::class.java))
        }
        findViewById<ImageView>(R.id.logoButton).setOnClickListener {
            startActivity(Intent(this, ShortsActivity::class.java))
        }

        // 테마별 레시피 탭 이동
        val categoryMap = mapOf(
            R.id.total to null,
            R.id.koreanFood to "koreaFood",
            R.id.westernFood to "westernFood",
            R.id.categoryItem to "japaneseFood",
            R.id.christmasSale to "chineseFood",
            R.id.vegetarianDiet to "vegetarianDiet",
            R.id.snack to "snack",
            R.id.relish to "alcoholSnack",
            R.id.sideDish to "sideDish"
        )
        categoryMap.forEach { (viewId, categoryValue) ->
            findViewById<LinearLayout>(viewId).setOnClickListener {
                startActivity(Intent(this, RecipeTapActivity::class.java).apply {
                    putExtra("selectedCategory", categoryValue)
                })
            }
        }

        val token = App.prefs.token.toString()

        // 동네주방 HOT
        hotTradeRecyclerView = findViewById(R.id.hotTradeRecyclerView)
        hotTradeAdapter = TradePostSimpleAdapter(mutableListOf()) { post ->
            startActivity(Intent(this, MaterialDetailActivity::class.java).apply {
                putExtra("postId", post.tradePostId)
            })
        }
        hotTradeRecyclerView.adapter = hotTradeAdapter
        loadHotTradePosts()

        // 임박 재료 리사이클러
        findViewById<RecyclerView>(R.id.recyclerExpiring).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            expiringAdapter = ExpiringIngredientAdapter(emptyList()) { recipeId ->
                startActivity(Intent(this@MainActivity, RecipeSeeMainActivity::class.java).apply {
                    putExtra("recipeId", recipeId)
                })
            }
            adapter = expiringAdapter
        }
        loadPantryExpiringSection() // ← 신규 섹션 로드

        // (OLD) getMyFridges로 고정 UI에 텍스트 박아넣던 블록은 전부 삭제했습니다. (컴파일 오류 원인)

        // “선호하는 레시피” 제목
        val likedRecipeUserNameTextView: TextView = findViewById(R.id.likedRecipeUserName)
        if (token.isBlank()) {
            likedRecipeUserNameTextView.text = "쿡앤쉐어 유저들이 선호하는 레시피"
        } else {
            RetrofitInstance.apiService.getUserInfo("Bearer $token")
                .enqueue(object : Callback<LoginInfoResponse> {
                    override fun onResponse(
                        call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            likedRecipeUserNameTextView.text = "${response.body()!!.name}님이 선호하는 레시피"
                        } else {
                            likedRecipeUserNameTextView.text = "쿡앤쉐어 유저들이 선호하는 레시피"
                        }
                    }
                    override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                        likedRecipeUserNameTextView.text = "쿡앤쉐어 유저들이 선호하는 레시피"
                    }
                })
        }

        loadPreferredRecipes()
        loadLikedVideoRecipes()
    }

    private fun parseLocalDateFlexible(s: String?): LocalDate? {
        if (s.isNullOrBlank()) return null
        val txt = s.trim()
        val candidates = listOf("yyyy-MM-dd", "yyyy.MM.dd", "yyyy/MM/dd")
        for (p in candidates) {
            try { return LocalDate.parse(txt, java.time.format.DateTimeFormatter.ofPattern(p)) } catch (_: Exception) {}
        }
        // ISO 형식 등 기본 파서도 시도
        return runCatching { LocalDate.parse(txt) }.getOrNull()
    }

    /** 팬트리 랜덤 1개 → 7일 임박 재고 랜덤 2개 → 각 재료별 추천 레시피 2개 */
    private fun loadPantryExpiringSection() {
        val token = App.prefs.token ?: return
        val bearer = "Bearer $token"
        val today = LocalDate.now()
        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        lifecycleScope.launch {
            try {
                // 1) 팬트리 목록 → 랜덤 1개
                val pantries = RetrofitInstance.pantryApi.listPantries(bearer)
                if (pantries.isEmpty()) return@launch
                val chosen = pantries.shuffled().first()
                val pantryId = chosen.id
                val pantryName = chosen.name

                // 2) 재고
                val stocks: List<PantryStockDto> =
                    RetrofitInstance.pantryApi.listPantryStocks(bearer, pantryId)

                // 3) 14일 이내 임박
                val soon: List<PantryStockDto> = stocks.filter { s ->
                    val d = parseLocalDateFlexible(s.expiresAt)
                    if (d == null) false
                    else {
                        val diff = ChronoUnit.DAYS.between(today, d)
                        diff in 0..14          // 오늘~14일 이내만
                    }
                }

                // 4) 후보 확정 + 뽑기
                val base: List<PantryStockDto> =
                    if (soon.isNotEmpty()) soon
                    else stocks.filter { s ->
                        parseLocalDateFlexible(s.expiresAt)?.let { it >= today } == true
                    }
                val picked: List<PantryStockDto> = base.shuffled().take(2)
                if (picked.isEmpty()) return@launch

                // 5) 재료명 리스트 — 제네릭으로 타입을 못박아주기
                val ingredientNames: List<String> =
                    picked.map<PantryStockDto, String> { dto -> dto.ingredientName }

                // 5-1) 재료명으로 그룹 추천 호출해서 groups 만들어두기 (중요!)
                val groups: List<IngredientRecipeGroup> =
                    suspendCancellableCoroutine { cont ->
                        RetrofitInstance.apiService
                            .recommendGrouped(ingredientNames, bearer)
                            .enqueue(object : Callback<List<IngredientRecipeGroup>> {
                                override fun onResponse(
                                    call: Call<List<IngredientRecipeGroup>>,
                                    response: Response<List<IngredientRecipeGroup>>
                                ) {
                                    cont.resume(response.body() ?: emptyList())
                                }

                                override fun onFailure(
                                    call: Call<List<IngredientRecipeGroup>>,
                                    t: Throwable
                                ) {
                                    cont.resume(emptyList())
                                }
                            })
                    }

                // 6) 그룹 추천 받은 뒤 매핑 — 이제 groups 타입이 있으니 추론 OK
                val recipeMap: Map<String, List<RecipeMainSearchResponseDTO>> =
                    groups.associate { g ->
                        g.ingredient to g.recipes.map { rs ->
                            RecipeMainSearchResponseDTO(
                                recipeId    = rs.recipeId,
                                title       = rs.title,
                                mainImageUrl= rs.mainImageUrl,         // String? 로 받으니 그대로
                                difficulty  = rs.difficulty,           // 그대로
                                cookingTime = rs.cookingTime,          // 그대로
                                liked       = false,                   // ← DTO에 없으니 기본값
                                videoUrl    = rs.videoUrl,             // 그대로(Nullable)
                                viewCount   = rs.viewCount,            // 그대로
                                likes       = rs.likes,                // 그대로
                                averageRating = 0.0,                   // ← DTO에 없으니 기본값
                                reviewCount   = 0,                     // ← DTO에 없으니 기본값
                                user        = rs.user,                 // 그대로
                                category    = rs.category,             // 그대로
                                createdAt   = rs.createdAt             // 그대로
                            )
                        }.shuffled().take(2)
                    }

                // 7) UI 모델
                val uiList: List<com.example.test.ui.ExpiringItemUi> =
                    picked.map<PantryStockDto, com.example.test.ui.ExpiringItemUi> { stock ->
                        val recipes = recipeMap[stock.ingredientName] ?: emptyList()
                        stock.toExpiringUi(
                            pantryName = pantryName,
                            pantryId = pantryId,
                            recipes = recipes
                        )
                    }

                expiringAdapter.submit(uiList)
                findViewById<TextView>(R.id.fridgeMainText)?.text =
                    "유통기한이 임박한 냉장고 재료를 알려드려요!"

            } catch (e: Exception) {
                Log.e("MainActivity", "loadPantryExpiringSection failed", e)
            }
        }
    }

    // 2. 배너 세팅
    private fun setupBanner() {
        val fragmentList = listOf(
            AllBannerFragment(),
            AllBannerFragment2(),
            AllBannerFragment3(),
            AllBannerFragment4(),
            AllBannerFragment5()
        )
        bannerPagerAdapter.submitList(fragmentList)
        binding.homeBannerVp.adapter = bannerPagerAdapter
    }

    // 3. 화살표 버튼 세팅
    private fun setupArrowButtons() {
        binding.leftArrow.setOnClickListener {
            val currentItem = binding.homeBannerVp.currentItem
            if (currentItem > 0) binding.homeBannerVp.currentItem = currentItem - 1
        }
        binding.rightArrow.setOnClickListener {
            val currentItem = binding.homeBannerVp.currentItem
            if (currentItem < (binding.homeBannerVp.adapter?.itemCount ?: 0) - 1) {
                binding.homeBannerVp.currentItem = currentItem + 1
            }
        }
    }

    // 4. 자동 슬라이드 세팅
    private fun setupAutoSlide() {
        sliderHandler = Handler(Looper.getMainLooper())

        sliderRunnable = object : Runnable {
            override fun run() {
                val itemCount = binding.homeBannerVp.adapter?.itemCount ?: 0
                if (itemCount == 0) return
                if (currentPage == itemCount) currentPage = 0
                binding.homeBannerVp.currentItem = currentPage++
                sliderHandler.postDelayed(this, 3000)
            }
        }

        val space = (5 * resources.displayMetrics.density).toInt()
        binding.homeBannerVp.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: android.view.View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.left = space
                outRect.right = space
            }
        })

        binding.homeBannerVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position + 1
                val itemCount = binding.homeBannerVp.adapter?.itemCount ?: 0
                if (itemCount > 0) binding.pageIndicator.text = "${position + 1} / $itemCount"
                binding.topTextView.text = "Top ${position + 1}"
            }
        })

        binding.homeBannerVp.apply {
            adapter = bannerPagerAdapter
            offscreenPageLimit = 3
            setPageTransformer { page, position ->
                val scale = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
                page.scaleY = scale
            }
        }
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    // (남겨도 무해) 좋아요 버튼 토글 유틸 — 현재는 사용 안 함
    private fun setupHeartToggle(buttons: List<ImageView>, initiallyLiked: Boolean) {
        val TAG_IS_LIKED = R.id.tag_is_liked  // ✅ 우리가 만든 태그 id
        buttons.forEach { button ->
            button.setTag(TAG_IS_LIKED, initiallyLiked)
            button.setOnClickListener {
                val isLiked = it.getTag(TAG_IS_LIKED) as Boolean
                val newLiked = !isLiked
                if (newLiked) {
                    button.setImageResource(R.drawable.ic_heart_fill)
                    Toast.makeText(this, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    button.setImageResource(R.drawable.ic_heart_list)
                }
                it.setTag(TAG_IS_LIKED, newLiked)
            }
        }
    }

    // 동네주방
    private fun loadHotTradePosts() {
        RetrofitInstance.apiService.getPopularTradePosts("Bearer ${App.prefs.token}")
            .enqueue(object : Callback<List<TradePostSimpleResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostSimpleResponse>>,
                    response: Response<List<TradePostSimpleResponse>>
                ) {
                    if (response.isSuccessful) {
                        hotTradeAdapter.updateData(response.body()?.toMutableList() ?: mutableListOf())
                    } else {
                        Log.e("HOT_TRADE", "서버 응답 실패: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<List<TradePostSimpleResponse>>, t: Throwable) {
                    Log.e("HOT_TRADE", "네트워크 오류: ${t.message}")
                }
            })
    }

    private fun convertTimeAgo(dateStr: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val postDate = LocalDate.parse(dateStr, formatter)
            val daysAgo = ChronoUnit.DAYS.between(postDate, LocalDate.now())
            when (daysAgo) {
                0L -> "오늘"
                1L -> "어제"
                else -> "${daysAgo}일 전"
            }
        } catch (e: Exception) {
            "-"
        }
    }

    private fun loadPreferredRecipes() {
        val token = App.prefs.token
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerLikedRecipes)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val call: Call<List<RecipeMainSearchResponseDTO>> = if (token.isNullOrBlank()) {
            RetrofitInstance.apiService.getTopViewedRecipes()
        } else {
            RetrofitInstance.apiService.getMainLikedRecipes("Bearer $token")
        }

        call.enqueue(object : Callback<List<RecipeMainSearchResponseDTO>> {
            override fun onResponse(
                call: Call<List<RecipeMainSearchResponseDTO>>,
                response: Response<List<RecipeMainSearchResponseDTO>>
            ) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.take(6) ?: return
                    recyclerView.adapter = LikedRecipeAdapter(recipes) { selectedRecipe ->
                        startActivity(Intent(this@MainActivity, RecipeSeeMainActivity::class.java).apply {
                            putExtra("recipeId", selectedRecipe.recipeId)
                        })
                    }
                } else {
                    Log.e("MainActivity", "레시피 응답 실패: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<RecipeMainSearchResponseDTO>>, t: Throwable) {
                Log.e("MainActivity", "레시피 불러오기 실패", t)
            }
        })
    }

    private fun loadLikedVideoRecipes() {
        val token = App.prefs.token ?: return
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerLikedVideoRecipes)
        recyclerView.layoutManager = GridLayoutManager(this, 1)

        RetrofitInstance.apiService.getMainLikedRecipes("Bearer $token")
            .enqueue(object : Callback<List<RecipeMainSearchResponseDTO>> {
                override fun onResponse(
                    call: Call<List<RecipeMainSearchResponseDTO>>,
                    response: Response<List<RecipeMainSearchResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        val recipes = response.body()?.filter { !it.videoUrl.isNullOrBlank() } ?: return
                        recyclerView.adapter = LikedVideoRecipeAdapter(recipes) { selectedRecipe ->
                            startActivity(Intent(this@MainActivity, RecipeSeeMainActivity::class.java).apply {
                                putExtra("recipeId", selectedRecipe.recipeId)
                            })
                        }
                    } else {
                        Log.e("MainActivity", "관심 동영상 응답 실패: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<List<RecipeMainSearchResponseDTO>>, t: Throwable) {
                    Log.e("MainActivity", "관심 동영상 레시피 불러오기 실패", t)
                }
            })
    }
}
