package com.example.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.example.test.network.ApiService
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.test.model.RecipeSearchResponseDTO
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.model.IngredientRecipeGroup
import com.google.firebase.messaging.FirebaseMessaging
import com.example.test.model.TradePost.TradePostSimpleResponse
import java.text.DecimalFormat
import android.Manifest

lateinit var binding: ActivityMainBinding
private var currentPage = 0
private lateinit var sliderHandler: Handler
private lateinit var sliderRunnable: Runnable
private lateinit var bannerPagerAdapter: BannerPagerAdapter

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

        // 알림 권한 설정
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

        //알림용
        createNotificationChannel(this)

        // 배너
        val bannerAdapter = BannerViewPagerAdapter(this@MainActivity)
        binding.homeBannerVp.adapter = bannerAdapter
        binding.homeBannerVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        bannerPagerAdapter = BannerPagerAdapter(this)

        setupBanner()
        setupArrowButtons()
        setupAutoSlide()

        // tapVillageKitchenIcon 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenIcon: ImageView = findViewById(R.id.tapVillageKitchenIcon)
        tapVillageKitchenIcon.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapVillageKitchenText 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenText: TextView = findViewById(R.id.tapVillageKitchenText)
        tapVillageKitchenText.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeIcon 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeIcon: ImageView = findViewById(R.id.tapRecipeIcon)
        tapRecipeIcon.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeText 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeText: TextView = findViewById(R.id.tapRecipeText)
        tapRecipeText.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityIcon 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityIcon: ImageView = findViewById(R.id.tapCommunityIcon)
        tapCommunityIcon.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityText 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityText: TextView = findViewById(R.id.tapCommunityText)
        tapCommunityText.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeIcon 클릭했을 때 FridgeActivity 이동
        val tapFridgeIcon: ImageView = findViewById(R.id.tapFridgeIcon)
        tapFridgeIcon.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeText 클릭했을 때 FridgeActivity 이동
        val tapFridgeText: TextView = findViewById(R.id.tapFridgeText)
        tapFridgeText.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // searchIcon 클릭했을 때 SearchMainActivity 이동
        val searchIcon: ImageView = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMainActivity::class.java)
            startActivity(intent)
        }

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: ImageView = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        // mypageIcon 클릭했을 때 MypageActivity 이동
        val mypageIcon: ImageView = findViewById(R.id.mypageIcon)
        mypageIcon.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // materialExchange 클릭했을 때 FridgeIngredientActivity 이동
        val materialExchange: LinearLayout = findViewById(R.id.materialExchange)
        materialExchange.setOnClickListener {
            val intent = Intent(this, FridgeIngredientActivity::class.java)
            startActivity(intent)
        }

        // recipeWrite 클릭했을 때 RecipeWriteMain 이동
        val recipeWrite: LinearLayout = findViewById(R.id.recipeWrite)
        recipeWrite.setOnClickListener {
            val intent = Intent(this, RecipeWriteMain::class.java)
            startActivity(intent)
        }

        // villageKitchenChat 클릭했을 때 MaterialChatActivity 이동
        val villageKitchenChat: LinearLayout = findViewById(R.id.villageKitchenChat)
        villageKitchenChat.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }

        // seeMoreTwo 클릭했을 때 MaterialActivity 이동
        val seeMoreTwo: TextView = findViewById(R.id.seeMoreTwo)
        seeMoreTwo.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        //seeMore 클릭했을 때 FridgeActivity 이동
        val seeMore: TextView = findViewById(R.id.seeMore)
        seeMore.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // moreSeeRecipe 클릭했을 때 RecipeActivity 이동
        val moreSeeRecipe: LinearLayout = findViewById(R.id.moreSeeRecipe)
        moreSeeRecipe.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // interestList 클릭했을 때 RecipeSeeVideoActivity 이동
        val interestList: GridLayout = findViewById(R.id.interestList)
        interestList.setOnClickListener {
            val intent = Intent(this, RecipeSeeVideoActivity::class.java)
            startActivity(intent)
        }

        // themeRecipe 클릭했을 때 RecipeActivity 이동
        val themeRecipe: TextView = findViewById(R.id.themeRecipe)
        themeRecipe.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }
        //테마별 레시피
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
                val intent = Intent(this, RecipeActivity::class.java)
                intent.putExtra("selectedCategory", categoryValue)
                startActivity(intent)
            }
        }

        //좋아요 버튼(안채워진거)
        setupHeartToggle(
            listOf(
                findViewById(R.id.fridgeHeartTwo),
                findViewById(R.id.fridgeHeartThree),
                findViewById(R.id.fridgeHeartFour),
                findViewById(R.id.saveHeartTwo),
                findViewById(R.id.saveHeartFive),
                findViewById(R.id.saveHeartSix)
            ),
            initiallyLiked = false
        )

        //좋아요 버튼(채워진거)
        setupHeartToggle(
            listOf(
                findViewById(R.id.fridgeHeart),
                findViewById(R.id.saveHeart),
                findViewById(R.id.saveHeartThree),
                findViewById(R.id.saveHeartFour),
                findViewById(R.id.interestHeart),
                findViewById(R.id.interestHeartTwo),
                findViewById(R.id.interestHeartThree),
                findViewById(R.id.interestHeartFour),
                findViewById(R.id.interestHeartFive)
            ),
            initiallyLiked = true
        )

        val token = App.prefs.token.toString()
        val call = RetrofitInstance.apiService.getMainMessage("Bearer $token")
        loadPopularKitchenPosts()

        call.enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    val message = response.body()?.get("fridgeMainText") ?: ""
                    findViewById<TextView>(R.id.fridgeMainText).text = message
                } else {
                    findViewById<TextView>(R.id.fridgeMainText).text =
                        "로그인 후 유통기한이 임박한 냉장고 재료를 확인해보세요!"
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("MainAPI", "연결 실패", t) // 로그 찍기
                findViewById<TextView>(R.id.fridgeMainText).text = "서버 연결 실패"
            }
        })

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.getMyFridges("Bearer $token")

                if (response.isSuccessful) {
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                    val fridgeList = response.body() ?: emptyList()

                    val filteredList = fridgeList.filter {
                        it.dateOption == "유통기한" &&
                                it.fridgeDate != null &&
                                try {
                                    ChronoUnit.DAYS.between(today, LocalDate.parse(it.fridgeDate, formatter)) in 0..14
                                } catch (e: Exception) {
                                    false
                                }
                    }

                    val fallbackList = fridgeList.filter { it.dateOption == "유통기한" }

                    val targetList = if (filteredList.isNotEmpty()) {
                        filteredList.shuffled().take(2)
                    } else {
                        fallbackList.shuffled().take(2)
                    }

                    val boxIds = listOf(
                        Triple(R.id.fridgeMaterialName1, R.id.fridgeMaterialCount1, R.id.fridgeMaterialUnit1) to
                                Pair(R.id.fridgeIngredientDateLabel1, R.id.fridgeIngredientDateText1),
                        Triple(R.id.fridgeMaterialName2, R.id.fridgeMaterialCount2, R.id.fridgeMaterialUnit2) to
                                Pair(R.id.fridgeIngredientDateLabel2, R.id.fridgeIngredientDateText2)
                    )

                    targetList.forEachIndexed { index, item ->
                        val (nameId, countId, unitId) = boxIds[index].first
                        val (labelId, dateId) = boxIds[index].second

                        val quantityStr = if (item.quantity % 1.0 == 0.0)
                            item.quantity.toInt().toString()
                        else
                            item.quantity.toString()

                        findViewById<TextView>(nameId).text = item.ingredientName
                        findViewById<TextView>(countId).text = quantityStr
                        findViewById<TextView>(unitId).text = item.unitDetail
                        findViewById<TextView>(labelId).text = "유통기한 : "
                        findViewById<TextView>(dateId).text = item.fridgeDate
                    }

                    val selectedIngredients = targetList.map { it.ingredientName }

                    RetrofitInstance.apiService.recommendGrouped(selectedIngredients, "Bearer $token")
                        .enqueue(object : Callback<List<IngredientRecipeGroup>> {
                            override fun onResponse(
                                call: Call<List<IngredientRecipeGroup>>,
                                response: Response<List<IngredientRecipeGroup>>
                            ) {
                                if (response.isSuccessful) {
                                    val groups = response.body() ?: return

                                    val uiMapping = listOf(
                                        Quadruple(
                                            R.id.fridgeMainRecipeImage1,
                                            R.id.fridgeMainRecipeName1,
                                            R.id.fridgeMainRecipeDifficulty1,
                                            R.id.fridgeMainPlayBtn1
                                        ) to R.id.fridgeMainRecipeTime1,
                                        Quadruple(
                                            R.id.fridgeMainRecipeImage2,
                                            R.id.fridgeMainRecipeName2,
                                            R.id.fridgeMainRecipeDifficulty2,
                                            R.id.fridgeMainPlayBtn2
                                        ) to R.id.fridgeMainRecipeTime2,
                                        Quadruple(
                                            R.id.fridgeMainRecipeImage3,
                                            R.id.fridgeMainRecipeName3,
                                            R.id.fridgeMainRecipeDifficulty3,
                                            R.id.fridgeMainPlayBtn3
                                        ) to R.id.fridgeMainRecipeTime3,
                                        Quadruple(
                                            R.id.fridgeMainRecipeImage4,
                                            R.id.fridgeMainRecipeName4,
                                            R.id.fridgeMainRecipeDifficulty4,
                                            R.id.fridgeMainPlayBtn4
                                        ) to R.id.fridgeMainRecipeTime4
                                    )

                                    var uiIndex = 0

                                    groups.take(2).forEachIndexed { groupIndex, group ->
                                        group.recipes.take(2)
                                            .forEachIndexed { recipeIndex, recipe ->
                                                val uiIndex =
                                                    groupIndex * 2 + recipeIndex  // ← 핵심
                                                if (uiIndex >= uiMapping.size) return@forEachIndexed

                                                val (imageId, nameId, difficultyId, playBtnId) = uiMapping[uiIndex].first
                                                val timeId = uiMapping[uiIndex].second

                                                val imageView =
                                                    findViewById<ImageView>(imageId)
                                                val nameView =
                                                    findViewById<TextView>(nameId)
                                                val difficultyView =
                                                    findViewById<TextView>(difficultyId)
                                                val timeView =
                                                    findViewById<TextView>(timeId)
                                                val playBtn =
                                                    findViewById<ImageView>(playBtnId)

                                                // 이미지는 그대로 유지
                                                if (recipe.mainImageUrl.isNullOrBlank()) {
                                                    imageView.setImageResource(R.drawable.image_recently_stored_materials_food)
                                                } else {
                                                    Glide.with(this@MainActivity)
                                                        .load(recipe.mainImageUrl)
                                                        .into(imageView)
                                                }

                                                nameView.text = recipe.title
                                                difficultyView.text = recipe.difficulty
                                                timeView.text = "${recipe.cookingTime}분"
                                                playBtn.visibility =
                                                    if (recipe.videoUrl.isNullOrBlank()) View.GONE else View.VISIBLE
                                            }
                                    }

                                } else {
                                    Log.e("추천 레시피", "서버 응답 오류: ${response.code()}")
                                }
                            }

                            override fun onFailure(
                                call: Call<List<IngredientRecipeGroup>>,
                                t: Throwable
                            ) {
                                Log.e("추천 레시피", "네트워크 오류", t)
                            }
                        })
                } else {
                    Log.e("FridgeMain", "getMyFridges 실패: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("FridgeMain", "재료 불러오기 실패", e)
            }
        }

        lifecycleScope.launch {
            try {
                val token = App.prefs.token.toString()
                val response = RetrofitInstance.apiService.getMyFridges("Bearer $token")

                if (response.isSuccessful) {
                    val fridgeList = response.body() ?: emptyList()

                    // 재료 이름만 추출
                    val ingredientNames = fridgeList.map { it.ingredientName }

                } else {
                    Log.e("Fridge API", "재료 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Fridge API", "오류 발생", e)
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
            if (currentItem > 0) {
                binding.homeBannerVp.currentItem = currentItem - 1
            }
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
        val sliderHandler = Handler(Looper.getMainLooper())

        sliderRunnable = object : Runnable {
            override fun run() {
                val itemCount = binding.homeBannerVp.adapter?.itemCount ?: 0
                if (itemCount == 0) return

                if (currentPage == itemCount) {
                    currentPage = 0
                }

                binding.homeBannerVp.currentItem = currentPage++
                sliderHandler.postDelayed(this, 3000)
            }
        }

        // 양 옆 배너 간격 추가 (10dp)
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
                if (itemCount > 0) {
                    binding.pageIndicator.text = "${position + 1} / $itemCount"
                }

                binding.topTextView.text = "Top ${position + 1}"
            }
        })

        binding.homeBannerVp.apply {
            adapter = bannerPagerAdapter
            offscreenPageLimit = 3 // 주변 배너 미리 로딩

            setPageTransformer { page, position ->
                val scale = 0.85f + (1 - Math.abs(position)) * 0.15f
                page.scaleY = scale // 가운데 배너 약간 크게
            }
        }

        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    //좋아요 버튼
    private fun setupHeartToggle(buttons: List<ImageView>, initiallyLiked: Boolean) {
        val TAG_IS_LIKED = R.id.fridgeHeart

        // 버튼 ID → 현재 레이아웃 ID + 다음 레이아웃 ID 매핑
        val buttonToLayoutMap = mapOf(
            R.id.interestHeart to Pair(R.id.interestItem1, R.id.interestItem2),
            R.id.interestHeartTwo to Pair(R.id.interestItem2, R.id.interestItem3),
            R.id.interestHeartThree to Pair(R.id.interestItem3, R.id.interestItem4),
            R.id.interestHeartFour to Pair(R.id.interestItem4, R.id.interestItem5),
            R.id.interestHeartFive to Pair(R.id.interestItem5, null) // 마지막 항목은 다음이 없음
        )

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

                    // 현재 + 다음 레이아웃 처리
                    val layoutPair = buttonToLayoutMap[button.id]
                    layoutPair?.let { (currentId, nextId) ->
                        // 현재 레이아웃 GONE
                        findViewById<View>(currentId)?.visibility = View.GONE

                        // 다음 레이아웃 marginTop 제거
                        nextId?.let { nid ->
                            val nextLayout = findViewById<View>(nid)
                            val params = nextLayout.layoutParams as? ViewGroup.MarginLayoutParams
                            params?.let {
                                it.topMargin = 0
                                nextLayout.layoutParams = it
                            }
                        }
                    }
                }

                it.setTag(TAG_IS_LIKED, newLiked)
            }
        }
    }

    //동네주방
    private fun loadPopularKitchenPosts() {
        val token = App.prefs.token ?: return
        val container = findViewById<GridLayout>(R.id.kitchenList)

        RetrofitInstance.apiService.getPopularTradePosts("Bearer $token")
            .enqueue(object : Callback<List<TradePostSimpleResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostSimpleResponse>>,
                    response: Response<List<TradePostSimpleResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val posts = response.body()!!
                        container.removeAllViews()

                        for (post in posts) {
                            val postView = layoutInflater.inflate(R.layout.item_main_trade_post, container, false)

                            val imageView = postView.findViewById<ImageView>(R.id.itemImage)
                            val titleView = postView.findViewById<TextView>(R.id.itemTitle)
                            val priceView = postView.findViewById<TextView>(R.id.itemPrice)

                            val distanceText = postView.findViewById<TextView>(R.id.distanceText)

                            titleView.text = post.title
                            val formatter = DecimalFormat("#,###")
                            priceView.text = if (post.price == 0) {
                                "나눔"
                            } else {
                                "${formatter.format(post.price)} P"
                            }

                            Glide.with(this@MainActivity)
                                .load(post.firstImageUrl)
                                .placeholder(R.drawable.img_kitchen1)
                                .into(imageView)

                            // 숨김 처리
                            distanceText.visibility = View.GONE

                            container.addView(postView)
                        }
                    }
                }

                override fun onFailure(call: Call<List<TradePostSimpleResponse>>, t: Throwable) {
                    Log.e("MainActivity", "인기 거래글 불러오기 실패", t)
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


}