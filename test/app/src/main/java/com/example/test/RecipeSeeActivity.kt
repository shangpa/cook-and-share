package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.test.Utils.LikeUtils
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

private lateinit var steps: List<View>
private var currentStep = 0

class RecipeSeeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see)
        val recipeId = intent.getLongExtra("recipeId", -1L)
        val endFixButton: Button = findViewById(R.id.endFixButton)
        endFixButton.setOnClickListener {
            val intent = Intent(this, ReveiwWriteActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        // 리뷰 작성하기 선언
        val peopleChoice = findViewById<ConstraintLayout>(R.id.peopleChoice)
        val zero = findViewById<EditText>(R.id.zero)
        val recipeSeeMain = findViewById<ConstraintLayout>(R.id.recipeSeeMain)
        val tapBar = findViewById<ConstraintLayout>(R.id.tapBar)
        val shareButton = findViewById<ImageButton>(R.id.shareButton)
        val cookButton = findViewById<Button>(R.id.cookButton)
        val nextFixButton = findViewById<Button>(R.id.nextFixButton)
        val voice = findViewById<ImageButton>(R.id.voice)
        val voiceBubble = findViewById<View>(R.id.voiceBubble)
        val beforeStep = findViewById<TextView>(R.id.beforeStep)
        val before = findViewById<TextView>(R.id.before)

        // 다음으로 버튼 선언
        steps = listOf(
            findViewById(R.id.recipeSeeMain),
            findViewById(R.id.recipeSeeOne),
        )

        // 다음으로 버튼 클릭시 다음 화면으로 이동
        nextFixButton.setOnClickListener {
            if (!::steps.isInitialized || steps.isEmpty()) return@setOnClickListener

            if (currentStep < steps.size - 1) {
                steps[currentStep].visibility = View.GONE
                currentStep++

                Log.d("NEXT_STEP", "currentStep: $currentStep, view id: ${steps[currentStep].id}")

                // 재료화면이면 stepContainer 숨기고, 조리 순서 시작되면 보여주기
                if (steps[currentStep].id == R.id.recipeSeeMain) {
                    findViewById<LinearLayout>(R.id.stepContainer).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.recipeSeeMain).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.recipeSeeOne).visibility = View.GONE
                } else {
                    findViewById<LinearLayout>(R.id.stepContainer).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.recipeSeeMain).visibility = View.GONE
                    findViewById<ConstraintLayout>(R.id.recipeSeeOne).visibility = View.VISIBLE
                }

                steps[currentStep].visibility = View.VISIBLE
            }
        }

        // 조리하기 버튼 클릭시 상자 보이기
        cookButton.setOnClickListener {
            peopleChoice.visibility = View.GONE
            recipeSeeMain.visibility = View.VISIBLE
            tapBar.visibility = View.VISIBLE
            steps[currentStep].visibility = View.VISIBLE
        }

        // 하트버튼 선언
        val heartButtons = listOf(
            findViewById<ImageButton>(R.id.heartButton),
            findViewById(R.id.heartButtonTwo),
        )

        // 각 버튼에 대해 좋아요 기능 설정
        heartButtons.forEach { button ->
            // 초기 상태를 false로 태그 저장
            button.setTag(R.id.heartButton, false)
            // 서버 연동 + 토글 UI
            LikeUtils.setupLikeButton(button, recipeId)
        }


        // 좋아요 버튼 선언
        val goodButtons = listOf(
            findViewById<ImageButton>(R.id.goodButton),
            findViewById(R.id.goodButtonTwo),
        )

        // 좋아요 버튼 클릭시 채워진 좋아요로 바뀜
        goodButtons.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.goodButton, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.goodButton) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_good)
                } else {
                    button.setImageResource(R.drawable.ic_good_fill)
                    Toast.makeText(this, "해당 레시피를 추천하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.goodButton, !isLiked)
            }
        }

        // 공유 버튼 선언
        val shareButtons = listOf(
            findViewById<ImageButton>(R.id.shareButton),
            findViewById(R.id.shareButtonTwo),
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 텍스트 공유
            putExtra(Intent.EXTRA_SUBJECT, "레시피 공유") // 제목 (선택)
            putExtra(Intent.EXTRA_TEXT, "링크를 공유했어요!\n" + "어떤 링크인지 들어가서 확인해볼까요?!\nhttps://your-recipe-link.com") // 공유할 내용
        }

        val chooser = Intent.createChooser(shareIntent, "공유할 앱을 선택하세요")

        shareButtons.forEach { button ->
            button.setOnClickListener {
                startActivity(chooser)
            }
        }

        // 음성 버튼 클릭시 상자 보이기
        voice.setOnClickListener {
            if (voiceBubble.visibility == View.VISIBLE) {
                voiceBubble.visibility = View.GONE
                beforeStep.visibility = View.GONE
                before.visibility = View.GONE
            } else {
                voiceBubble.visibility = View.VISIBLE
                beforeStep.visibility = View.VISIBLE
                before.visibility = View.VISIBLE
            }
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
                            val tagView = TextView(this@RecipeSeeActivity) // ← Activity의 Context 명확히 지정
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
                        val titleText = TextView(this@RecipeSeeActivity).apply {
                            text = "${recipe.title} 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(titleText)

                        val thickDivider = View(this@RecipeSeeActivity).apply {
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
                            val itemLayout = LinearLayout(this@RecipeSeeActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeActivity).apply {
                                text = ingredient.name
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val amountText = TextView(this@RecipeSeeActivity).apply {
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
                            val thinDivider = View(this@RecipeSeeActivity).apply {
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
                        ingredientContainer.addView(View(this@RecipeSeeActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                16.dpToPx()
                            )
                        })
                        //대체 재료탭
                        val altTitleText = TextView(this@RecipeSeeActivity).apply {
                            text = "대체 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(altTitleText)

                        val altTitleDivider = View(this@RecipeSeeActivity).apply {
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
                            val itemLayout = LinearLayout(this@RecipeSeeActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeActivity).apply {
                                text = "${ingredient.name}"
                                textSize = 13f
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val amountText = TextView(this@RecipeSeeActivity).apply {
                                text = ingredient.amount
                                textSize = 13f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(amountText)
                            ingredientContainer.addView(itemLayout)

                            val thinDivider = View(this@RecipeSeeActivity).apply {
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
                        ingredientContainer.addView(View(this@RecipeSeeActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                16.dpToPx()
                            )
                        })
                        //재료 처리 방법 탭
                        val handlingTitleText = TextView(this@RecipeSeeActivity).apply {
                            text = "재료 처리 방법"
                            textSize = 15f
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(handlingTitleText)

                        val handlingTitleDivider = View(this@RecipeSeeActivity).apply {
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

                            val itemLayout = LinearLayout(this@RecipeSeeActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20.dpToPx(), 11.dpToPx(), 20.dpToPx(), 0)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeActivity).apply {
                                text = "$name"
                                textSize = 13f
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            val handlingText = TextView(this@RecipeSeeActivity).apply {
                                text = handling
                                textSize = 13f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(handlingText)
                            ingredientContainer.addView(itemLayout)

                            val thinDivider = View(this@RecipeSeeActivity).apply {
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
                        val inflater = layoutInflater
                        val stepViewList = mutableListOf<View>()

                        // 1. 조리순서 JSON 파싱
                        val stepsFromJson = gson.fromJson<List<CookingStep>>(
                            recipe.cookingSteps, object : TypeToken<List<CookingStep>>() {}.type
                        ).map { step ->
                            val cleanedUrl = step.mediaUrl.trim()
                            val fullUrl = if (cleanedUrl.isNotEmpty()) RetrofitInstance.BASE_URL + cleanedUrl else ""
                            step.copy(mediaUrl = fullUrl)
                        }

                        // 3. 각 조리 순서 View 동적 생성
                        stepsFromJson.forEachIndexed { index, step ->
                            val stepView = inflater.inflate(R.layout.recipe_see_step_item, stepContainer, false)
                            // 제목 & 설명
                            stepView.findViewById<TextView>(R.id.cookNameFive).text = recipe.title
                            stepView.findViewById<TextView>(R.id.stepOne).text = "STEP ${index + 1}"
                            stepView.findViewById<TextView>(R.id.explainOne).text = step.description
                            Log.d("STEP_CHECK", "Step ${index + 1} desc: ${step.description}, image: ${step.mediaUrl}")
                            val imageView = stepView.findViewById<ImageView>(R.id.imageTwo)
                            if (step.mediaUrl.isNullOrBlank()) {
                                imageView.visibility = View.GONE
                            } else {
                                imageView.visibility = View.VISIBLE
                                Glide.with(this@RecipeSeeActivity)
                                    .load(step.mediaUrl)
                                    .into(imageView)
                            }

                            // 타이머 표시
                            if (step.timeInSeconds > 0) {
                                val min = step.timeInSeconds / 60
                                val sec = step.timeInSeconds % 60
                                stepView.findViewById<TextView>(R.id.hour).text = String.format("%02d", min)
                                stepView.findViewById<TextView>(R.id.minute).text = String.format("%02d", sec)
                                // 타이머 동작 추가
                                val hourText = stepView.findViewById<TextView>(R.id.hour)
                                val minuteText = stepView.findViewById<TextView>(R.id.minute)
                                val startButton = stepView.findViewById<Button>(R.id.start)
                                val stopButton = stepView.findViewById<Button>(R.id.stop)

                                var timeLeft = step.timeInSeconds * 1000L
                                var timer: CountDownTimer? = null
                                var isTimerRunning = false

                                startButton.setOnClickListener {
                                    if (!isTimerRunning) {
                                        timer = object : CountDownTimer(timeLeft, 1000) {
                                            override fun onTick(millisUntilFinished: Long) {
                                                timeLeft = millisUntilFinished
                                                val minutes = (millisUntilFinished / 1000) / 60
                                                val seconds = (millisUntilFinished / 1000) % 60
                                                hourText.text = String.format("%02d", minutes)
                                                minuteText.text = String.format("%02d", seconds)
                                            }

                                            override fun onFinish() {
                                                isTimerRunning = false
                                                Toast.makeText(this@RecipeSeeActivity, "타이머 종료!", Toast.LENGTH_SHORT).show()
                                            }
                                        }.start()
                                        isTimerRunning = true
                                    }
                                }

                                stopButton.setOnClickListener {
                                    timer?.cancel()
                                    isTimerRunning = false
                                }
                            } else {
                                stepView.findViewById<View>(R.id.timerBar).visibility = View.GONE
                                stepView.findViewById<TextView>(R.id.hour).visibility = View.GONE
                                stepView.findViewById<TextView>(R.id.colon).visibility = View.GONE
                                stepView.findViewById<TextView>(R.id.minute).visibility = View.GONE
                                stepView.findViewById<Button>(R.id.start).visibility = View.GONE
                                stepView.findViewById<Button>(R.id.stop).visibility = View.GONE

                                // 설명의 아래 마진이 충분한지 확인하거나 추가
                                val explainOne = stepView.findViewById<TextView>(R.id.explainOne)
                                val params = explainOne.layoutParams as ConstraintLayout.LayoutParams
                                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                                params.bottomMargin = 40.dpToPx()
                                explainOne.layoutParams = params

                            }

                            stepView.visibility = View.GONE
                            stepContainer.addView(stepView)
                            stepViewList.add(stepView)
                        }

                        // 4. steps 리스트 초기화 (재료 화면 + 조리 순서 화면들)
                        steps = listOf<View>(
                            findViewById(R.id.recipeSeeMain)
                        ) + stepViewList
                        currentStep = 0

                        //음성
                        stepsFromJson.forEachIndexed { index, step ->
                            val stepView = inflater.inflate(R.layout.recipe_see_step_item, stepContainer, false)

                            // 기존 코드들
                            stepView.findViewById<TextView>(R.id.stepOne).text = "STEP ${index + 1}"
                            stepView.findViewById<TextView>(R.id.explainOne).text = step.description

                            // 음성 버튼 및 말풍선 toggle
                            val voiceBtn = stepView.findViewById<ImageButton>(R.id.voice)
                            val voiceBubble = stepView.findViewById<View>(R.id.voiceBubble)

                            voiceBtn.setOnClickListener {
                                voiceBubble.visibility = if (voiceBubble.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                            }

                            // 이전 스텝으로 가기
                            stepView.findViewById<TextView>(R.id.before).setOnClickListener {
                                if (currentStep > 0) {
                                    steps[currentStep].visibility = View.GONE
                                    currentStep--
                                    steps[currentStep].visibility = View.VISIBLE
                                }
                            }

                            // 다음 스텝으로 가기
                            stepView.findViewById<TextView>(R.id.next).setOnClickListener {
                                if (currentStep < steps.size - 1) {
                                    steps[currentStep].visibility = View.GONE
                                    currentStep++
                                    steps[currentStep].visibility = View.VISIBLE
                                }
                            }

                            // step 추가
                            stepView.visibility = View.GONE
                            stepContainer.addView(stepView)
                            stepViewList.add(stepView)
                        }
                    }
                }
                // dp 변환 함수 추가
                fun Int.dpToPx(): Int {
                    return (this * Resources.getSystem().displayMetrics.density).toInt()
                }
                override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@RecipeSeeActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })

    }
}