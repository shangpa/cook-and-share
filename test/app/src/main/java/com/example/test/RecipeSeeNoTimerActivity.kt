package com.example.test

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.model.Ingredient
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecipeSeeNoTimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see_no_timer)

        val reviewWriteButton: Button = findViewById(R.id.reviewWriteButton)
        reviewWriteButton.setOnClickListener {
            val intent = Intent(this, ReveiwWriteActivity::class.java)
            startActivity(intent)
        }

        val peopleChoice = findViewById<ConstraintLayout>(R.id.peopleChoice)
        val zero = findViewById<EditText>(R.id.zero)
        val nextFixButton = findViewById<Button>(R.id.nextFixButton)
        val recipeSee = findViewById<ConstraintLayout>(R.id.recipeSee)
        val indicatorBar = findViewById<View>(R.id.divideRectangleBarTewleve)
        val downArrow = findViewById<ImageButton>(R.id.downArrow)
        val latest = findViewById<TextView>(R.id.latest)

        // 조리하기 버튼 클릭시 상자 보이기
        nextFixButton.setOnClickListener {
            peopleChoice.visibility = View.GONE
            recipeSee.visibility = View.VISIBLE
        }

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
            findViewById<ImageButton>(R.id.goodButton)
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
        val shareButtons = listOf(findViewById<ImageButton>(R.id.shareButton))

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
        val recipeId = 46L // 테스트용
        val token = App.prefs.token.toString()

        RetrofitInstance.apiService.getRecipeById("Bearer $token", recipeId)
            .enqueue(object : Callback<RecipeDetailResponse> {
                override fun onResponse(call: Call<RecipeDetailResponse>, response: Response<RecipeDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val recipe = response.body()!!
                        val gson = Gson()

                        // 📌 이 아래에 추가해줘!
                        val ingredientContainer = findViewById<LinearLayout>(R.id.ingredientContainer)

                        val ingredients = gson.fromJson<List<Ingredient>>(
                            recipe.ingredients, object : TypeToken<List<Ingredient>>() {}.type
                        )

                        ingredientContainer.removeAllViews()

                        ingredients.forEach { ingredient ->
                            val itemLayout = LinearLayout(this@RecipeSeeNoTimerActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(20, 10, 20, 10)
                                }
                            }

                            val nameText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = ingredient.name
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                            }

                            val amountText = TextView(this@RecipeSeeNoTimerActivity).apply {
                                text = ingredient.amount
                                textSize = 13f
                                setTextColor(Color.parseColor("#2B2B2B"))
                                setPadding(100, 0, 0, 0)
                            }

                            itemLayout.addView(nameText)
                            itemLayout.addView(amountText)
                            ingredientContainer.addView(itemLayout)
                        }
                    }
                }
                override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@RecipeSeeNoTimerActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })


    }

}