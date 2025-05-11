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
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeSeeMainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see_main)
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
        viewWithTimer.setOnClickListener {
            val intent = Intent(this, RecipeSeeActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)

        }

        val rigthArrow: ImageButton = findViewById(R.id.rigthArrow)
        rigthArrow.setOnClickListener {
            val intent = Intent(this, RecipeSeeActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        val viewWithoutTimer: TextView = findViewById(R.id.viewWithoutTimer)
        viewWithoutTimer.setOnClickListener {
            val intent = Intent(this, RecipeSeeNoTimerActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        val rigthArrowTwo: ImageButton = findViewById(R.id.rigthArrowTwo)
        rigthArrowTwo.setOnClickListener {
            val intent = Intent(this, RecipeSeeNoTimerActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        val videoSee: TextView = findViewById(R.id.videoSee)
        videoSee.setOnClickListener {
            val intent = Intent(this, RecipeSeeVideoActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        val rigthArrowThree: ImageButton = findViewById(R.id.rigthArrowThree)
        rigthArrowThree.setOnClickListener {
            val intent = Intent(this, RecipeSeeVideoActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        val yes: Button = findViewById(R.id.yes)
        yes.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        val recipeVideoBar = findViewById<View>(R.id.divideRectangleBarFour)

        //레시피 메인 선언
        val manageButton = findViewById<Button>(R.id.manageButton)
        val dimView = findViewById<View>(R.id.dimView)
        val materialUseBox = findViewById<View>(R.id.materialUseBox)
        val materialUse = findViewById<View>(R.id.materialUse)
        val no = findViewById<Button>(R.id.no)
        val rigthArrowFour = findViewById<ImageButton>(R.id.rigthArrowFour)
        val onePerson = findViewById<TextView>(R.id.onePerson)

        // 냉장고 재료 관리하러 가기 버튼 클릭시 상자 보이기
        manageButton.setOnClickListener {
            dimView.visibility = View.VISIBLE
            materialUseBox.visibility = View.VISIBLE
            materialUse.visibility = View.VISIBLE
            no.visibility = Button.VISIBLE
            yes.visibility = Button.VISIBLE
        }

        // 냉장고 재료 관리하러 가기 버튼 클릭시 상자 보이기
        no.setOnClickListener {
            dimView.visibility = View.GONE
            materialUseBox.visibility = View.GONE
            materialUse.visibility = View.GONE
            no.visibility = Button.GONE
            yes.visibility = Button.GONE
        }

        //리뷰 드롭다운 버튼 클릭
        rigthArrowFour.setOnClickListener {
            val popup = PopupMenu(this, rigthArrowFour)
            val items = listOf("1인분", "2인분", "3인분","4인분", "5인분")

            items.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                onePerson.text = item.title // 선택된 텍스트 적용!
                true
            }

            popup.show()
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
    }
}