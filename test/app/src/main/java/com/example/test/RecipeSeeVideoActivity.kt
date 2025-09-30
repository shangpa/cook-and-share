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
import com.bumptech.glide.Glide
import com.example.test.model.Ingredient
import com.example.test.model.recipeDetail.CookingStep
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.test.Utils.LikeUtils
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.test.Utils.RecommendUtils
import com.example.test.Utils.TabBarUtils

private lateinit var player: ExoPlayer
private lateinit var playerView: PlayerView
private lateinit var speechRecognizer: SpeechRecognizer

class RecipeSeeVideoActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            Toast.makeText(this, "음성 인식 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see_video)
        val recipeId = intent.getLongExtra("recipeId", -1L)
        val token = App.prefs.token.toString()
        val reviewWriteButton: Button = findViewById(R.id.reviewWriteButton)
        reviewWriteButton.setOnClickListener {
            val intent = Intent(this, ReviewWriteActivity::class.java)
            startActivity(intent)
        }

        TabBarUtils.setupTabBar(this)

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                1
            )
        } else {

        }

        val recipeSee = findViewById<ConstraintLayout>(R.id.recipeSee)
        val indicatorBar = findViewById<View>(R.id.divideRectangleBarTewleve)
        val downArrow = findViewById<ImageButton>(R.id.downArrow)
        val latest = findViewById<TextView>(R.id.latest)


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

        // 하트버튼 선언
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

        goodButtons.forEach { button ->
            RecommendUtils.setupRecommendButton(button, recipeId)
        }

        // 공유 버튼
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
        RetrofitInstance.apiService.getRecipeById("Bearer $token", recipeId)
            .enqueue(object : Callback<RecipeDetailResponse> {
                override fun onResponse(call: Call<RecipeDetailResponse>, response: Response<RecipeDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val recipe = response.body()!!
                        val gson = Gson()

                        val fullVideoUrl = RetrofitInstance.BASE_URL + (recipe.videoUrl?.trim() ?: "")
                        initSpeechRecognizer(fullVideoUrl)
                        val imageView = findViewById<ImageView>(R.id.image)
                        val imageUrl = recipe.mainImageUrl?.trim()
                        if (imageUrl.isNullOrBlank()) {
                            // mainImageUrl이 없으면 → 영상 첫 프레임 썸네일
                            Glide.with(this@RecipeSeeVideoActivity)
                                .load(fullVideoUrl)
                                .frame(0) // 1초 지점 프레임, 필요시 0으로 변경 가능
                                .into(imageView)
                        } else {
                            // mainImageUrl이 있으면 → 이미지 썸네일 사용
                            Glide.with(this@RecipeSeeVideoActivity)
                                .load(RetrofitInstance.BASE_URL + imageUrl)
                                .into(imageView)
                        }


                        // 동영상
                        val playButton = findViewById<ImageButton>(R.id.btnVideo)
                        val playerView = findViewById<PlayerView>(R.id.videoPlayerView)

                        // 버튼 클릭 시 영상 재생
                        playButton.setOnClickListener {
                            initializePlayer(fullVideoUrl)
                            playerView.visibility = View.VISIBLE
                            findViewById<ImageView>(R.id.image).alpha = 0f
                            playButton.visibility = View.GONE

                            // ✅ 이 시점에 음성 인식 시작
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                            }
                            speechRecognizer.startListening(intent)

                            // Retrofit 성공 콜백 안에서 ↓↓ 이걸로 교체
                            initSpeechRecognizer(fullVideoUrl)


                        }


                        // 작성자, 제목, 카테고리, 난이도, 시간, 태그
                        findViewById<TextView>(R.id.saltShow).text = recipe.writer
                        findViewById<TextView>(R.id.vegetarianDietName).text = recipe.title
                        findViewById<TextView>(R.id.vegetarianDiet).text = recipe.category
                        findViewById<TextView>(R.id.elementaryLevel).text = recipe.difficulty
                        findViewById<TextView>(R.id.halfHour).text = "${recipe.cookingTime}분"

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
                            val tagView = TextView(this@RecipeSeeVideoActivity) // ← Activity의 Context 명확히 지정
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
                        val titleText = TextView(this@RecipeSeeVideoActivity).apply {
                            text = "${recipe.title} 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(titleText)

                        val thickDivider = View(this@RecipeSeeVideoActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.dpToPx()
                            ).apply {
                                setMargins(20.dpToPx(), 15.dpToPx(), 20.dpToPx(), 0)
                            }
                            setBackgroundResource(R.drawable.bar_recipe_see)
                        }
                        ingredientContainer.addView(thickDivider)

                        val ingredients = recipe.ingredients

                        ingredients.forEach { ingredient ->
                            val itemLayout = LinearLayout(this@RecipeSeeVideoActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeVideoActivity).apply {
                                text = ingredient.name
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val amountText = TextView(this@RecipeSeeVideoActivity).apply {
                                text = ingredient.amount.toString()
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(amountText)
                            ingredientContainer.addView(itemLayout)

                            // 얇은 구분선
                            val thinDivider = View(this@RecipeSeeVideoActivity).apply {
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
                        ingredientContainer.addView(View(this@RecipeSeeVideoActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                16.dpToPx()
                            )
                        })
                        //대체 재료탭
                        val altTitleText = TextView(this@RecipeSeeVideoActivity).apply {
                            text = "대체 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(altTitleText)

                        val altTitleDivider = View(this@RecipeSeeVideoActivity).apply {
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
                            val itemLayout = LinearLayout(this@RecipeSeeVideoActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeVideoActivity).apply {
                                text = "${ingredient.name}"
                                textSize = 13f
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val amountText = TextView(this@RecipeSeeVideoActivity).apply {
                                text = ingredient.amount
                                textSize = 13f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(amountText)
                            ingredientContainer.addView(itemLayout)

                            val thinDivider = View(this@RecipeSeeVideoActivity).apply {
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
                        ingredientContainer.addView(View(this@RecipeSeeVideoActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                16.dpToPx()
                            )
                        })
                        //재료 처리 방법 탭
                        val handlingTitleText = TextView(this@RecipeSeeVideoActivity).apply {
                            text = "재료 처리 방법"
                            textSize = 15f
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(handlingTitleText)

                        val handlingTitleDivider = View(this@RecipeSeeVideoActivity).apply {
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

                            val itemLayout = LinearLayout(this@RecipeSeeVideoActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeVideoActivity).apply {
                                text = "$name"
                                textSize = 13f
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val handlingText = TextView(this@RecipeSeeVideoActivity).apply {
                                text = handling
                                textSize = 13f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(handlingText)
                            ingredientContainer.addView(itemLayout)

                            val thinDivider = View(this@RecipeSeeVideoActivity).apply {
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


                        // 조리 순서
                        val stepContainer = findViewById<LinearLayout>(R.id.stepContainer)
                        stepContainer.removeAllViews()

                        val steps = gson.fromJson<List<CookingStep>>(
                            recipe.cookingSteps, object : TypeToken<List<CookingStep>>() {}.type
                        ).map { step ->
                            step.copy(mediaUrl = RetrofitInstance.BASE_URL + step.mediaUrl.trim())
                        }
                        steps.forEach { step ->
                            // STEP 타이틀
                            val stepTitle = TextView(this@RecipeSeeVideoActivity).apply {
                                text = "STEP ${step.step}"
                                textSize = 14f
                                setTextColor(Color.BLACK)
                                setPadding(0, 12.dpToPx(), 0, 4.dpToPx())
                                setTypeface(null, Typeface.BOLD)
                            }

                            // 설명
                            val description = TextView(this@RecipeSeeVideoActivity).apply {
                                text = step.description
                                textSize = 13f
                                setTextColor(Color.DKGRAY)
                                setPadding(0, 0, 0, 6.dpToPx())
                            }

                            // 이미지
                            val imageView = ImageView(this@RecipeSeeVideoActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    200.dpToPx()
                                ).apply {
                                    bottomMargin = 12.dpToPx()
                                }
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                visibility = if (step.mediaUrl.isNullOrBlank()) View.GONE else View.VISIBLE
                                if (!step.mediaUrl.isNullOrBlank()) {
                                    Glide.with(this@RecipeSeeVideoActivity).load(step.mediaUrl).into(this)
                                    Log.d("mediaUrl", step.mediaUrl)
                                }
                            }

                            // 구분선
                            val divider = View(this@RecipeSeeVideoActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2.dpToPx()
                                ).apply {
                                    topMargin = 12.dpToPx()
                                }
                                setBackgroundResource(R.drawable.bar_recipe_see_material)
                            }

                            // 전체 묶기
                            stepContainer.addView(stepTitle)
                            stepContainer.addView(description)
                            stepContainer.addView(imageView)
                            stepContainer.addView(divider)
                        }
                    }
                }

                fun Int.dpToPx(): Int {
                    return (this * Resources.getSystem().displayMetrics.density).toInt()
                }

                override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@RecipeSeeVideoActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })

        findViewById<ImageButton>(R.id.backArrow).setOnClickListener {
            finish()
        }

    }
    private fun initializePlayer(videoUrl: String) {
        player = ExoPlayer.Builder(this).build()
        playerView = findViewById<PlayerView>(R.id.videoPlayerView)

        playerView.player = player

        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun initSpeechRecognizer(fullVideoUrl: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "음성 인식을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    for (command in it) {
                        if (command.contains("재생")) {
                            // 플레이어가 아직 준비되지 않았다면 초기화
                            if (!::player.isInitialized) {
                                initializePlayer(fullVideoUrl)
                                findViewById<ImageView>(R.id.image).alpha = 0f
                                findViewById<ImageButton>(R.id.btnVideo).visibility = View.GONE
                                findViewById<PlayerView>(R.id.videoPlayerView).visibility = View.VISIBLE
                            } else {
                                player.play()
                            }
                        } else if (command.contains("정지")) {
                            if (::player.isInitialized) {
                                player.pause()
                            }
                        }
                    }
                }

                // 계속 리스닝
                speechRecognizer.startListening(intent)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

}