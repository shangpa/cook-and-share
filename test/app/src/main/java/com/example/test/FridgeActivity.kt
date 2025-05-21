package com.example.test

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.FridgeAdapter
import com.example.test.model.Fridge.FridgeCreateRequest
import com.example.test.model.Fridge.FridgeResponse
import com.example.test.model.Fridge.IngredientData
import com.example.test.model.Fridge.SelectedIngredient
import com.example.test.model.recipt.*
import com.example.test.network.ApiService
import com.example.test.network.GoogleVisionApi
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FridgeActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private var allFridgeList: List<FridgeResponse> = listOf()
    private var currentCategory = "전체"
    private var selectedFridgeIds = mutableSetOf<Long>()
    private lateinit var fridgeAdapter: FridgeAdapter
    private lateinit var imageUri: Uri
    private var displayedFridgeList: List<FridgeResponse> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1001)
        }

        apiService = RetrofitInstance.apiService

        findViewById<TextView>(R.id.dayInput).text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())

        val categoryAll = findViewById<LinearLayout>(R.id.categoryAll)
        val categoryFridge = findViewById<LinearLayout>(R.id.categoryFridge)
        val categoryFreeze = findViewById<LinearLayout>(R.id.categoryFreeze)
        val categoryRoom = findViewById<LinearLayout>(R.id.categoryRoom)
        val categoryViews = listOf(categoryAll, categoryFridge, categoryFreeze, categoryRoom)

        setCategorySelected(categoryAll, categoryViews)
        categoryViews.forEach { container ->
            container.setOnClickListener {
                setCategorySelected(it as LinearLayout, categoryViews)
            }
        }

        findViewById<TextView>(R.id.fridegeCameraText).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("사진 가져오기")
                .setItems(arrayOf("카메라 촬영", "앨범에서 선택")) { _, which ->
                    when (which) {
                        0 -> {
                            val photoFile = File.createTempFile("ocr_", ".jpg", cacheDir)
                            imageUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
                            takePictureLauncher.launch(imageUri)
                        }
                        1 -> {
                            galleryLauncher.launch("image/*")
                        }
                    }
                }.show()
        }

        findViewById<RecyclerView>(R.id.fridgeRecyclerView).layoutManager = LinearLayoutManager(this)

        findViewById<LinearLayout>(R.id.fridgeAddBtn).setOnClickListener {
            startActivity(Intent(this, FridgeIngredientActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.recipeRecommendBtn).setOnClickListener {
            val selectedIngredients = allFridgeList.filter { selectedFridgeIds.contains(it.id) }.map {
                SelectedIngredient(
                    name = it.ingredientName,
                    quantity = it.quantity,
                    unit = it.unitDetail,
                    dateLabel = it.dateOption ?: "유통기한",
                    dateText = it.fridgeDate
                )
            }

            if (selectedIngredients.isEmpty()) {
                Toast.makeText(this, "추천할 재료를 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val allIngredients = allFridgeList.map {
                SelectedIngredient(
                    name = it.ingredientName,
                    quantity = it.quantity,
                    unit = it.unitDetail,
                    dateLabel = it.dateOption ?: "유통기한",
                    dateText = it.fridgeDate
                )
            }

            val intent = Intent(this, FridgeRecipeActivity::class.java).apply {
                putParcelableArrayListExtra("selectedIngredients", ArrayList(selectedIngredients))
                putParcelableArrayListExtra("allIngredients", ArrayList(allIngredients))
            }
            startActivity(intent)
        }

        findViewById<TextView>(R.id.fridgeDeleteText).setOnClickListener {
            val token = "Bearer ${getTokenFromPreferences()}"
            val deleteTargets = allFridgeList.filter { selectedFridgeIds.contains(it.id) }

            if (deleteTargets.isEmpty()) {
                Toast.makeText(this, "삭제할 항목을 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            deleteTargets.forEach { fridge ->
                CoroutineScope(Dispatchers.IO).launch {
                    val response = apiService.deleteFridge(fridge.id, token)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@FridgeActivity, "삭제 완료!", Toast.LENGTH_SHORT).show()
                            fetchFridgeData()
                        } else {
                            Toast.makeText(this@FridgeActivity, "삭제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        findViewById<TextView>(R.id.fridgeEditText).setOnClickListener {
            if (selectedFridgeIds.size != 1) {
                Toast.makeText(this, "편집할 항목을 하나 선택해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val targetFridge = allFridgeList.find { it.id == selectedFridgeIds.first() }
                if (targetFridge != null) {
                    val intent = Intent(this, FridgeIngredientActivity::class.java).apply {
                        putExtra("ingredientName", targetFridge.ingredientName)
                        putExtra("quantity", targetFridge.quantity.toString())
                        putExtra("unit", targetFridge.unitDetail)
                        putExtra("storageArea", targetFridge.storageArea)
                        putExtra("fridgeDate", targetFridge.fridgeDate)
                        putExtra("dateOption", targetFridge.dateOption)
                        putExtra("fridgeId", targetFridge.id)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "선택된 항목의 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<EditText>(R.id.fridgeSearchInput).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterAndDisplayFridgeItems(currentCategory, s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<ImageView>(R.id.fridgeAllCheckIcon).setOnClickListener {
            val icon = it as ImageView
            val filteredIds = displayedFridgeList.map { it.id }
            val isAllSelected = selectedFridgeIds.containsAll(filteredIds)

            if (isAllSelected) {
                selectedFridgeIds.removeAll(filteredIds)
                icon.setImageResource(R.drawable.ic_fridge_check)
            } else {
                selectedFridgeIds.addAll(filteredIds)
                icon.setImageResource(R.drawable.btn_fridge_checked)
            }

            fridgeAdapter.notifyDataSetChanged()
        }

    }

    override fun onResume() {
        super.onResume()
        fetchFridgeData()
    }

    private fun fetchFridgeData() {
        val token = "Bearer ${getTokenFromPreferences()}"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMyFridges(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        allFridgeList = response.body()?.sortedByDescending { it.updatedAt } ?: listOf()
                        filterAndDisplayFridgeItems(currentCategory)
                    } else {
                        Toast.makeText(this@FridgeActivity, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "알 수 없는 오류"
                Log.e("FridgeActivity", "데이터 불러오기 예외", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FridgeActivity, "네트워크 오류: $msg", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterAndDisplayFridgeItems(category: String, keyword: String = "") {
        val filteredList = allFridgeList.filter {
            val categoryMatch = when (category) {
                "냉장" -> it.storageArea == "냉장"
                "냉동" -> it.storageArea == "냉동"
                "실온" -> it.storageArea == "실온"
                else -> true
            }
            val keywordMatch = it.ingredientName.contains(keyword, ignoreCase = true)
            categoryMatch && keywordMatch
        }

        // 동일 ingredientName끼리 묶어서 quantity 합산
        val groupedList = filteredList
            .filter { it.ingredientName != null && it.updatedAt != null }
            .groupBy { it.ingredientName!! }
            .map { (name, items) ->
                val latest = items.maxByOrNull { it.updatedAt!! } ?: items.first()
                latest.copy(quantity = items.sumOf { it.quantity })
            }
        fridgeAdapter = FridgeAdapter(
            fridgeList = groupedList,
            selectedIds = selectedFridgeIds,
            onItemClick = { fridge ->
                if (selectedFridgeIds.contains(fridge.id)) {
                    selectedFridgeIds.remove(fridge.id)
                } else {
                    selectedFridgeIds.add(fridge.id)
                }
                fridgeAdapter.notifyDataSetChanged()
            },
            onIconClick = { fridge ->
                val intent = Intent(this@FridgeActivity, FridgeMaterialListActivity::class.java)
                intent.putExtra("ingredientName", fridge.ingredientName) // ✅ 수정
                startActivity(intent)
            }
        )

        displayedFridgeList = groupedList
        findViewById<RecyclerView>(R.id.fridgeRecyclerView).adapter = fridgeAdapter
    }

    private fun setCategorySelected(selected: LinearLayout, allCategories: List<LinearLayout>) {
        val textView = selected.getChildAt(0) as? TextView
        currentCategory = textView?.text.toString()
        filterAndDisplayFridgeItems(currentCategory)

        for (container in allCategories) {
            val tv = container.getChildAt(0) as? TextView
            if (container == selected) {
                container.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                tv?.setTextColor(resources.getColor(R.color.white))
            } else {
                container.setBackgroundResource(R.drawable.btn_fridge_ct)
                tv?.setTextColor(resources.getColor(R.color.black))
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    val cleanedText = text.replace(" ", "").replace("\n", "")
                    if (!cleanedText.contains(Regex("[가-힣]"))) {
                        Log.d("OCR_RESULT", "한글 없음, Vision API 시도")
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        callGoogleVisionAPI(bitmap)
                        return@addOnSuccessListener
                    }
                    handleMatchedText(cleanedText)
                }
                .addOnFailureListener { e ->
                    Log.w("OCR_MLKIT", "ML Kit 인식 실패: ${e.message}, Vision API 시도")
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    callGoogleVisionAPI(bitmap)
                }
        }
    }

    private fun handleMatchedText(text: String) {
        val knownIngredients = listOf("감자", "깻잎", "햇감자", "당근", "양파", "계란", "오이", "상추", "시금치")
        val matched = knownIngredients.firstOrNull { text.contains(it) }
        if (matched != null) {
            val intent = Intent(this, FridgeIngredientActivity::class.java).apply {
                putExtra("ingredientName", matched)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "음식 재료를 인식하지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            callGoogleVisionAPI(bitmap)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            callGoogleVisionAPI(bitmap)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    fun callGoogleVisionAPI(bitmap: Bitmap) {
        val base64Image = bitmapToBase64(bitmap)
        val image = Image(content = base64Image)
        val feature = Feature()
        val context = ImageContext(languageHints = listOf("ko"))
        val requestItem = RequestItem(image, listOf(feature), context)
        val request = VisionRequest(listOf(requestItem))

        val gson = Gson()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://vision.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleVisionApi::class.java)
        val apiKey = "AIzaSyDQWnQPMzjhZU4h6XZ3R9f8BO3bTwt8at0"
        Log.d("API_KEY_CHECK", "Google Vision API Key: '$apiKey'")
        val call = service.annotateImage(apiKey, request)

        Log.d("VISION_REQUEST_URL", call.request().url.toString())

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val text = response.body()
                        ?.getAsJsonArray("responses")
                        ?.get(0)?.asJsonObject
                        ?.getAsJsonObject("fullTextAnnotation")
                        ?.get("text")?.asString ?: "결과 없음"
                    Log.d("VISION_REQUEST_URL", call.request().url.toString())
                    Log.d("VISION_RESULT", text)
                    handleDetectedText(text)
                } else {
                    Log.e("VISION_ERROR", "오류 응답: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("VISION_ERROR", "네트워크 오류", t)
            }
        })
    }


    private fun handleDetectedText(text: String) {
        val dateRegex = Regex("""판매일[:\s]+(\d{2,4}[-.]\d{2}[-.]\d{2})""")
        val dateRaw = dateRegex.find(text)?.groupValues?.get(1) ?: ""
        val fridgeDate = if (dateRaw.isNotEmpty()) {
            val parts = dateRaw.split("[-./]".toRegex())
            if (parts[0].length == 2) "20${parts[0]}-${parts[1]}-${parts[2]}" else dateRaw
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        val lines = text.lines()
        val items = mutableListOf<FridgeCreateRequest>()

        // 여기서 itemHeaderRegex 선언
        val itemHeaderRegex = Regex("""\d{3}\s+([가-힣]+)""")

        var index = 0
        while (index < lines.size) {
            val line = lines[index].trim()
            val matchedName = itemHeaderRegex.find(line)?.groupValues?.get(1)

            if (matchedName != null && isValidIngredientName(matchedName)) {
                var quantity = 1.0
                if (index + 3 < lines.size) {
                    val quantityLine = lines[index + 3].trim().replace(",", "")
                    quantity = quantityLine.toDoubleOrNull() ?: 1.0
                }

                items.add(
                    FridgeCreateRequest(
                        ingredientName = matchedName,
                        quantity = quantity,
                        fridgeDate = fridgeDate,
                        dateOption = "구매일자",
                        storageArea = "실온",
                        unitDetail = "개",
                        unitCategory = "COUNT"
                    )
                )
                index += 1 // 한 줄씩 검사하도록 변경
            } else {
                index++
            }
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "인식된 재료가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        saveItemsToServerSequentially(items)
    }


    fun isValidIngredientName(name: String): Boolean {
        val excludedWords = listOf("계산원", "고객", "카드", "영수증", "권미경", "신용카드", "판매일")
        if (name.length !in 2..6) return false
        if (excludedWords.any { name.contains(it) }) return false
        return true
    }
    private fun saveItemsToServerSequentially(items: List<FridgeCreateRequest>, index: Int = 0) {
        if (index >= items.size) {
            Log.d("OCR_SAVE_DEBUG", "모든 아이템 저장 완료")
            fetchFridgeData()
            return
        }

        val token = "Bearer ${App.prefs.token}"
        val api = RetrofitInstance.apiService
        val item = items[index]

        Log.d("OCR_SAVE_DEBUG", "아이템 ${index} 저장 시도: ${item.ingredientName}")

        api.createFridgeByOCR(item, token).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("OCR_SAVE_DEBUG", "아이템 ${index} 저장 완료: ${item.ingredientName}")
                    saveItemsToServerSequentially(items, index + 1)  // 재귀 호출
                } else {
                    Log.e("OCR_SAVE_DEBUG", "아이템 ${index} 저장 실패 코드: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                val message = t.message ?: "알 수 없는 네트워크 오류"
                Log.e("OCR_SAVE_DEBUG", "아이템 $index 저장 실패 에러: $message", t)
                runOnUiThread {
                    Toast.makeText(this@FridgeActivity, "네트워크 오류: $message", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }


    private fun parseItem(lines: List<String>, date: String): IngredientData? {
        // 1. 재료명 찾기: 한글로 된 단어 중 가장 먼저 나오는 걸 이름으로 사용
        val name = lines.firstOrNull { it.contains(Regex("[가-힣]+")) }?.trim() ?: return null

        // 2. 수량 찾기: 숫자만 있는 줄이나, '1' 또는 '2' 같은 단순 숫자를 우선 찾기
        val quantityLine = lines.firstOrNull { it.trim().matches(Regex("""\d+""")) }
        val quantity = quantityLine?.toIntOrNull() ?: 1

        return IngredientData(name, quantity.toString(), date)
    }


    private fun getTokenFromPreferences(): String {
        return App.prefs.token ?: ""
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
