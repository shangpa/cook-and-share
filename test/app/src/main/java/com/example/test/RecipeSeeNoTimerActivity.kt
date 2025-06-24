package com.example.test

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.Utils.LikeUtils
import com.example.test.Utils.RecommendUtils
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.ReviewAdapter
import com.example.test.model.Ingredient
import com.example.test.model.recipeDetail.CookingStep
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.model.review.ReviewResponseDTO
import com.example.test.network.RetrofitInstance
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale


class RecipeSeeNoTimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see_no_timer)

        TabBarUtils.setupTabBar(this)

        val recipeId = intent.getLongExtra("recipeId", -1L)
        val selectedTab = intent.getIntExtra("selectedTab", 0) // 기본은 0(재료)
        val reviewWriteButton: Button = findViewById(R.id.reviewWriteButton)
        reviewWriteButton.setOnClickListener {
            val intent = Intent(this, ReviewWriteActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        val recipeSee = findViewById<ConstraintLayout>(R.id.recipeSee)
        val indicatorBar = findViewById<View>(R.id.divideRectangleBarTewleve)
        val downArrow = findViewById<ImageButton>(R.id.downArrow)
        val latest = findViewById<TextView>(R.id.latest)

        //리뷰
        val reviewRecyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
        val reviewAdapter = ReviewAdapter(emptyList())
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.adapter = reviewAdapter


        // 재료, 조리순서, 리뷰 TextView 리스트
        val textViews = listOf(
            findViewById<TextView>(R.id.material),
            findViewById<TextView>(R.id.cookOrder),
            findViewById<TextView>(R.id.review)
        )

        // ConstraintLayout 리스트 (TextView와 1:1 매칭)
        val layouts = listOf(
            findViewById<ConstraintLayout>(R.id.materialTap),
            findViewById<ConstraintLayout>(R.id.cookOrderTap),
            findViewById<ConstraintLayout>(R.id.reviewTap)
        )

        // 재료, 조리순서, 리뷰 TextView 클릭 시 해당 화면으로 이동 & 바 위치 변경
        textViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                // 모든 ConstraintLayout 숨김
                layouts.forEach { it.visibility = View.GONE }

                // 클릭된 TextView에 해당하는 ConstraintLayout만 표시
                layouts[index].visibility = View.VISIBLE

                // 모든 TextView 색상 초기화
                textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }

                // 클릭된 TextView만 색상 변경 (#2B2B2B)
                textView.setTextColor(Color.parseColor("#2B2B2B"))

                // 바(View)의 위치를 클릭한 TextView의 중앙으로 이동
                val targetX = textView.x + (textView.width / 2) - (indicatorBar.width / 2)
                indicatorBar.x = targetX
            }
        }

        val heartButtons = listOf(
            findViewById<ImageButton>(R.id.heartButton)
        )

        heartButtons.forEach { button ->
            LikeUtils.isRecipeLiked(recipeId) { isLiked ->
                button.setImageResource(
                    if (isLiked) R.drawable.ic_heart_fill else R.drawable.ic_recipe_heart
                )
                button.setTag(R.id.heartButton, isLiked)
            }

            LikeUtils.setupLikeButton(button, recipeId)
        }

        // 좋아요 버튼 선언
        val goodButtons = listOf(
            findViewById<ImageButton>(R.id.goodButton)
        )

        // 좋아요 버튼 클릭시 채워진 좋아요로 바뀜
        goodButtons.forEach { button ->
            RecommendUtils.setupRecommendButton(button, recipeId)
        }

        // 공유 버튼 선언
        val shareButtons = listOf(findViewById<ImageButton>(R.id.shareButton))

        shareButtons.forEach { button ->
            button.setOnClickListener {
                val recipeId = intent.getLongExtra("recipeId", -1L)

                if (recipeId != -1L) {
                    val link = "https://shangpa.github.io/open.html?id=$recipeId"

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "레시피 공유")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "레시피를 공유했어요!\n👇 아래 링크를 눌러서 바로 확인해보세요!\n$link"
                        )
                    }

                    val chooser = Intent.createChooser(shareIntent, "공유할 앱을 선택하세요")
                    startActivity(chooser)
                } else {
                    Toast.makeText(this, "레시피 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }


        //리뷰 드롭다운 버튼 클릭
        downArrow.setOnClickListener {
            val popup = PopupMenu(this, downArrow)
            val items = listOf("최신순", "인기순", "추천순")

            items.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                latest.text = item.title // 선택된 텍스트 적용!
                true
            }

            popup.show()
        }

        // 레시피 조회 기능 추가

        val token = App.prefs.token.toString()

        RetrofitInstance.apiService.getRecipeById("Bearer $token", recipeId)
            .enqueue(object : Callback<RecipeDetailResponse> {
                override fun onResponse(call: Call<RecipeDetailResponse>, response: Response<RecipeDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val recipe = response.body()!!
                        val gson = Gson()

                        // 작성자, 제목, 카테고리, 난이도, 시간, 태그
                        findViewById<TextView>(R.id.saltShow).text = recipe.writer
                        findViewById<TextView>(R.id.vegetarianDietName).text = recipe.title
                        findViewById<TextView>(R.id.vegetarianDiet).text = recipe.category
                        findViewById<TextView>(R.id.elementaryLevel).text = recipe.difficulty
                        findViewById<TextView>(R.id.halfHour).text = "${recipe.cookingTime}분"
                        val mainImageView = findViewById<ImageView>(R.id.riceImage)

                        val imageUrl = recipe.mainImageUrl?.let {
                            if (it.startsWith("http")) it
                            else RetrofitInstance.BASE_URL + it
                        }
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this@RecipeSeeNoTimerActivity)
                                .load(imageUrl)
                                .into(mainImageView)
                        }
                        //날짜
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
                        try {
                            val parsedDate = inputFormat.parse(recipe.createdAt)
                            val formattedDate = outputFormat.format(parsedDate!!)
                            findViewById<TextView>(R.id.date).text = formattedDate // 예: 03.23
                        } catch (e: Exception) {
                            findViewById<TextView>(R.id.date).text = "-" // 에러 발생 시 예외 처리
                        }
                        val ingredientContainer = findViewById<LinearLayout>(R.id.ingredientContainer)
                        ingredientContainer.removeAllViews()

                        //태그
                        val tagContainer = findViewById<FlexboxLayout>(R.id.tagContainer)
                        tagContainer.removeAllViews()

                        val tagList = recipe.tags.split(",").map { it.trim() }

                        tagList.forEach { tag ->
                            val tagView = TextView(this@RecipeSeeNoTimerActivity) // ← Activity의 Context 명확히 지정
                                .apply {
                                    text = "# $tag"
                                    textSize = 10f
                                    setTextColor(Color.parseColor("#747474"))
                                    setBackgroundResource(R.drawable.ic_step_recipe_see_main_rect)
                                    setPadding(20, 4, 20, 4) // 태그 내부 여백
                                    layoutParams = FlexboxLayout.LayoutParams(
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(6.dpToPx(), 6.dpToPx(), 6.dpToPx(), 6.dpToPx())
                                    }
                                }
                            tagContainer.addView(tagView)
                        }

                        // 재료 탭
                        val titleText = TextView(this@RecipeSeeNoTimerActivity).apply {
                            text = "${recipe.title} 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(titleText)

                        val thickDivider = View(this@RecipeSeeNoTimerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.dpToPx()
                            ).apply {
                                setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                            }
                            setBackgroundResource(R.drawable.bar_recipe_see)
                        }
                        ingredientContainer.addView(thickDivider)

                        val ingredients = gson.fromJson<List<Ingredient>>(
                            recipe.ingredients, object : TypeToken<List<Ingredient>>() {}.type
                        )

                        ingredients.forEach { ingredient ->
                            val itemLayout = LinearLayout(this@RecipeSeeNoTimerActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = ingredient.name
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val amountText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = ingredient.amount
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(amountText)
                            ingredientContainer.addView(itemLayout)

                            // 얇은 구분선
                            val thinDivider = View(this@RecipeSeeNoTimerActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2.dpToPx()
                                ).apply {
                                    setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                                }
                                setBackgroundResource(R.drawable.bar_recipe_see_material)
                            }
                            ingredientContainer.addView(thinDivider)
                        }
                        // 재료 끝난 후 여백
                        ingredientContainer.addView(View(this@RecipeSeeNoTimerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                16.dpToPx()
                            )
                        })
                        //대체 재료탭
                        val altTitleText = TextView(this@RecipeSeeNoTimerActivity).apply {
                            text = "대체 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(altTitleText)

                        val altTitleDivider = View(this@RecipeSeeNoTimerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.dpToPx()
                            ).apply {
                                setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                            }
                            setBackgroundResource(R.drawable.bar_recipe_see)
                        }
                        ingredientContainer.addView(altTitleDivider)

                        // 대체 재료 표시
                        val alternativeIngredients = gson.fromJson<List<Ingredient>>(
                            recipe.alternativeIngredients, object : TypeToken<List<Ingredient>>() {}.type
                        )

                        alternativeIngredients.forEach { ingredient ->
                            val itemLayout = LinearLayout(this@RecipeSeeNoTimerActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = "${ingredient.name}"
                                textSize = 13f
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val amountText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = ingredient.amount
                                textSize = 13f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(amountText)
                            ingredientContainer.addView(itemLayout)

                            val thinDivider = View(this@RecipeSeeNoTimerActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2.dpToPx()
                                ).apply {
                                    setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                                }
                                setBackgroundResource(R.drawable.bar_recipe_see_material)
                            }
                            ingredientContainer.addView(thinDivider)
                        }
                        // 대체 재료 끝난 후 여백
                        ingredientContainer.addView(View(this@RecipeSeeNoTimerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                16.dpToPx()
                            )
                        })
                        //재료 처리 방법 탭
                        val handlingTitleText = TextView(this@RecipeSeeNoTimerActivity).apply {
                            text = "재료 처리 방법"
                            textSize = 15f
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(handlingTitleText)

                        val handlingTitleDivider = View(this@RecipeSeeNoTimerActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.dpToPx()
                            ).apply {
                                setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                            }
                            setBackgroundResource(R.drawable.bar_recipe_see)
                        }
                        ingredientContainer.addView(handlingTitleDivider)

                        val handlingMethods = gson.fromJson<List<String>>(
                            recipe.handlingMethods, object : TypeToken<List<String>>() {}.type
                        )

                        handlingMethods.forEach { method ->
                            val parts = method.split(" : ")
                            val name = parts.getOrNull(0) ?: ""
                            val handling = parts.getOrNull(1) ?: ""

                            val itemLayout = LinearLayout(this@RecipeSeeNoTimerActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = "$name"
                                textSize = 13f
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val handlingText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = handling
                                textSize = 13f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(handlingText)
                            ingredientContainer.addView(itemLayout)

                            val thinDivider = View(this@RecipeSeeNoTimerActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2.dpToPx()
                                ).apply {
                                    setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                                }
                                setBackgroundResource(R.drawable.bar_recipe_see_material)
                            }
                            ingredientContainer.addView(thinDivider)

                        }

                        //조리순서
                        val stepContainer = findViewById<LinearLayout>(R.id.stepContainer)
                        stepContainer.removeAllViews()

                        val steps = gson.fromJson<List<CookingStep>>(
                            recipe.cookingSteps, object : TypeToken<List<CookingStep>>() {}.type
                        ).map { step ->
                            val trimmedUrl = step.mediaUrl?.trim()
                            if (trimmedUrl.isNullOrEmpty()) {
                                step.copy(mediaUrl = "")  // ✅ null 대신 빈 문자열
                            } else {
                                step.copy(mediaUrl = RetrofitInstance.BASE_URL + trimmedUrl)
                            }
                        }

                        steps.forEach { step ->
                            // STEP 타이틀
                            val stepTitle = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = "STEP ${step.step}"
                                textSize = 14f
                                setTextColor(Color.BLACK)
                                setPadding(0, 12.dpToPx(), 0, 4.dpToPx())
                                setTypeface(null, Typeface.BOLD)
                            }

                            // 설명 (이미지 없으면 paddingBottom 20dp, 있으면 6dp)
                            val description = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = step.description
                                textSize = 13f
                                setTextColor(Color.DKGRAY)
                                setPadding(0, 0, 0, if (step.mediaUrl.isNullOrBlank()) 20.dpToPx() else 6.dpToPx())
                            }

                            // 구분선
                            val divider = View(this@RecipeSeeNoTimerActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2.dpToPx()
                                ).apply {
                                    topMargin = 12.dpToPx()
                                }
                                setBackgroundResource(R.drawable.bar_recipe_see_material)
                            }

                            // 추가
                            stepContainer.addView(stepTitle)
                            stepContainer.addView(description)

                            // 이미지 있을 때만 생성 및 추가
                            if (!step.mediaUrl.isNullOrBlank()) {
                                val imageView = ImageView(this@RecipeSeeNoTimerActivity).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        200.dpToPx()
                                    ).apply {
                                        bottomMargin = 12.dpToPx()
                                    }
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                                Glide.with(this@RecipeSeeNoTimerActivity).load(step.mediaUrl).into(imageView)
                                Log.d("mediaUrl", step.mediaUrl)
                                stepContainer.addView(imageView)
                            }

                            stepContainer.addView(divider)
                        }
                    }
                }

                fun Int.dpToPx(): Int {
                    return (this * Resources.getSystem().displayMetrics.density).toInt()
                }

                override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@RecipeSeeNoTimerActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
        //리뷰
        RetrofitInstance.apiService.getReviews("Bearer $token", recipeId).enqueue(object : Callback<List<ReviewResponseDTO>> {
            override fun onResponse(call: Call<List<ReviewResponseDTO>>, response: Response<List<ReviewResponseDTO>>) {
                Log.d("리뷰응답", "성공 여부: ${response.isSuccessful}")
                Log.d("리뷰응답", "내용: ${response.body()}")
                if (response.isSuccessful && response.body() != null) {
                    val reviews = response.body()!!
                    Log.d("리뷰응답", "리뷰 개수: ${reviews.size}")
                    reviews.forEach {
                        Log.d("리뷰내용", "작성자=${it.username}, 평점=${it.rating}, 내용=${it.content}")
                    }
                    reviewAdapter.updateData(reviews)
                }
            }

            override fun onFailure(call: Call<List<ReviewResponseDTO>>, t: Throwable) {
                Toast.makeText(this@RecipeSeeNoTimerActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }

    }

}