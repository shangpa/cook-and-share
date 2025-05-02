package com.example.test

import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView

lateinit var binding: ActivityMainBinding
private var currentPage = 0
private lateinit var sliderHandler: Handler
private lateinit var sliderRunnable: Runnable
private lateinit var bannerPagerAdapter: BannerPagerAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}