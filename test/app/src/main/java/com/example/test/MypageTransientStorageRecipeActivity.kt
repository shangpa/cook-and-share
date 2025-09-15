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
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.test.adapter.DraftAdapter
import com.example.test.model.recipeDetail.RecipeDraftDto

class MypageTransientStorageRecipeActivity : AppCompatActivity() {

    private lateinit var adapter: DraftAdapter
    private lateinit var numberTextView: TextView
    private var allDrafts: List<RecipeDraftDto> = emptyList()
    private var currentCategory: String? = null // "koreaFood" 등

    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_transient_storage_recipe)

        numberTextView = findViewById(R.id.number)
        findViewById<ImageButton>(R.id.backArrow).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.draftRecycler)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = DraftAdapter(
            onClick = { draft ->
                val clazz = when (draft.recipeType?.uppercase()) {
                    "IMAGE" -> RecipeWriteImageActivity::class.java
                    "VIDEO" -> RecipeWriteVideoActivity::class.java
                    "BOTH"  -> RecipeWriteBothActivity::class.java
                    else    -> RecipeWriteBothActivity::class.java // 안전한 기본값
                }
                val intent = Intent(this, clazz).apply {
                    putExtra("draftId", draft.recipeId ?: -1L)
                    putExtra("recipeType", draft.recipeType) // 필요하면 타입도 같이 전달
                }
                startActivity(intent)
            },
            onDelete = { draft ->
                val token = "Bearer ${App.prefs.token}"
                RetrofitInstance.apiService.deleteMyDraft(token, draft.recipeId!!)
                    .enqueue(object: Callback<Void> {
                        override fun onResponse(call: Call<Void>, res: Response<Void>) {
                            if (res.isSuccessful) {
                                Toast.makeText(this@MypageTransientStorageRecipeActivity, "삭제 완료", Toast.LENGTH_SHORT).show()
                                loadDrafts()
                            } else {
                                Toast.makeText(this@MypageTransientStorageRecipeActivity, "삭제 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@MypageTransientStorageRecipeActivity, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        )
        rv.adapter = adapter

        // 카테고리 버튼 세팅 (선택 시 currentCategory 갱신 후 filter)
        setupCategoryButtons()

        // 최초 로드
        loadDrafts()
    }

    private fun loadDrafts() {
        val token = "Bearer ${App.prefs.token}"
        RetrofitInstance.apiService.getMyDrafts(token)
            .enqueue(object: Callback<List<RecipeDraftDto>> {
                override fun onResponse(call: Call<List<RecipeDraftDto>>, res: Response<List<RecipeDraftDto>>) {
                    if (res.isSuccessful) {
                        allDrafts = res.body().orEmpty()
                        applyFilter()
                    } else {
                        allDrafts = emptyList()
                        applyFilter()
                        Toast.makeText(this@MypageTransientStorageRecipeActivity, "불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<RecipeDraftDto>>, t: Throwable) {
                    allDrafts = emptyList()
                    applyFilter()
                    Toast.makeText(this@MypageTransientStorageRecipeActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilter() {
        val filtered = allDrafts.filter { d ->
            currentCategory == null || d.category == currentCategory
        }
        adapter.submitList(filtered)
        numberTextView.text = filtered.size.toString()
    }

    private fun setupCategoryButtons() {
        val buttons = listOf(
            Pair(findViewById<AppCompatButton>(R.id.allBtn), null),
            Pair(findViewById<AppCompatButton>(R.id.krBtn), "koreaFood"),
            Pair(findViewById<AppCompatButton>(R.id.wsBtn), "westernFood"),
            Pair(findViewById<AppCompatButton>(R.id.jpBtn), "japaneseFood"),
            Pair(findViewById<AppCompatButton>(R.id.cnBtn), "chineseFood"),
            Pair(findViewById<AppCompatButton>(R.id.vgBtn), "vegetarianDiet"),
            Pair(findViewById<AppCompatButton>(R.id.snBtn), "snack"),
            Pair(findViewById<AppCompatButton>(R.id.asBtn), "alcoholSnack"),
            Pair(findViewById<AppCompatButton>(R.id.sdBtn), "sideDish"),
        )
        buttons.forEach { (btn, code) ->
            btn.setOnClickListener {
                currentCategory = code
                styleCategoryButtons(buttons.map { it.first }, btn)
                applyFilter()
            }
        }
        // 초기 선택
        styleCategoryButtons(buttons.map { it.first }, buttons[0].first)
    }

    private fun styleCategoryButtons(all: List<AppCompatButton>, selected: AppCompatButton) {
        all.forEach { b ->
            if (b == selected) {
                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
                    setColor(ContextCompat.getColor(this@MypageTransientStorageRecipeActivity, R.color.black))
                }
                b.background = bg
                b.setTextColor(Color.WHITE)
            } else {
                b.setBackgroundResource(R.drawable.btn_recipe_add)
                b.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    private fun dpToPx(dp: Float) = dp * resources.displayMetrics.density
}
