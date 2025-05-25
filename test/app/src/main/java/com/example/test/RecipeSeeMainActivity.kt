package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.ExpectedIngredientAdapter
import com.example.test.model.recipeDetail.ExpectedIngredient
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeSeeMainActivity : AppCompatActivity() {

    var adapter: ExpectedIngredientAdapter? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see_main)

        TabBarUtils.setupTabBar(this)

        val data: Uri? = intent?.data
        val recipeId: Long = data?.lastPathSegment?.toLongOrNull()
            ?: intent.getLongExtra("recipeId", -1L)

        Log.d("RecipeSeeMain", "받은 recipeId: $recipeId")
        // 유효하지 않은 경우 종료
        if (recipeId == -1L) {
            Toast.makeText(this, "레시피 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val viewWithTimer: TextView = findViewById(R.id.viewWithTimer)
        val rigthArrow: ImageButton = findViewById(R.id.rigthArrow)
        val viewWithoutTimer: TextView = findViewById(R.id.viewWithoutTimer)
        val rigthArrowTwo: ImageButton = findViewById(R.id.rigthArrowTwo)
        val videoSee: TextView = findViewById(R.id.videoSee)
        val rigthArrowThree: ImageButton = findViewById(R.id.rigthArrowThree)


        var nextActivityIntent: Intent? = null

        val viewToActivityMap = mapOf(
            viewWithTimer to RecipeSeeActivity::class.java,
            rigthArrow to RecipeSeeActivity::class.java,
            viewWithoutTimer to RecipeSeeNoTimerActivity::class.java,
            rigthArrowTwo to RecipeSeeNoTimerActivity::class.java,
            videoSee to RecipeSeeVideoActivity::class.java,
            rigthArrowThree to RecipeSeeVideoActivity::class.java
        )

        viewToActivityMap.forEach { (view, activityClass) ->
            view.setOnClickListener {
                nextActivityIntent = Intent(this, activityClass).apply {
                    putExtra("recipeId", recipeId)
                }
                showMaterialUseBox()
            }
        }

        val yes: Button = findViewById(R.id.yes)
        val no = findViewById<Button>(R.id.no)

        yes.setOnClickListener {
            adapter?.let { ingredientAdapter ->
                val usedList = ingredientAdapter.getUsedIngredients()
                val token = App.prefs.token.toString()

                RetrofitInstance.apiService.useIngredients("Bearer $token", usedList)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Log.d("RecipeSeeMain", "재료 차감 성공")
                                hideMaterialUseBox()
                                nextActivityIntent?.let { startActivity(it) }
                            } else {
                                Log.e("RecipeSeeMain", "재료 차감 실패: ${response.code()}")
                                Toast.makeText(this@RecipeSeeMainActivity, "재료 차감 실패", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("RecipeSeeMain", "차감 API 호출 실패: ${t.message}")
                            Toast.makeText(this@RecipeSeeMainActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        no.setOnClickListener {
            hideMaterialUseBox()
            nextActivityIntent?.let { startActivity(it) }
        }

        val recipeVideoBar = findViewById<View>(R.id.divideRectangleBarFour)

        //레시피 메인 선언
        val manageButton = findViewById<Button>(R.id.manageButton)
        val dimView = findViewById<View>(R.id.dimView)
        val materialUseBox = findViewById<View>(R.id.materialUseBox)
        val materialUse = findViewById<View>(R.id.materialUse)

        val rigthArrowFour = findViewById<ImageButton>(R.id.rigthArrowFour)
        val onePerson = findViewById<TextView>(R.id.onePerson)

        // 냉장고 재료 관리하러 가기 버튼
        manageButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }


        val token = App.prefs.token.toString()

        if (recipeId != -1L && token.isNotEmpty()) {
            RetrofitInstance.apiService.getRecipeById("Bearer $token", recipeId)
                .enqueue(object : Callback<RecipeDetailResponse> {
                    override fun onResponse(call: Call<RecipeDetailResponse>, response: Response<RecipeDetailResponse>) {
                        if (response.isSuccessful) {
                            val recipe = response.body()
                            val recipeTitleTextView = findViewById<TextView>(R.id.cookname)
                            recipeTitleTextView.text = recipe?.title ?: "제목 없음"
                            if (recipe?.cookingSteps.isNullOrBlank() || recipe?.cookingSteps == "[]") {
                                viewWithTimer.visibility = View.GONE
                                rigthArrow.visibility = View.GONE
                                viewWithoutTimer.visibility = View.GONE
                                rigthArrowTwo.visibility = View.GONE
                                Log.d("RecipeSeeMain", "cookingSteps 비어있어서 타이머 보기 숨김 처리됨")
                            }
                            // videoUrl이 null이거나 빈 문자열이면 관련 뷰 숨기기
                            if (recipe?.videoUrl.isNullOrBlank()) {
                                videoSee.visibility = View.GONE
                                rigthArrowThree.visibility = View.GONE
                                recipeVideoBar.visibility = View.GONE
                            }
                        } else {
                            Log.e("RecipeSeeMain", "레시피 조회 실패: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                        Log.e("RecipeSeeMain", "네트워크 오류: ${t.message}")
                    }
                })
        }

        val expectedRecyclerView = findViewById<RecyclerView>(R.id.recyclerExpectedIngredients)
        expectedRecyclerView.layoutManager = LinearLayoutManager(this)

        // 예상 재료 불러오기
        RetrofitInstance.apiService.getExpectedIngredients(recipeId, "Bearer $token")
            .enqueue(object : Callback<List<ExpectedIngredient>> {
                override fun onResponse(
                    call: Call<List<ExpectedIngredient>>,
                    response: Response<List<ExpectedIngredient>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        adapter = ExpectedIngredientAdapter(response.body()!!)
                        expectedRecyclerView.adapter = adapter

                        rigthArrowFour.setOnClickListener {
                            val popup = PopupMenu(this@RecipeSeeMainActivity, rigthArrowFour)
                            val items = listOf("1인분", "2인분", "3인분", "4인분", "5인분")

                            items.forEach { popup.menu.add(it) }

                            popup.setOnMenuItemClickListener { item: MenuItem ->
                                onePerson.text = item.title
                                val servings = item.title.toString().replace("인분", "").toIntOrNull() ?: 1
                                adapter?.updateServings(servings)
                                true
                            }

                            popup.show()
                        }
                    } else {
                        Log.e("RecipeSeeMain", "예상 사용 재료 응답 오류: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<ExpectedIngredient>>, t: Throwable) {
                    Log.e("RecipeSeeMain", "예상 사용 재료 불러오기 실패: ${t.message}")
                }
            })

    }

    private fun showMaterialUseBox() {
        findViewById<View>(R.id.dimView).visibility = View.VISIBLE
        findViewById<View>(R.id.materialUseBox).visibility = View.VISIBLE
        findViewById<View>(R.id.materialUse).visibility = View.VISIBLE
        findViewById<Button>(R.id.no).visibility = View.VISIBLE
        findViewById<Button>(R.id.yes).visibility = View.VISIBLE
    }

    private fun hideMaterialUseBox() {
        findViewById<View>(R.id.dimView).visibility = View.GONE
        findViewById<View>(R.id.materialUseBox).visibility = View.GONE
        findViewById<View>(R.id.materialUse).visibility = View.GONE
        findViewById<Button>(R.id.no).visibility = View.GONE
        findViewById<Button>(R.id.yes).visibility = View.GONE
    }
}