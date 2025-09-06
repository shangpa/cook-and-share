package com.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.RecipeSearchAdapter
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.View
import android.widget.LinearLayout

class MypageTransientStorageRecipeActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<AppCompatButton>
    private lateinit var adapter: RecipeSearchAdapter
    private lateinit var numberTextView: TextView
    private lateinit var sortTextView: TextView
    private var currentSelectedCategory: String? = null
    private var sortOrder: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_transient_storage_recipe)

        // 카테고리 버튼 세팅
        val allBtn = findViewById<AppCompatButton>(R.id.allBtn)
        val krBtn = findViewById<AppCompatButton>(R.id.krBtn)
        val wsBtn = findViewById<AppCompatButton>(R.id.wsBtn)
        val jpBtn = findViewById<AppCompatButton>(R.id.jpBtn)
        val cnBtn = findViewById<AppCompatButton>(R.id.cnBtn)
        val vgBtn = findViewById<AppCompatButton>(R.id.vgBtn)
        val snBtn = findViewById<AppCompatButton>(R.id.snBtn)
        val asBtn = findViewById<AppCompatButton>(R.id.asBtn)
        val sdBtn = findViewById<AppCompatButton>(R.id.sdBtn)

        // 레이아웃
        val recipeList = findViewById<ConstraintLayout>(R.id.recipeList)
        val recipeListTwo = findViewById<ConstraintLayout>(R.id.recipeListTwo)
        val recipeListThree = findViewById<ConstraintLayout>(R.id.recipeListThree)
        val recipeListFour = findViewById<ConstraintLayout>(R.id.recipeListFour)

        // 삭제 버튼
        setDeleteAction(findViewById(R.id.deleteBtn), findViewById(R.id.recipeList))
        setDeleteAction(findViewById(R.id.deleteBtnTwo), findViewById(R.id.recipeListTwo))
        setDeleteAction(findViewById(R.id.deleteBtnThree), findViewById(R.id.recipeListThree))
        setDeleteAction(findViewById(R.id.deleteBtnFour), findViewById(R.id.recipeListFour))

        categoryButtons = listOf(allBtn, krBtn, wsBtn, jpBtn, cnBtn, vgBtn, snBtn, asBtn, sdBtn)

        allBtn.setOnClickListener { setCategoryButtonStyle(allBtn); filterByCategory(null) }
        krBtn.setOnClickListener { setCategoryButtonStyle(krBtn); filterByCategory("koreaFood") }
        wsBtn.setOnClickListener { setCategoryButtonStyle(wsBtn); filterByCategory("westernFood") }
        jpBtn.setOnClickListener { setCategoryButtonStyle(jpBtn); filterByCategory("japaneseFood") }
        cnBtn.setOnClickListener { setCategoryButtonStyle(cnBtn); filterByCategory("chineseFood") }
        vgBtn.setOnClickListener { setCategoryButtonStyle(vgBtn); filterByCategory("vegetarianDiet") }
        snBtn.setOnClickListener { setCategoryButtonStyle(snBtn); filterByCategory("snack") }
        asBtn.setOnClickListener { setCategoryButtonStyle(asBtn); filterByCategory("alcoholSnack") }
        sdBtn.setOnClickListener { setCategoryButtonStyle(sdBtn); filterByCategory("sideDish") }

        // 갯수 텍스트뷰
        numberTextView = findViewById(R.id.number)

        val writeIntent = Intent(this, RecipeWriteMain::class.java)
        val initialCategory = intent.getStringExtra("selectedCategory")

        val categoryMap = mapOf(
            null to allBtn,
            "koreaFood" to krBtn,
            "westernFood" to wsBtn,
            "japaneseFood" to jpBtn,
            "chineseFood" to cnBtn,
            "vegetarianDiet" to vgBtn,
            "snack" to snBtn,
            "alcoholSnack" to asBtn,
            "sideDish" to sdBtn
        )

        val selectedBtn = categoryMap[initialCategory] ?: allBtn
        setCategoryButtonStyle(selectedBtn)
        filterByCategory(initialCategory)

        // 레이아웃 클릭시 RecipeWriteBothActivity 이동
        val listener = View.OnClickListener {
            val intent = Intent(this, RecipeWriteBothActivity::class.java)
            startActivity(intent)
        }

        recipeList.setOnClickListener(listener)
        recipeListTwo.setOnClickListener(listener)
        recipeListThree.setOnClickListener(listener)
        recipeListFour.setOnClickListener(listener)

        // 레시피 작성 메인으로 이동
        val recipeWriteButton: AppCompatButton = findViewById(R.id.recipeWriteButton)
        recipeWriteButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteMain::class.java)
            startActivity(intent)
        }

        // 뒤로가기
        findViewById<ImageButton>(R.id.backArrow).setOnClickListener {
            finish()
        }
    }

    private fun filterByCategory(category: String?) {
        val token = "Bearer ${App.prefs.token}"
        currentSelectedCategory = category
        RetrofitInstance.apiService.getAllPublicRecipes(token = token,sort = sortOrder)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (response.isSuccessful) {
                        val recipeList = response.body()?.filter {
                            category == null || it.category == category
                        } ?: emptyList()

                        adapter.updateData(recipeList)
                        numberTextView.text = recipeList.size.toString()
                    } else {
                        Toast.makeText(this@MypageTransientStorageRecipeActivity, "레시피 불러오기 실패", Toast.LENGTH_SHORT).show()
                        numberTextView.text = "0"
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@MypageTransientStorageRecipeActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    numberTextView.text = "0"
                }
            })
    }

    private fun setCategoryButtonStyle(selectedButton: AppCompatButton) {
        categoryButtons.forEach { button ->
            if (button == selectedButton) {
                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
                    setColor(ContextCompat.getColor(this@MypageTransientStorageRecipeActivity, R.color.black))
                }
                button.background = selectedDrawable
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    // 삭제 버튼 클릭시 해당 레이아웃 삭제
    private fun setDeleteAction(deleteBtn: ImageButton, targetLayout: ConstraintLayout) {
        deleteBtn.setOnClickListener {
            targetLayout.visibility = View.GONE
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
