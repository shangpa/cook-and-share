package com.example.test

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.IngredientGridAdapter
import com.example.test.model.pantry.IngredientMasterResponse
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PantryMaterialAddActivity : AppCompatActivity() {

    private lateinit var buttons: List<AppCompatButton>
    private lateinit var cameraPhotoUri: Uri

    private lateinit var rv: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: IngredientGridAdapter
    private lateinit var searchEt: EditText
    private lateinit var searchBtn: ImageButton

    private var pantryId: Long = -1L
    private var currentCategory: String = "Vegetables"
    private var keyword: String? = null

    // 갤러리
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { goToMaterialAdd(it, "gallery") } }

    // 카메라
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) goToMaterialAdd(cameraPhotoUri, "camera") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_material_add)

        TabBarUtils.setupTabBar(this)
        pantryId = intent.getLongExtra("pantryId", -1L)
        Log.d("Fridge", "PantryMaterialAddActivity pantryId=$pantryId")

        findViewById<ImageButton>(R.id.backArrow).setOnClickListener { finish() }

        val addBtn = findViewById<ImageButton>(R.id.add)
        val registerText = findViewById<TextView>(R.id.register)

        searchEt = findViewById(R.id.materialSearch)
        searchBtn = findViewById(R.id.searchIcon)

        rv = findViewById(R.id.rvIngredients)
        rv.layoutManager = GridLayoutManager(this, 4)
        adapter = IngredientGridAdapter { m -> onIngredientClick(m) }
        rv.adapter = adapter

        // 카테고리 버튼들
        buttons = listOf(
            findViewById(R.id.total),
            findViewById(R.id.vegetable),
            findViewById(R.id.meat),
            findViewById(R.id.seafood),
            findViewById(R.id.grain),
            findViewById(R.id.fruits),
            findViewById(R.id.dairyProduct),
            findViewById(R.id.season),
            findViewById(R.id.processedFood),
            findViewById(R.id.noodle),
            findViewById(R.id.etc)
        )

        buttons.forEachIndexed { idx, btn ->
            btn.setOnClickListener {
                applyCategoryUi(idx)
                currentCategory = mapIndexToServerCategory(idx)
                loadIngredients()
            }
        }
        // 초기: "전체" 선택 UI만 주고 실제 쿼리는 채소로 시작
        applyCategoryUi(0)
        currentCategory = "Vegetables"
        loadIngredients()

        // 검색
        searchBtn.setOnClickListener {
            keyword = searchEt.text?.toString()?.trim().orEmpty().ifBlank { null }
            loadIngredients()
        }
        searchEt.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                keyword = v.text?.toString()?.trim().orEmpty().ifBlank { null }
                loadIngredients()
                true
            } else false
        }

        // 영수증/사진 등록 (TODO)
        val showRegisterMenu: (View) -> Unit = { anchor ->
            val popup = PopupMenu(this, anchor)
            listOf("영수증으로 등록", "사진으로 등록").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "영수증으로 등록" -> {
                        val photoFile = File.createTempFile("receipt_", ".jpg", cacheDir)
                        cameraPhotoUri = FileProvider.getUriForFile(
                            this, "${packageName}.provider", photoFile
                        )
                        takePictureLauncher.launch(cameraPhotoUri)
                        true
                    }
                    "사진으로 등록" -> { galleryLauncher.launch("image/*"); true }
                    else -> false
                }
            }
            popup.show()
        }
        addBtn.setOnClickListener(showRegisterMenu)
        registerText.setOnClickListener { showRegisterMenu(findViewById(R.id.add)) }
    }

    private fun onIngredientClick(m: IngredientMasterResponse) {
        startActivity(
            Intent(this, PantryMaterialAddDetailActivity::class.java).apply {
                putExtra("pantryId", pantryId)
                putExtra("ingredientId", m.id)
                putExtra("ingredientName", m.nameKo)
                putExtra("defaultUnitId", m.defaultUnitId ?: -1L)
                putExtra("iconUrl", m.iconUrl)
                putExtra("category", m.category)
            }
        )
    }

    private var loadJob: Job? = null

    private fun loadIngredients() {
        val token = getBearerToken() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        loadJob?.cancel() // 이전 요청 취소(의도적으로)
        loadJob = lifecycleScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.getIngredients(
                        token = token,
                        category = currentCategory,
                        keyword = keyword,
                        page = 0,
                        size = 100
                    )
                }
                adapter.submit(list)
            } catch (_: CancellationException) {
                // 사용자가 탭 전환/뒤로가기/다른 카테고리 선택 등으로 취소된 정상 케이스
            } catch (e: Exception) {
                Log.e("Fridge API", "재료 불러오기 실패", e)
                Toast.makeText(this@PantryMaterialAddActivity, "재료 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyCategoryUi(selectedIndex: Int) {
        buttons.forEachIndexed { i, button ->
            if (i == selectedIndex) {
                button.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                button.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    /** 한글 버튼 index → 서버 enum */
    private fun mapIndexToServerCategory(idx: Int): String = when (idx) {
        1 -> "Vegetables"
        2 -> "Meats"
        3 -> "Seafood"
        4 -> "Grains"
        5 -> "Fruits"
        6 -> "Dairy"
        7 -> "Seasonings"
        8 -> "ProcessedFoods"
        9 -> "Noodles"
        10 -> "Others"
        11 -> "Kimchi"
        12 -> "Beverages"
        else -> "Vegetables"
    }

    private fun goToMaterialAdd(uri: Uri, source: String) {
        startActivity(Intent(this, PantryMaterialAddDetailActivity::class.java).apply {
            putExtra("imageUri", uri.toString())
            putExtra("source", source)
            putExtra("pantryId", pantryId)
        })
    }

    /** 프로젝트에 맞게 토큰을 꺼내와서 "Bearer xxx" 형태로 반환 */
    private fun getBearerToken(): String? {
        val raw = App.prefs.token
        return if (!raw.isNullOrBlank()) "Bearer $raw" else null
    }
}
