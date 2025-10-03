package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.IngredientGridAdapter
import com.example.test.model.pantry.IngredientMasterResponse
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.*
import java.io.File

class PantryMaterialAddActivity : AppCompatActivity() {

    private lateinit var buttons: List<AppCompatButton>

    private lateinit var rv: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: IngredientGridAdapter
    private lateinit var searchEt: EditText
    private lateinit var searchBtn: ImageButton

    private var pantryId: Long = -1L
    private var currentCategory: String = "Vegetables"
    private var keyword: String? = null

    // 촬영 결과 저장 URI
    private var cameraUri: Uri? = null

    /** 카메라 촬영 (지정 URI에 저장) */
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && cameraUri != null) {
                goReceiptReview(cameraUri!!)
            } else {
                Toast.makeText(this, "촬영이 취소되었어요.", Toast.LENGTH_SHORT).show()
            }
        }

    /** 앨범에서 선택 (시스템 포토피커/갤러리) */
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) goReceiptReview(uri)
            else Toast.makeText(this, "선택이 취소되었어요.", Toast.LENGTH_SHORT).show()
        }

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

        // 상단 등록 버튼 → 1차 메뉴
        addBtn.setOnClickListener { showRegisterMenu(it) }
        registerText.setOnClickListener { showRegisterMenu(addBtn) }

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

        // 초기 상태
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
    }

    /** 1차 메뉴: 영수증/사진 */
    private fun showRegisterMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("영수증으로 등록")
        popup.menu.add("사진으로 등록")
        popup.setOnMenuItemClickListener {
            when (it.title) {
                "영수증으로 등록" -> { showReceiptSourceChooser(); true }   // 2차 메뉴로
                "사진으로 등록"   -> { openGallery(); true }               // 곧바로 앨범
                else -> false
            }
        }
        popup.show()
    }

    /** 2차 메뉴: 카메라/앨범 */
    private fun showReceiptSourceChooser() {
        val items = arrayOf("카메라 촬영", "앨범에서 선택")
        AlertDialog.Builder(this)
            .setTitle("영수증으로 등록")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {                      // 카메라
                        cameraUri = createTempImageUri()
                        if (cameraUri == null) {
                            Toast.makeText(this, "이미지 저장 Uri 생성 실패", Toast.LENGTH_SHORT).show()
                        } else {
                            takePictureLauncher.launch(cameraUri)
                        }
                    }
                    1 -> openGallery()          // 앨범
                }
            }.show()
    }


    /** 카메라 열기: 임시파일 URI 만들고 촬영 */
    private fun openCamera() {
        cameraUri = createTempImageUri() ?: run {
            Toast.makeText(this, "이미지 저장 Uri 생성 실패", Toast.LENGTH_SHORT).show()
            return
        }
        takePictureLauncher.launch(cameraUri)
    }

    /** 앨범 열기 */
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    /** 촬영/선택 결과 → OCR 리뷰 화면으로 */
    private fun goReceiptReview(uri: Uri) {
        startActivity(Intent(this, ReceiptReviewActivity::class.java).apply {
            putExtra("imageUri", uri.toString())
            putExtra("pantryId", pantryId)
        })
    }

    /** FileProvider용 임시 이미지 URI 생성 (file_paths.xml: cache/external-cache 기준) */
    private fun createTempImageUri(): Uri? {
        val file = File.createTempFile("receipt_", ".jpg", cacheDir)
        return FileProvider.getUriForFile(
            this,
            "${packageName}.provider",   // <-- Manifest랑 동일하게 .provider 로!
            file
        )
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

        loadJob?.cancel()
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
                button.setTextColor(android.graphics.Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add)
                button.setTextColor(android.graphics.Color.parseColor("#8A8F9C"))
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

    /** 프로젝트 토큰 → "Bearer xxx" */
    private fun getBearerToken(): String? {
        val raw = App.prefs.token
        return if (!raw.isNullOrBlank()) "Bearer $raw" else null
    }
}
