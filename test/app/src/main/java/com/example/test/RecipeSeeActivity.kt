package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.model.Ingredient
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

        val endFixButton: Button = findViewById(R.id.endFixButton)
        endFixButton.setOnClickListener {
            val intent = Intent(this, ReveiwWriteActivity::class.java)
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

        // 다음으로 버튼 선언
        steps = listOf(
            findViewById(R.id.recipeSeeMain),
            findViewById(R.id.recipeSeeOne),
        )

        // 다음으로 버튼 클릭시 다음 화면으로 이동
        nextFixButton.setOnClickListener {
            if (currentStep < steps.size - 1) {
                steps[currentStep].visibility = View.GONE
                currentStep++
                steps[currentStep].visibility = View.VISIBLE
            }
        }

        // 조리하기 버튼 클릭시 상자 보이기
        cookButton.setOnClickListener {
            peopleChoice.visibility = View.GONE
            recipeSeeMain.visibility = View.VISIBLE
            tapBar.visibility = View.VISIBLE
        }

        // 하트버튼 선언
        val heartButtons = listOf(
            findViewById<ImageButton>(R.id.heartButton),
            findViewById(R.id.heartButtonTwo),
        )

        // 하트버튼 클릭시 채워진 하트로 바뀜
        heartButtons.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.heartButton, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.heartButton) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_recipe_heart)
                } else {
                    button.setImageResource(R.drawable.ic_heart_fill)
                    Toast.makeText(this, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.heartButton, !isLiked)
            }
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

        // val recipeId = intent.getLongExtra("RECIPE_ID", -1L)
        // 레시피 조회 기능 추가
        val recipeId=77L // 테스트 하드코딩

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

                        // 1. 레시피 제목 + " 재료" 텍스트
                        val titleText = TextView(this@RecipeSeeActivity).apply {
                            text = "${recipe.title} 재료"
                            textSize = 15f
                            setTextColor(Color.parseColor("#2B2B2B"))
                            setPadding(20.dpToPx(), 10.dpToPx(), 0, 0)
                        }
                        ingredientContainer.addView(titleText)

                        // 2. 굵은 바
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

                        // 3. 재료 반복
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

                            // 얇은 바
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