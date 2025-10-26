/*레시피 이미지*/
//todo 타이머 수정해야함
package com.example.test

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
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
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.example.test.Repository.RecipeRepository
import com.example.test.Utils.TabBarUtils
import com.example.test.model.recipeDetail.CookingStep
import com.example.test.model.Ingredient
import com.example.test.model.MasterIngredient
import com.example.test.model.RecipeRequest
import com.example.test.model.ingredients.IngredientResponse
import com.example.test.model.recipeDetail.PublishRequest
import com.example.test.model.recipeDetail.RecipeCreateResponse
import com.example.test.model.recipeDetail.RecipeDraftDto
import com.example.test.model.recipeDetail.RecipeIngredientReq
import com.example.test.model.recipeDetail.ThumbnailResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Stack
import com.example.test.model.recipeDetail.RecipeIngredientRes
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private lateinit var materialContainer: LinearLayout
private lateinit var replaceMaterialContainer: LinearLayout
private lateinit var handlingMethodContainer: LinearLayout
private lateinit var cookOrderRecipeContainer: LinearLayout
private lateinit var replaceMaterialAddFixButton: AppCompatButton
private lateinit var handlingMethodAddFixButton: AppCompatButton
private lateinit var hourEditText: EditText
private lateinit var minuteEditText: EditText
private lateinit var startTextView: TextView
private lateinit var timeSeparator: TextView
private lateinit var deleteTextView: TextView
private var createdRecipeId: Long? = null
private var itemCount = 0 // 추가된 개수 추적
private val maxItems = 10 // 최대 10개 제한
private val buttonMarginIncrease = 130 // 버튼을 아래로 내릴 거리 (px)
private var countDownTimer: CountDownTimer? = null
private var timeInMillis: Long = 0
private lateinit var imageContainer: LinearLayout
private lateinit var representImageContainer: LinearLayout
private var stepCount = 1 // 1-1부터 시작
private var currentStep = 1  // 현재 Step 번호 (ex. 1, 2, 3...)
private var currentSubStep = 1
private var recipeStepCount = 1 // 조리 순서 번호 관리 (1-1, 1-2, ...)
private var isPublic: Boolean = true
private var recipe: RecipeRequest? = null  // 있으면 생략
private var draftId: Long? = null
private val stepRecipeCountMap = mutableMapOf<Int, Int>()
private val stepTimerMap = mutableMapOf<Int, Pair<Int, Int>>()
private var pendingStepForImage: Int? = null
private var isSavingDraft = false

@androidx.media3.common.util.UnstableApi
class RecipeWriteImageActivity : AppCompatActivity() {
    //조리순서 이미지 선택된거
    private var selectedContainer: LinearLayout? = null
    //메인 이미지
    private var mainImageUrl: String = "" // 대표 이미지 저장용 변수
    // 업로드된 이미지 url
    private val stepImages  =  mutableMapOf<Int, String>()
    // 조리순서 이미지 업로드
    private val pickImageLauncherForCamera =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                    putExtra("imageUri", it.toString())
                    putExtra("isStepImage", true) // 조리순서인지 여부
                }
                startActivityForResult(intent, EDIT_IMAGE_REQUEST_CODE)
            }
        }
    val token = App.prefs.token ?: ""
    companion object {
        const val EDIT_IMAGE_REQUEST_CODE = 1001
    }

    private lateinit var container: LinearLayout
    private lateinit var searchBox: EditText
    private val allIngredients = mutableListOf<IngredientResponse>()   // 전체 목록
    private val selectedIngredients = mutableSetOf<Long>() // 선택된 재료 ID

    // 대표사진 이미지 업로드
    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(cacheDir, "cropped_represent_${System.currentTimeMillis()}.jpg"))
                val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                    putExtra("imageUri", it.toString())
                }
                startActivityForResult(intent, EDIT_IMAGE_REQUEST_CODE)
            }
        }
    private var isPickingRepresentImage = false
    private lateinit var stepContainer: LinearLayout // STEP을 추가할 컨테이너
    private lateinit var pickImageLauncherForStepCamera: ActivityResultLauncher<String>
    private lateinit var layoutList: List<ConstraintLayout>
    private lateinit var textViewList: List<TextView>
    private lateinit var underlineBar: View
    private val layoutHistory = Stack<ConstraintLayout>() // ← 이전 레이아웃 저장용
    private lateinit var currentLayout: ConstraintLayout
    private val tabCompleted = BooleanArray(6) { false }
    private lateinit var progressBars: List<View>
    private var selectedIndex = 0
    private lateinit var committedCompleted: BooleanArray
    private var lastSelectedIndex = 0
    private var isSwitching = false
    private val rectToRow = mutableMapOf<View, View>()
    private val selectedRects = mutableSetOf<View>()

    // Gson: null도 키 유지
    private val gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()     // ⭐ null이어도 키가 사라지지 않음
            .create()
    }

    // 업로드 진행 카운터(임시저장 전에 대기)
    private var uploadsInFlight = 0

    // 임시저장에 쓸 스텝 DTO (널 금지 + 기본값)
    data class CookingStepReq(
        val step: Int,
        val description: String,
        val mediaType: String = "NONE",   // "IMAGE"|"VIDEO"|"NONE"
        val mediaUrl: String = "",
        val timeInSeconds: Int = 0
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_image)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        TabBarUtils.setupTabBar(this)
        draftId = savedInstanceState?.getLong("draftId")
            ?: intent.getLongExtra("draftId", -1L).takeIf { it > 0 }

        // 배열 초기화
        committedCompleted = BooleanArray(tabCompleted.size) { false }
        lastSelectedIndex = selectedIndex

        // 재료
        materialContainer = findViewById(R.id.materialContainer)
        container = findViewById(R.id.categoryButtonContainer)
        searchBox = findViewById(R.id.materialCook)
        loadIngredients()


        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 filterIngredients 함수를 호출
                filterIngredients(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        // 대체 재료
        replaceMaterialContainer = findViewById(R.id.replaceMaterialContainer)
        replaceMaterialAddFixButton = findViewById(R.id.replaceMaterialAddFixButton)

        replaceMaterialAddFixButton.setOnClickListener {
            if (itemCount < maxItems) {
                replaceMaterialAddNewItem()
                replaceMaterialMoveButtonDown() // 버튼 아래로 이동
            }
        }

        // 처리방법
        handlingMethodContainer = findViewById(R.id.handlingMethodContainer)
        handlingMethodAddFixButton = findViewById(R.id.handlingMethodAddFixButton)

        handlingMethodAddFixButton.setOnClickListener {
            if (itemCount < maxItems) {
                handlingMethodAddNewItem()
                handlingMethodMoveButtonDown() // 버튼 아래로 이동
            }
        }

        // 카테고리 선언
        val recipeWrite = findViewById<View>(R.id.recipeWrite)
        val recipeWriteCategory = findViewById<ConstraintLayout>(R.id.recipeWriteCategory)
        val indicatorBar = findViewById<View>(R.id.divideRectangleBarTwentythree)

        // 레시피 타이틀 선언
        val recipeWriteTitleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout)
        val recipeTitleWrite = findViewById<EditText>(R.id.recipeTitleWrite)
        val downArrow = findViewById<ImageButton>(R.id.downArrow)
        val recipeName = findViewById<ConstraintLayout>(R.id.recipeName)
        val koreanFood = findViewById<TextView>(R.id.koreanFood)
        val continueButton = findViewById<AppCompatButton>(R.id.continueButton)
        val beforeButton = findViewById<AppCompatButton>(R.id.beforeButton)
        val temporaryStorageBtn = findViewById<AppCompatButton>(R.id.temporaryStorage)
        val transientStorageLayout = findViewById<ConstraintLayout>(R.id.transientStorage)
        val transientStorage = findViewById<ConstraintLayout>(R.id.transientStorage)
        val btnCancel = findViewById<AppCompatButton>(R.id.cancelThree)
        val btnStore = findViewById<AppCompatButton>(R.id.store)

        // 레시피 재료 선언
        val recipeWriteMaterialLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout)
        val delete = findViewById<ImageButton>(R.id.delete)
        val divideRectangleBarFive = findViewById<View>(R.id.divideRectangleBarFive)
        val foodName = findViewById<TextView>(R.id.foodName)
        val materialKoreanFood = findViewById<TextView>(R.id.materialKoreanFood)


        // 레시피 대체재료 선언
        val recipeWriteReplaceMaterialLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteReplaceMaterialLayout)
        val replaceMaterialName = findViewById<EditText>(R.id.replaceMaterialName)
        val replaceMaterial = findViewById<EditText>(R.id.replaceMaterial)
        val replaceMaterialDelete = findViewById<ImageButton>(R.id.replaceMaterialDelete)
        val divideRectangleBarTwelve = findViewById<View>(R.id.divideRectangleBarTwelve)
        val replaceMaterialMaterialTwo = findViewById<EditText>(R.id.replaceMaterialMaterialTwo)
        val replaceMaterialTwo = findViewById<EditText>(R.id.replaceMaterialTwo)
        val replaceMaterialDeleteTwo = findViewById<ImageButton>(R.id.replaceMaterialDeleteTwo)
        val divideRectangleBarThirteen = findViewById<View>(R.id.divideRectangleBarThirteen)
        val replaceMaterialfoodName = findViewById<TextView>(R.id.replaceMaterialfoodName)
        val replaceMaterialKoreanFood = findViewById<TextView>(R.id.replaceMaterialKoreanFood)

        // 레시피 처리방법 선언
        val recipeWriteHandlingMethodLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteHandlingMethodLayout)
        val handlingMethodName = findViewById<EditText>(R.id.handlingMethodName)
        val handlingMethod = findViewById<EditText>(R.id.handlingMethod)
        val handlingMethodDelete = findViewById<ImageButton>(R.id.handlingMethodDelete)
        val divideRectangleBarFifteen = findViewById<View>(R.id.divideRectangleBarFifteen)
        val handlingMethodMaterialTwo = findViewById<EditText>(R.id.handlingMethodMaterialTwo)
        val handlingMethodTwo = findViewById<EditText>(R.id.handlingMethodTwo)
        val handlingMethodDeleteTwo = findViewById<ImageButton>(R.id.handlingMethodDeleteTwo)
        val divideRectangleBarSixteen = findViewById<View>(R.id.divideRectangleBarSixteen)
        val handlingMethodFoodName = findViewById<TextView>(R.id.handlingMethodFoodName)
        val handlingMethodKoreanFood = findViewById<TextView>(R.id.handlingMethodKoreanFood)
        // 레시피 조리순서 선언
        val recipeWriteCookOrderLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout)
        val cookOrderAddButton = findViewById<ConstraintLayout>(R.id.cookOrderAddButton)
        val cookOrderTimer = findViewById<ConstraintLayout>(R.id.cookOrderTimer)
        val cookOrderTapBar = findViewById<ConstraintLayout>(R.id.cookOrderTapBar)
        imageContainer = findViewById(R.id.imageContainer)
        representImageContainer = findViewById(R.id.representImageContainer)
        val cookOrderRecipeWrite = findViewById<EditText>(R.id.cookOrderRecipeWrite)
        val stepLittleOne = findViewById<TextView>(R.id.stepLittleOne)
        val camera = findViewById<ImageButton>(R.id.camera)
        val timerAdd = findViewById<AppCompatButton>(R.id.timerAdd)
        val endFixButton = findViewById<AppCompatButton>(R.id.endFixButton)
        val stepAddButton = findViewById<AppCompatButton>(R.id.stepAddFixButton)
        val contentAdd = findViewById<AppCompatButton>(R.id.contentAdd)
        val timer = findViewById<TextView>(R.id.timer)
        val hour = findViewById<EditText>(R.id.hour)
        val time = findViewById<TextView>(R.id.time)
        val minute = findViewById<EditText>(R.id.minute)
        val start = findViewById<TextView>(R.id.start)
        val middle = findViewById<TextView>(R.id.middle)
        val timerDelete = findViewById<TextView>(R.id.timerDelete)
        val stepOne = findViewById<TextView>(R.id.stepOne)
        hourEditText = findViewById(R.id.hour)
        minuteEditText = findViewById(R.id.minute)
        startTextView = findViewById(R.id.start)
        timeSeparator = findViewById(R.id.time)
        deleteTextView = findViewById(R.id.timerDelete)
        val cookOrderFoodName = findViewById<TextView>(R.id.cookOrderFoodName)
        val cookOrderKoreanFood = findViewById<TextView>(R.id.cookOrderKoreanFood)


        // 레시피 세부설정 선언
        val recipeWriteDetailSettleLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        val requiredTimeAndTag = findViewById<ConstraintLayout>(R.id.requiredTimeAndTag)
        val detailSettleCamera = findViewById<ImageButton>(R.id.detailSettleCamera)
        val elementaryLevel = findViewById<TextView>(R.id.elementaryLevel)
        val detailSettleDownArrow = findViewById<ImageButton>(R.id.detailSettleDownArrow)
        val zero = findViewById<EditText>(R.id.zero)
        val halfHour = findViewById<EditText>(R.id.halfHour)
        val detailSettleRecipeTitleWrite = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite)
        val detailSettleFoodName = findViewById<TextView>(R.id.detailSettleFoodName)
        val detailSettleKoreanFood = findViewById<TextView>(R.id.detailSettleKoreanFood)

        // 레시피 작성한 내용 선언
        val contentCheckLayout = findViewById<ConstraintLayout>(R.id.contentCheckLayout)
        val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
        val contentCheckTapBar = findViewById<ConstraintLayout>(R.id.contentCheckTapBar)
        val shareFixButton = findViewById<AppCompatButton>(R.id.shareFixButton)
        val registerFixButton = findViewById<AppCompatButton>(R.id.registerFixButton)
        val uncheck = findViewById<ImageButton>(R.id.uncheck)
        val uncheckTwo = findViewById<ImageButton>(R.id.uncheckTwo)
        val cancel = findViewById<AppCompatButton>(R.id.cancel)
        val settle = findViewById<AppCompatButton>(R.id.settle)
        val cancelTwo = findViewById<AppCompatButton>(R.id.cancelTwo)
        val register = findViewById<AppCompatButton>(R.id.register)

        // 레시피 위 탭바 선언
        val one = findViewById<TextView>(R.id.one)
        val two = findViewById<TextView>(R.id.two)
        val three = findViewById<TextView>(R.id.three)
        val four = findViewById<TextView>(R.id.four)
        val five = findViewById<TextView>(R.id.five)
        val six = findViewById<TextView>(R.id.six)

        // 레시피 레이아웃 선언
        layoutList = listOf(
            recipeWriteTitleLayout,
            recipeWriteMaterialLayout,
            recipeWriteReplaceMaterialLayout,
            recipeWriteHandlingMethodLayout,
            recipeWriteCookOrderLayout,
            recipeWriteDetailSettleLayout
        )

        currentLayout = recipeWriteTitleLayout
        showOnlyLayout(currentLayout)

        // 레시피 탭바와 레이아웃 1:1
        one.setOnClickListener { changeLayout(recipeWriteTitleLayout) }
        two.setOnClickListener { changeLayout(recipeWriteMaterialLayout) }
        three.setOnClickListener { changeLayout(recipeWriteReplaceMaterialLayout) }
        four.setOnClickListener { changeLayout(recipeWriteHandlingMethodLayout) }
        five.setOnClickListener { changeLayout(recipeWriteCookOrderLayout) }
        six.setOnClickListener { changeLayout(recipeWriteDetailSettleLayout) }

        progressBars = listOf(
            findViewById(R.id.barOne),
            findViewById(R.id.barTwo),
            findViewById(R.id.barThree),
            findViewById(R.id.barFour),
            findViewById(R.id.barFive),
            findViewById(R.id.barSix)
        )

        draftId = intent.getLongExtra("draftId", -1L).takeIf { it > 0 }

        val token = App.prefs.token ?: ""
        draftId?.let { id ->
            RetrofitInstance.apiService.getMyDraftById("Bearer $token", id)
                .enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                    override fun onResponse(
                        call: retrofit2.Call<RecipeDraftDto>,
                        response: retrofit2.Response<RecipeDraftDto>
                    ) {
                        if (!response.isSuccessful) return
                        val dto = response.body() ?: return
                        bindDraft(dto)
                    }
                    override fun onFailure(call: retrofit2.Call<RecipeDraftDto>, t: Throwable) { }
                })
        }

        // 레시피 이전으로 버튼 클릭시 이전 화면으로 이동
        beforeButton.setOnClickListener {
            // 타이틀 화면일 때 → RecipeWriteMainActivity로 이동
            if (currentLayout.id == R.id.recipeWriteTitleLayout) {
                val intent = Intent(this, RecipeWriteMain::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
                return@setOnClickListener
            }

            // 일반적인 이전 이동 처리
            if (layoutHistory.isNotEmpty()) {
                val previousLayout = layoutHistory.pop()
                val prevIndex = layoutList.indexOf(previousLayout).coerceAtLeast(0)
                switchTo(prevIndex, pushHistory = false)
            } else {
                val prev = (selectedIndex - 1).coerceAtLeast(0)
                if (prev != selectedIndex) switchTo(prev, pushHistory = false)
            }
        }

        fun updateMaterialList(
            materialContainer: LinearLayout,
            ingredients: List<Pair<String, String>>
        ) {
            materialContainer.removeAllViews() // 기존 뷰 제거

            for ((materialName, quantity) in ingredients) {
                val itemLayout = LinearLayout(materialContainer.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(15, 10, 0, 0) }
                }

                val materialTextView = TextView(materialContainer.context).apply {
                    text = materialName
                    textSize = 13f
                    setTextColor(Color.parseColor("#2B2B2B"))
                }

                val quantityTextView = TextView(materialContainer.context).apply {
                    text = quantity
                    textSize = 13f
                    setTextColor(Color.parseColor("#2B2B2B"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(115, 0, 0, 0) }
                }

                itemLayout.addView(materialTextView)
                itemLayout.addView(quantityTextView)
                materialContainer.addView(itemLayout)
            }
        }

        // 계속하기 버튼 클릭시 다음 화면으로 이동
        continueButton.setOnClickListener {
            val next = (selectedIndex + 1).coerceAtMost(tabCompleted.lastIndex)
            if (next != selectedIndex) switchTo(next, pushHistory = true)
            val currentIndex = layoutList.indexOf(currentLayout)

            // 마지막 화면이 세부설정이면 내용 확인 화면으로
            if (currentLayout.id == R.id.recipeWriteDetailSettleLayout) {
                // 레이아웃 토글
                findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout).visibility = View.GONE
                findViewById<View>(R.id.cookOrderTapBar).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.contentCheckTapBar).visibility = View.VISIBLE

                // 대표 이미지
                val representativeImage = findViewById<ImageView>(R.id.representativeImage)
                if (mainImageUrl.isNotBlank()) {
                    val fullImageUrl = RetrofitInstance.BASE_URL + mainImageUrl.trim()
                    Glide.with(this).load(fullImageUrl).into(representativeImage)
                } else {
                    representativeImage.setImageDrawable(null)
                }

                // 기본 값들
                val categoryText = koreanFood.text.toString()
                val recipeTitle  = recipeTitleWrite.text.toString()

                val ingredients = mutableListOf<Pair<String, String>>()
                for (i in 0 until materialContainer.childCount) {
                    val row = materialContainer.getChildAt(i)
                    val nameTv = row.findViewById<TextView>(R.id.tvMaterialName)
                    val qtyEt  = row.findViewById<EditText>(R.id.etMeasuring)

                    val name = nameTv?.text?.toString()?.trim().orEmpty()
                    val qty  = qtyEt?.text?.toString()?.trim().orEmpty()

                    if (name.isNotBlank() && qty.isNotBlank()) {
                        ingredients += name to qty
                    }
                }

                val ingredientsForRequest = ingredients.map { (name, qty) ->
                    // 이름으로 전체 재료 목록(allIngredients)에서 원본 데이터(id 포함) 찾기
                    val originalIngredient = allIngredients.find { it.nameKo == name }
                    val ingredientId = originalIngredient?.id
                    Log.d("RecipeRequest", "재료 매핑: name=$name, id=$ingredientId, qty=$qty")
                    // 서버가 요구하는 MasterIngredient 모델로 변환
                    MasterIngredient(
                        id = ingredientId,
                        name = name,
                        amount = qty
                    )
                }

                val filteredIngredients = ingredients.filter { it.first.isNotBlank() && it.second.isNotBlank() }
                // 입력 폼 쪽 미리보기 업데이트(선택)
                updateMaterialList(materialContainer, filteredIngredients)

                // 처리방법(문자열) 수집
                val handlingMethods = listOf(
                    "${handlingMethodName.text.toString().trim()} : ${handlingMethod.text.toString().trim()}",
                    "${handlingMethodMaterialTwo.text.toString().trim()} : ${handlingMethodTwo.text.toString().trim()}"
                ).filter { it.isNotBlank() }

                // 조리 순서/타이머
                val cookingSteps = saveRecipeSteps()
                val cookingHour   = zero.text.toString().toIntOrNull() ?: 0
                val cookingMinute = halfHour.text.toString().toIntOrNull() ?: 0

                // 태그
                val recipeTag = detailSettleRecipeTitleWrite.text.toString()

                // 내용 확인 화면 텍스트 채우기
                findViewById<TextView>(R.id.contentCheckFoodName).text = recipeTitle
                findViewById<TextView>(R.id.contentCheckKoreanFood).text = categoryText
                findViewById<TextView>(R.id.contentCheckBeginningLevel).text = elementaryLevel.text
                findViewById<TextView>(R.id.foodNameTwo).text = recipeTag
                findViewById<TextView>(R.id.contentCheckZero).text = cookingHour.toString()
                findViewById<TextView>(R.id.contentCheckHalfHour).text = cookingMinute.toString()

                // 직렬화용 값들
                val totalCookingTime = cookingHour * 60 + cookingMinute
                val difficulty  = elementaryLevel.text.toString()
                val categoryEnum = mapCategoryToEnum(categoryText)
                val gson = Gson()

                // 대체재료/처리방법 Pair (미리보기용)
                val altPairs: List<Pair<String, String>> = collectAlternativePairs()
                val handlingPairs: List<Pair<String, String>> =
                    handlingMethods.mapNotNull { s ->
                        val idx = s.indexOf(" : ")
                        if (idx <= 0) null else s.substring(0, idx).trim() to s.substring(idx + 3).trim()
                    }

                // RecipeRequest 생성
                recipe = RecipeRequest(
                    title = recipeTitle,
                    category = categoryEnum,
                    ingredients = ingredientsForRequest,
                    alternativeIngredients = gson.toJson(altPairs.map { (o, r) -> Ingredient(o, r) }),
                    // 서버 계약을 따르기 위해 처리방법은 기존 문자열 리스트 그대로 전송
                    handlingMethods = gson.toJson(handlingMethods),
                    cookingSteps = gson.toJson(
                        cookingSteps.mapIndexed { index, stepText ->
                            val stepNo = index + 1
                            val (h, m) = stepTimerMap[stepNo] ?: (0 to 0)
                            val totalSec = h * 3600 + m * 60
                            val imageUrl = stepImages[stepNo] ?: ""
                            CookingStep(
                                step = stepNo,
                                description = stepText,
                                mediaUrl = imageUrl,
                                mediaType = "IMAGE",
                                timeInSeconds = totalSec
                            )
                        }
                    ),
                    mainImageUrl = mainImageUrl,
                    difficulty = difficulty,
                    tags = recipeTag,
                    cookingTime = totalCookingTime,
                    servings = 2,
                    isPublic = true
                )
                val json = gson.toJson(recipe)
                Log.d("RecipeRequest", "보낼 JSON: $json")
                // 내용 확인 화면의 재료/대체재료/처리방법 표시
                updateMaterialListView(
                    findViewById(R.id.materialList),
                    filteredIngredients,
                    altPairs,
                    handlingPairs
                )

                // 내용 확인 화면의 조리 순서 표시
                recipe?.let { local ->
                    val type = object : TypeToken<List<CookingStep>>() {}.type
                    val list: List<CookingStep> = gson.fromJson(local.cookingSteps, type)
                    addCookingSteps(this, list)
                }

                selectedIndex = currentIndex
                checkTabs()
                return@setOnClickListener
            }
        }

        // 임시저장 버튼 클릭시 여부 나타남
        temporaryStorageBtn.setOnClickListener {
            transientStorageLayout.visibility = View.VISIBLE
        }

        // 임시저장 취소 클릭시 임시저장 여부 없어짐
        btnCancel.setOnClickListener {
            transientStorage.visibility = View.GONE
        }

        // 임시저장 저장 클릭시 홈으로 이동
        btnStore.setOnClickListener {
            lifecycleScope.launch {
                val token = App.prefs.token ?: ""
                if (token.isBlank()) {
                    Toast.makeText(this@RecipeWriteImageActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                if (isSavingDraft) return@launch
                isSavingDraft = true

                // ⭐ 이미지 업로드 대기 (진행 중이면 토스트 + 대기)
                if (uploadsInFlight > 0) {
                    Toast.makeText(this@RecipeWriteImageActivity, "이미지 업로드 중… 잠시만요", Toast.LENGTH_SHORT).show()
                    awaitUploadsIfAny()   // <- 클래스 내부에 suspend fun 으로 이미 추가했다고 가정
                }

                val dto = buildDraftDto()  // ⭐ 이제 cookingSteps는 객체 배열 JSON으로 들어가야 함(앞서 수정한대로)

                if (draftId == null) {
                    // 최초 임시저장 - 생성
                    RetrofitInstance.apiService.createDraft("Bearer $token", dto)
                        .enqueue(object : retrofit2.Callback<RecipeCreateResponse> {
                            override fun onResponse(
                                call: retrofit2.Call<RecipeCreateResponse>,
                                response: retrofit2.Response<RecipeCreateResponse>
                            ) {
                                isSavingDraft = false
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    draftId = body?.recipeId
                                    Toast.makeText(this@RecipeWriteImageActivity, "임시저장 완료", Toast.LENGTH_SHORT).show()

                                    // ✅ 오버레이 닫기
                                    findViewById<ConstraintLayout>(R.id.transientStorage)?.visibility = View.GONE
                                    findViewById<ConstraintLayout>(R.id.transientStorage)?.visibility = View.GONE
                                    // 위 두 줄 중 하나만 쓰는 프로젝트도 있음. 기존 변수명이 있다면 그걸로 대체해도 OK:
                                    // transientStorageLayout.visibility = View.GONE

                                    // ✅ 이전 화면에 결과 전달(선택 사항) + 뒤로
                                    setResult(RESULT_OK, Intent().apply {
                                        putExtra("draftId", draftId)
                                        putExtra("justSaved", true)
                                    })
                                    finish()
                                } else {
                                    Toast.makeText(this@RecipeWriteImageActivity, "임시저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: retrofit2.Call<RecipeCreateResponse>, t: Throwable) {
                                isSavingDraft = false
                                Toast.makeText(this@RecipeWriteImageActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                } else {
                    // 업데이트
                    RetrofitInstance.apiService.updateDraft("Bearer $token", draftId!!, dto)
                        .enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                            override fun onResponse(
                                call: retrofit2.Call<RecipeDraftDto>,
                                response: retrofit2.Response<RecipeDraftDto>
                            ) {
                                isSavingDraft = false
                                if (response.isSuccessful) {
                                    // 서버가 recipeId를 돌려주면 최신값으로 보정
                                    draftId = response.body()?.recipeId ?: draftId
                                    Toast.makeText(this@RecipeWriteImageActivity, "임시저장 업데이트 완료", Toast.LENGTH_SHORT).show()

                                    // ✅ 오버레이 닫기
                                    findViewById<ConstraintLayout>(R.id.transientStorage)?.visibility = View.GONE
                                    // 또는: transientStorageLayout.visibility = View.GONE

                                    // ✅ 이전 화면에 결과 전달(선택 사항) + 뒤로
                                    setResult(RESULT_OK, Intent().apply {
                                        putExtra("draftId", draftId)
                                        putExtra("justSaved", true)
                                    })
                                    finish()
                                } else {
                                    Toast.makeText(this@RecipeWriteImageActivity, "업데이트 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: retrofit2.Call<RecipeDraftDto>, t: Throwable) {
                                isSavingDraft = false
                                Toast.makeText(this@RecipeWriteImageActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        }


        // 레시피 탭바와 바 선언
        textViewList = listOf(one, two, three, four, five, six)
        underlineBar = findViewById(R.id.divideRectangleBarTwentythree)
        underlineBar.post {
            val textView = findViewById<TextView>(R.id.one)
            val targetX = textView.x + (textView.width / 2) - (indicatorBar.width / 2)
            indicatorBar.x = targetX
        }

        textViewList.forEachIndexed { i, tv ->
            tv.setOnClickListener {
                if (selectedIndex == i) return@setOnClickListener
                onTabSwitched(i)
            }
        }

        updateSelectedTab(one)
        moveUnderlineBar(one)

        recipeTitleWrite.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                foodName.text = text
                replaceMaterialfoodName.text = text
                handlingMethodFoodName.text = text
                cookOrderFoodName.text = text
                detailSettleFoodName.text = text
                checkTabs()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 레시피 타이틀 드롭다운 버튼 클릭 시 열기/닫기 토글
        downArrow.setOnClickListener {
            val popup = PopupMenu(this, it)

            val categories = listOf("전체", "한식", "양식", "일식", "중식", "채식", "간식", "안주", "반찬", "기타")
            categories.forEach { category ->
                popup.menu.add(category)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                koreanFood.text = menuItem.title
                koreanFood.setTextColor(Color.parseColor("#2B2B2B")) // 선택 시 진한 텍스트 색상으로 변경
                checkTabs()
                checkAndUpdateContinueButton()
                true
            }

            popup.show()
        }

        // 한식 바뀜
        fun updateKoreanFoodTextViews(text: String) {
            materialKoreanFood.text = text
            replaceMaterialKoreanFood.text = text
            handlingMethodKoreanFood.text = text
            cookOrderKoreanFood.text = text
            detailSettleKoreanFood.text = text
        }

        // koreanFood 값이 변경될 때 자동 반영
        koreanFood.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateKoreanFoodTextViews(koreanFood.text.toString())
        }



        //대체재료 채워지면 계속하기 바뀜
        replaceMaterialName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        replaceMaterial.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //처리방법 채워지면 계속하기 바뀜
        handlingMethodName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        handlingMethod.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //조리순서 채워지면 끝내기 바뀜
        val stepEditText = findViewById<EditText>(R.id.cookOrderRecipeWrite)

        stepEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkCookOrderAndUpdateEndButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        //세부설정 채워지면 계속하기 바뀜
        val cookingTimeEditText = findViewById<EditText>(R.id.zero)
        val tagEditText = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite)

        cookingTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkAndUpdateContinueButton()
                checkTabs()}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        tagEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkAndUpdateContinueButton()
                checkTabs()}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 난이도 선택은 메뉴 선택 후 호출
        elementaryLevel.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            checkAndUpdateContinueButton()
        }

        // 레시피 대체재료 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        replaceMaterialDeleteTwo.setOnClickListener {
            replaceMaterialMaterialTwo.visibility = View.GONE
            replaceMaterialTwo.visibility = View.GONE
            replaceMaterialDeleteTwo.visibility = ImageButton.GONE
            divideRectangleBarThirteen.visibility = View.GONE
        }

        // 레시피 처리방법 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        handlingMethodDeleteTwo.setOnClickListener {
            handlingMethodMaterialTwo.visibility = View.GONE
            handlingMethodTwo.visibility = View.GONE
            handlingMethodDeleteTwo.visibility = ImageButton.GONE
            divideRectangleBarSixteen.visibility = View.GONE
        }

        //조리 순서 생성될때 초기화
        stepCount = 1
        currentStep = 1
        currentSubStep = 1
        recipeStepCount = 1

        // 레시피 조리순서 내용 추가하기 눌렀을때 내용 추가
        cookOrderRecipeContainer = findViewById(R.id.cookOrderRecipeContainer) // 레이아웃 ID

        contentAdd.setOnClickListener {
            if (recipeStepCount < 10) {
                recipeStepCount++

                // ✅ 현재 Step의 SubStep 가져오기 (없으면 2부터 시작)
                val currentSubStep = stepRecipeCountMap[currentStep] ?: 2

                addRecipeStep(currentStep, currentSubStep)

                // ✅ Step별 SubStep 증가
                stepRecipeCountMap[currentStep] = currentSubStep + 1
            }
        }

        // 레시피 조리순서 step 추가
        stepContainer = findViewById(R.id.stepContainer) // onCreate에서 초기화

        stepAddButton.setOnClickListener {
            currentStep++  // 새로운 Step 추가 시 Step 번호 증가
            stepRecipeCountMap[currentStep] = 2 // 새로운 Step의 첫 번째 SubStep을 2로 설정
            addNewStep(currentStep)
        }



        // 레시피 조리순서 카메라 버튼 클릭 시 갤러리 열기
        camera.setOnClickListener {
            pickImageLauncherForCamera.launch("image/*")
        }

        // stepCamera용 런처 (조리 순서 이미지)
        pickImageLauncherForStepCamera =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                        putExtra("imageUri", it.toString())
                        putExtra("isStepImage", true)
                    }
                    startActivityForResult(intent, EDIT_IMAGE_REQUEST_CODE)
                }
            }

        // 레시피 조리순서 새로운 STEP을 담을 ConstraintLayout 생성
        val newStepLayout = LayoutInflater.from(this).inflate(R.layout.item_step, stepContainer, false)
        val stepCamera = newStepLayout.findViewById<ImageButton>(R.id.stepCamera)

        // 레시피 조리순서 카메라 버튼 클릭 시 갤러리 열기
        stepCamera.setOnClickListener {
            // 갤러리에서 이미지 선택하기
            pickImageLauncherForStepCamera.launch("image/*")
        }

        val linearLayout2 = findViewById<LinearLayout>(R.id.linearLayout2)
        val cookOrderStoreButton = findViewById<ConstraintLayout>(R.id.cookOrderStoreButton)

        // 레시피 조리순서 타이머 버튼 클릭시
        timerAdd.setOnClickListener {
            val timerLayout = LayoutInflater.from(this).inflate(R.layout.timer_step_layout, stepContainer, false)

            val hourPicker = timerLayout.findViewById<NumberPicker>(R.id.numberPicker1)
            val minutePicker = timerLayout.findViewById<NumberPicker>(R.id.numberPicker2)
            val storeBtn = timerLayout.findViewById<AppCompatButton>(R.id.storeBtn)

            hourPicker.minValue = 0
            hourPicker.maxValue = 24
            minutePicker.minValue = 0
            minutePicker.maxValue = 59
            minutePicker.setFormatter { i -> String.format("%02d", i) }

            storeBtn.setOnClickListener {
                val hour = hourPicker.value
                val minute = minutePicker.value
                stepTimerMap[currentStep] = hour to minute
                Toast.makeText(this, "STEP $currentStep 타이머 저장됨 ($hour:$minute)", Toast.LENGTH_SHORT).show()
            }

            stepContainer.addView(timerLayout)
        }


        // NumberPicker 초기화
        val hourPicker = findViewById<NumberPicker>(R.id.numberPicker1)
        val minutePicker = findViewById<NumberPicker>(R.id.numberPicker2)
        val storeBtn = findViewById<AppCompatButton>(R.id.storeBtn)

        // 시 (0~23)
        hourPicker.minValue = 0
        hourPicker.maxValue = 24
        hourPicker.wrapSelectorWheel = true

        // 분 (0~59)
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.wrapSelectorWheel = true

        // 00~59 형식 맞추기
        minutePicker.setFormatter { i -> String.format("%02d", i) }

        storeBtn.setOnClickListener {
            // NumberPicker에서 선택한 값 가져오기
            val selectedHour = hourPicker.value
            val selectedMinute = minutePicker.value

            // EditText에 설정된 형식으로 반영
            hourEditText.setText(String.format("%02d", selectedHour))
            minuteEditText.setText(String.format("%02d", selectedMinute))

            // View 변경 (기존 버튼 숨기고 새로운 레이아웃 표시)
            cookOrderStoreButton.visibility = View.GONE
            linearLayout2.visibility = View.GONE
            cookOrderTimer.visibility = View.VISIBLE
        }

        // 레시피 조리순서 시작 버튼 클릭 이벤트
        startTextView.setOnClickListener {
            startTimer()
        }

        // 레시피 조리순서 삭제 버튼 클릭 이벤트 (타이머 초기화)
        deleteTextView.setOnClickListener {
            resetTimer()
        }

        // 레시피 조리순서 끝내기 버튼 클릭시
        endFixButton.setOnClickListener{
            layoutHistory.push(currentLayout)

            val detailSettleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
            showOnlyLayout(detailSettleLayout)

            updateSelectedTab(six)
            moveUnderlineBar(six)
        }
        // 레시피 조리순서 다른 레이아웃 목록을 먼저 선언
        val otherLayouts = listOf(
            findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteReplaceMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteHandlingMethodLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        )

        // 레시피 조리순서 초기 상태: cookOrderTapBar 숨김
        cookOrderTapBar.visibility = View.GONE

        // 레시피 조리순서 조리순서 화면이 나타날 때 cookOrderTapBar 보이게 설정
        recipeWriteCookOrderLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (recipeWriteCookOrderLayout.visibility == View.VISIBLE) {
                cookOrderTapBar.visibility = View.VISIBLE
            }
        }

        // 레시피 조리순서 다른 화면이 나타나면 조리순서 화면을 숨기고 cookOrderTapBar도 숨김
        otherLayouts.forEach { layout ->
            layout.viewTreeObserver.addOnGlobalLayoutListener {
                if (layout.visibility == View.VISIBLE) {
                    recipeWriteCookOrderLayout.visibility = View.GONE // 겹치는 문제 해결
                    cookOrderTapBar.visibility = View.GONE
                }
            }
        }

        // 레시피 세부설정 카메라 버튼 클릭 시 갤러리 열기
        detailSettleCamera.setOnClickListener {
            isPickingRepresentImage = true
            pickImageLauncherForDetailSettle.launch("image/*")
        }

        // 세부설정 난이도 열기
        detailSettleDownArrow.setOnClickListener {
            val popup = PopupMenu(this, it)

            val categories = listOf("초급", "중급", "상급")
            categories.forEach { category ->
                popup.menu.add(category)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                elementaryLevel.text = menuItem.title
                elementaryLevel.setTextColor(Color.parseColor("#2B2B2B")) // 선택 시 진한 텍스트 색상으로 변경
                checkTabs()
                checkAndUpdateContinueButton()
                true
            }

            popup.show()
        }

        // 레시피 작성내용 확인 공유 설정 클릭시 상자 나타남
        shareFixButton.setOnClickListener {
            shareSettle.visibility = View.VISIBLE
        }

        // 레시피 작성내용 확인 공유 상자 선택 클릭
        var isCheckedOne = false
        var isCheckedTwo = false

        uncheck.setOnClickListener {
            isCheckedOne = !isCheckedOne
            uncheck.setImageResource(if (isCheckedOne) R.drawable.ic_check else R.drawable.ic_uncheck)
            isCheckedTwo = false
            uncheckTwo.setImageResource(R.drawable.ic_uncheck)
            isPublic = true
            recipe = recipe?.copy(isPublic = true)
        }

        uncheckTwo.setOnClickListener {
            isCheckedTwo = !isCheckedTwo
            uncheckTwo.setImageResource(if (isCheckedTwo) R.drawable.ic_check else R.drawable.ic_uncheck)
            isCheckedOne = false
            uncheck.setImageResource(R.drawable.ic_uncheck)
            isPublic = false
            recipe = recipe?.copy(isPublic = false)
        }

        // 레시피 작성내용 취소 버튼 클릭 시 shareSettle을 숨김
        cancel.setOnClickListener {
            shareSettle.visibility = View.GONE
        }

        // 레시피 작성내용 설정 버튼 클릭 시 shareSettle을 숨기고 recipeRegister를 표시
        settle.setOnClickListener {
            shareSettle.visibility = View.GONE
            recipeRegister.visibility = View.VISIBLE
        }

        // 레시피 작성내용 취소 버튼 클릭 시 shareSettle을 숨김
        cancelTwo.setOnClickListener {
            recipeRegister.visibility = View.GONE
        }

        // 레시피 등록한 레시피 확인 (작은 등록하기 클릭시 화면 이동)
        register.setOnClickListener {
            val token = App.prefs.token ?: ""
            if (token.isBlank()) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (draftId != null) {
                // 최신 내용 저장 후 발행
                saveDraftThenPublish(isPublic)
            } else {
                // 초안 없이 바로 신규 업로드 경로
                if (recipe != null) {
                    val uploadRecipe = recipe!!.copy(isPublic = isPublic)
                    sendRecipeToServer(uploadRecipe, onSuccess = { recipeId ->
                        val intent = Intent(this, RecipeSeeMainActivity::class.java)
                        intent.putExtra("recipeId", recipeId)
                        startActivity(intent)
                        finish()
                    }, onFailure = {
                        Toast.makeText(this, "레시피 업로드 실패1", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Toast.makeText(this, "레시피를 먼저 작성해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener {
            val token = App.prefs.token ?: ""
            if (token.isBlank()) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (draftId != null) {
                // 초안 기반: 저장 → 발행
                saveDraftThenPublish(isPublic)
            } else {
                // 신규 생성 경로
                if (recipe != null) {
                    val uploadRecipe = recipe!!.copy(isPublic = isPublic)
                    sendRecipeToServer(uploadRecipe, onSuccess = { recipeId ->
                        val intent = Intent(this, RecipeSeeMainActivity::class.java)
                        intent.putExtra("recipeId", recipeId)
                        startActivity(intent)
                        finish()
                    }, onFailure = {
                        Toast.makeText(this, "레시피 업로드 실패2", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Toast.makeText(this, "레시피를 먼저 작성해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<ImageButton>(R.id.backArrow).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.registerRecipeBackArrow).setOnClickListener {
            finish()
        }

        val recipeTitleEditText = findViewById<EditText>(R.id.recipeTitleWrite)
        val koreanFoodTextView = findViewById<TextView>(R.id.koreanFood)

        // 제목 입력 감지
        recipeTitleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // "한식"이 아닌 다른 텍스트로 변경되면 호출되게 (예: 다이얼로그에서 선택 시)
        koreanFoodTextView.setOnClickListener {
            // 예: 선택 다이얼로그 띄우고 결과 텍스트 설정 후 호출
            // koreanFoodTextView.text = "중식" 같은 거 하고 나서
            checkAndUpdateContinueButton()
        }

        //탭바로 이동해도 채워져있으면 계속하기 버튼 바껴져 있음
        checkAndUpdateContinueButton()

    }

    private fun mapCategoryToEnum(category: String?): String = when (category) {
        "한식" -> "koreaFood"
        "양식" -> "westernFood"
        "일식" -> "japaneseFood"
        "중식" -> "chineseFood"
        "채식" -> "vegetarianDiet"
        "간식" -> "snack"
        "안주" -> "alcoholSnack"
        "반찬" -> "sideDish"
        "기타" -> "etc"
        else -> "etc"
    }

    private fun collectAlternativePairs(): List<Pair<String,String>> {
        val pairs = mutableListOf<Pair<String,String>>()
        fun add(origId: Int, replId: Int) {
            val o = findViewById<EditText>(origId)?.text?.toString()?.trim().orEmpty()
            val r = findViewById<EditText>(replId)?.text?.toString()?.trim().orEmpty()
            if (o.isNotEmpty() && r.isNotEmpty()) pairs += o to r
        }
        // 정적 2칸
        add(R.id.replaceMaterialName, R.id.replaceMaterial)
        add(R.id.replaceMaterialMaterialTwo, R.id.replaceMaterialTwo)
        // 동적 컨테이너
        for (i in 0 until replaceMaterialContainer.childCount) {
            val row = replaceMaterialContainer.getChildAt(i) as? ViewGroup ?: continue
            val o = (row.getChildAt(0) as? EditText)?.text?.toString()?.trim().orEmpty()
            val r = (row.getChildAt(1) as? EditText)?.text?.toString()?.trim().orEmpty()
            if (o.isNotEmpty() && r.isNotEmpty()) pairs += o to r
        }
        return pairs
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        draftId?.let { outState.putLong("draftId", it) }
    }

    private fun buildDraftDto(isDraft: Boolean = true): RecipeDraftDto {
        fun splitAmount(amount: String): Pair<String, String> {
            val t = amount.trim()
            val spaceIdx = t.indexOf(' ')
            return if (spaceIdx > 0) {
                t.substring(0, spaceIdx) to t.substring(spaceIdx + 1)
            } else {
                val m = Regex("^([0-9]+(?:\\.[0-9]+)?)\\s*([^0-9].*)$").find(t)
                if (m != null) m.groupValues[1] to m.groupValues[2] else t to ""
            }
        }

        val gson = Gson()

        // ===== 재료 수집 =====
        val ingredientReqs = mutableListOf<RecipeIngredientReq>()

        // 1) 고정칸
        run {
            val name   = findViewById<EditText>(R.id.material)?.text?.toString()?.trim().orEmpty()
            val qtyTxt = findViewById<EditText>(R.id.measuring)?.text?.toString()?.trim().orEmpty()
            val unitTxt= findViewById<TextView?>(R.id.unit)?.text?.toString()?.trim().orEmpty()

            if (name.isNotBlank() && qtyTxt.isNotBlank()) {
                val id = allIngredients.firstOrNull { it.nameKo == name }?.id
                val (qtyStrFromSplit, unitFromSplit) = splitAmount(qtyTxt)
                val quantity = qtyStrFromSplit.toDoubleOrNull() ?: qtyTxt.toDoubleOrNull()
                val unit = when {
                    unitTxt.isNotBlank() && unitTxt != "단위" -> unitTxt
                    unitFromSplit.isNotBlank() -> unitFromSplit
                    else -> ""
                }.ifBlank { null }

                if (id != null && id > 0 && quantity != null) {
                    ingredientReqs += RecipeIngredientReq(id = id, quantity = quantity)
                }
            }
        }

        // 2) 동적칸 (materialContainer의 각 행: 0=이름, 1=수량)
        for (i in 0 until materialContainer.childCount) {
            val row = materialContainer.getChildAt(i) as? ViewGroup ?: continue
            val name = (row.getChildAtOrNull(0) as? TextView)?.text?.toString()?.trim()
                ?: (row.getChildAtOrNull(0) as? EditText)?.text?.toString()?.trim().orEmpty()
            val qtyTxt = (row.getChildAtOrNull(1) as? EditText)?.text?.toString()?.trim().orEmpty()

            if (name.isBlank() || qtyTxt.isBlank()) continue

            val id = allIngredients.firstOrNull { it.nameKo == name }?.id
            val (qtyStr, unitStr) = splitAmount(qtyTxt)
            val quantity = qtyStr.toDoubleOrNull()

            if (id != null && id > 0 && quantity != null) {
                ingredientReqs += RecipeIngredientReq(
                    id = id,
                    quantity = quantity
                )
            }
        }

        // ===== 대체재료 / 처리방법 → JSON 문자열 =====
        val altPairs = listOf(
            findViewById<EditText>(R.id.replaceMaterialName)?.text?.toString()?.trim().orEmpty() to
                    findViewById<EditText>(R.id.replaceMaterial)?.text?.toString()?.trim().orEmpty(),
            findViewById<EditText>(R.id.replaceMaterialMaterialTwo)?.text?.toString()?.trim().orEmpty() to
                    findViewById<EditText>(R.id.replaceMaterialTwo)?.text?.toString()?.trim().orEmpty()
        ).filter { it.first.isNotBlank() || it.second.isNotBlank() }

        val handlingPairs = listOf(
            findViewById<EditText>(R.id.handlingMethodName)?.text?.toString()?.trim().orEmpty() to
                    findViewById<EditText>(R.id.handlingMethod)?.text?.toString()?.trim().orEmpty(),
            findViewById<EditText>(R.id.handlingMethodMaterialTwo)?.text?.toString()?.trim().orEmpty() to
                    findViewById<EditText>(R.id.handlingMethodTwo)?.text?.toString()?.trim().orEmpty()
        ).filter { it.first.isNotBlank() || it.second.isNotBlank() }

        val alternativeIngredientsJson = gson.toJson(
            altPairs.map { com.example.test.model.Ingredient(it.first, it.second) }
        )
        val handlingMethodsJson = gson.toJson(
            handlingPairs.map { "${it.first} : ${it.second}" }
        )

        // collectCookingStepsJson() 의존성 제거 → saveRecipeSteps() 결과를 JSON으로
        val stepsJson = gson.toJson(buildStepObjectsFromCurrentUI())

        // ===== 제목/카테고리/난이도/태그/시간 =====
        val title        = findViewById<EditText>(R.id.recipeTitleWrite)?.text?.toString()?.trim().orEmpty()
        val categoryText = findViewById<TextView>(R.id.koreanFood)?.text?.toString()
        val categoryEnum = mapCategoryToEnum(categoryText)
        val difficulty   = findViewById<TextView>(R.id.elementaryLevel)?.text?.toString()?.trim().orEmpty()
        val tags         = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite)?.text?.toString()?.trim().orEmpty()

        val hours   = findViewById<EditText>(R.id.zero)?.text?.toString()?.toIntOrNull() ?: 0
        val minutes = findViewById<EditText>(R.id.halfHour)?.text?.toString()?.toIntOrNull() ?: 0
        val cookingTime = (hours * 60 + minutes).takeIf { it > 0 }

        val ingredientRes: List<RecipeIngredientRes> = ingredientReqs.map { req ->
            RecipeIngredientRes(
                id = req.id ?: -1L,
                name = resolveIngredientNameById(req.id), // 액티비티에 이미 있는 함수
                amount = req.quantity ?: 0.0
            )
        }

        return RecipeDraftDto(
            recipeId = draftId,
            title = title,
            category = categoryEnum.ifEmpty { "etc" },
            ingredients = ingredientRes,               // <- 여기만 바꾸기
            alternativeIngredients = alternativeIngredientsJson,
            handlingMethods = handlingMethodsJson,
            cookingSteps = stepsJson,
            mainImageUrl = mainImageUrl.ifBlank { "" },
            difficulty = difficulty,
            tags = tags,
            cookingTime = cookingTime,
            servings = null,
            isPublic = null,
            videoUrl = "",
            recipeType = "IMAGE",
            isDraft = isDraft
        )
    }

    private fun ViewGroup.getChildAtOrNull(index: Int): View? =
        if (index in 0 until childCount) getChildAt(index) else null

    private fun parseAmountToDouble(raw: String): Double? {
        val s = raw.trim()
        // "1/2" 같은 분수 처리
        val frac = Regex("""^\s*(\d+)\s*/\s*(\d+)\s*$""").matchEntire(s)
        if (frac != null) {
            val a = frac.groupValues[1].toDouble()
            val b = frac.groupValues[2].toDouble()
            return if (b != 0.0) a / b else null
        }
        // 일반 실수 추출
        val number = Regex("""([-+]?\d*\.?\d+)""").find(s)?.groupValues?.getOrNull(1)
        return number?.toDoubleOrNull()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val editedUriStr = data?.getStringExtra("editedImageUri")
            val editedUri = editedUriStr?.let { Uri.parse(it) }

            editedUri?.let { uri ->
                when {
                    // 1) 대표 이미지
                    isPickingRepresentImage -> {
                        displaySelectedImage(uri, representImageContainer)
                        uploadImageToServer(uri) { url ->
                            if (url != null) mainImageUrl = url
                        }
                        isPickingRepresentImage = false
                    }

                    // 2) 스텝 컨테이너가 지정된 경우(스텝 이미지)
                    selectedContainer != null -> {
                        val container = selectedContainer!!
                        displaySelectedImage(uri, container)
                        uploadImageToServer(uri) { url ->
                            if (url != null) {
                                val stepNo = pendingStepForImage ?: currentStep
                                stepImages[stepNo] = url
                            }
                            pendingStepForImage = null
                        }
                        selectedContainer = null
                    }

                    // 3) 기타(폴백) – 이것도 스텝 이미지로 간주
                    else -> {
                        displaySelectedImage(uri, imageContainer)
                        uploadImageToServer(uri) { url ->
                            if (url != null) {
                                val stepNo = pendingStepForImage ?: currentStep
                                stepImages[stepNo] = url
                            }
                            pendingStepForImage = null
                        }
                    }
                }
            }
        }
    }

    // 재료 이름 + 계량이 채워진 행이 1개 이상인지 확인
    private fun hasValidIngredientEntry(): Boolean {
        run {
            val name   = findViewById<EditText?>(R.id.material)?.text?.toString()?.trim().orEmpty()
            val qtyTxt = findViewById<EditText?>(R.id.measuring)?.text?.toString()?.trim().orEmpty()
            val unitTv = findViewById<TextView?>(R.id.unit)?.text?.toString()?.trim()
            val unitOk = (unitTv == null) || (unitTv.isNotBlank() && unitTv != "단위")
            if (name.isNotEmpty() && qtyTxt.isNotEmpty() && unitOk) return true
        }

        for (i in 0 until materialContainer.childCount) {
            val row = materialContainer.getChildAt(i)
            val name = row.findViewById<TextView?>(R.id.tvMaterialName)
                ?.text?.toString()?.trim().orEmpty()
            val qty  = row.findViewById<EditText?>(R.id.etMeasuring)
                ?.text?.toString()?.trim().orEmpty()

            val unitText = row.findViewById<TextView?>(R.id.unit)
                ?.text?.toString()?.trim()
            val unitOk = (unitText == null) || (unitText.isNotBlank() && unitText != "단위")

            if (name.isNotEmpty() && qty.isNotEmpty() && unitOk) {
                return true
            }
        }
        return false
    }

    private fun checkTabs() {
        // ===== 1번 탭: 타이틀 =====
        val titleView = findViewById<EditText?>(R.id.recipeTitleWrite)
        val categoryView = findViewById<TextView?>(R.id.koreanFood)

        if (titleView != null && categoryView != null) {
            val hasTitle = titleView.text.isNotBlank()
            val hasCategory = categoryView.text.isNotBlank() && categoryView.text != "카테고리 선택"
            tabCompleted[0] = hasTitle && hasCategory
        }

        // ===== 2번 탭: 재료 =====
        tabCompleted[1] = hasValidIngredientEntry()

        // ===== 3번 탭: 대체재료 =====
        val replaceMaterialNameView = findViewById<EditText?>(R.id.replaceMaterialName)
        val replaceMaterialView = findViewById<EditText?>(R.id.replaceMaterial)
        if (replaceMaterialNameView != null && replaceMaterialView != null) {
            val hasReplaceName = replaceMaterialNameView.text.isNotBlank()
            val hasReplace = replaceMaterialView.text.isNotBlank()
            tabCompleted[2] = hasReplaceName && hasReplace
        }

        // ===== 4번 탭: 처리방법 =====
        val handlingMethodNameView = findViewById<EditText?>(R.id.handlingMethodName)
        val handlingMethodView = findViewById<EditText?>(R.id.handlingMethod)
        if (handlingMethodNameView != null && handlingMethodView != null) {
            val hasHandlingName = handlingMethodNameView.text.isNotBlank()
            val hasHandling = handlingMethodView.text.isNotBlank()
            tabCompleted[3] = hasHandlingName && hasHandling
        }

        // ===== 5번 탭: 조리순서 =====
        val cookOrderView = findViewById<EditText?>(R.id.cookOrderRecipeWrite)
        if (cookOrderView != null) {
            val hasCookOrder = cookOrderView.text.isNotBlank()
            tabCompleted[4] = hasCookOrder
        }

        // ===== 6번: 세부설정 =====
        val levelView = findViewById<TextView?>(R.id.elementaryLevel)
        val hourView  = findViewById<EditText?>(R.id.zero)
        val minView   = findViewById<EditText?>(R.id.halfHour)
        val tagView   = findViewById<EditText?>(R.id.detailSettleRecipeTitleWrite)

        if (levelView != null && hourView != null && minView != null && tagView != null) {
            val hasLevel = levelView.text.isNotBlank() && levelView.text !in listOf("난이도", "선택")
            val hasTime = hourView.text.isNotBlank() || minView.text.isNotBlank()
            val hasTag = tagView.text.isNotBlank()
            tabCompleted[5] = hasLevel && hasTime && hasTag
        }
    }

    // 탭 색상 업데이트
    private fun updateSelectedTab(selected: TextView) {
        textViewList.forEachIndexed { index, tab ->
            val color = when {
                tab == selected -> "#35A825" // 현재 선택은 무조건 초록색
                tabCompleted[index] -> "#2B2B2B" // 완료된 탭 (검정)
                else -> "#A1A9AD" // 기본 (회색)
            }
            tab.setTextColor(Color.parseColor(color))
        }
    }

    // 바
    private fun updateProgressBars() {
        if (progressBars.isEmpty() || committedCompleted.isEmpty()) return

        val committedCount = committedCompleted.count { it }   // ✅ 커밋(true) 개수
        val maxBars = minOf(progressBars.size, committedCompleted.size)

        for (i in 0 until maxBars) {
            val visible = i < committedCount
            val bar = progressBars[i]
            bar.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) bar.setBackgroundResource(R.drawable.bar_recipe)
        }
    }

    private fun onTabSwitched(newIndex: Int) {
        if (isSwitching) return
        isSwitching = true

        // 1) 현재 입력으로 tabCompleted 최신화(계산만)
        checkTabs()

        // 2) 떠나는 탭 커밋 상태를 완료 여부에 맞춰 갱신 (증가/감소 모두 여기서)
        val leaving = selectedIndex
        if (leaving in tabCompleted.indices) {
            committedCompleted[leaving] = tabCompleted[leaving]
        }

        // 3) 실제 전환
        selectedIndex = newIndex.coerceIn(0, tabCompleted.lastIndex)
        updateSelectedTab(textViewList[selectedIndex])
        moveUnderlineBar(textViewList[selectedIndex])
        changeLayout(layoutList[selectedIndex])

        // 4) 바는 전환 시점에만 반영
        updateProgressBars()

        isSwitching = false
    }

    private fun switchTo(targetIndex: Int, pushHistory: Boolean) {
        if (isSwitching) return
        isSwitching = true

        // 1) 현재 입력 기반으로 완료 여부 최신화(계산만)
        checkTabs()

        // 2) 떠나는 탭의 커밋 상태를 완료 여부대로 갱신 (증가/감소 전부 여기서)
        val leaving = selectedIndex
        if (leaving in tabCompleted.indices) {
            committedCompleted[leaving] = tabCompleted[leaving]
        }

        // 3) 히스토리 푸시(앞으로 이동/탭 클릭 시에만)
        if (pushHistory) {
            layoutHistory.push(currentLayout) // changeLayout()에서 currentLayout 반드시 갱신되게 해두기
        }

        // 4) 실제 전환
        selectedIndex = targetIndex.coerceIn(0, tabCompleted.lastIndex)
        val nextLayout = layoutList[selectedIndex]
        val nextTab = textViewList[selectedIndex]

        updateSelectedTab(nextTab)
        moveUnderlineBar(nextTab)
        changeLayout(nextLayout)    // 내부에서 currentLayout = nextLayout 로 갱신되도록!

        // 5) 바 갱신(전환 시에만)
        updateProgressBars()

        // (선택) 버튼 상태 갱신
        checkAndUpdateContinueButton()

        isSwitching = false
    }

    //계속하기 버튼 색 바뀜
    private fun checkAndUpdateContinueButton() {
        val continueButton = findViewById<AppCompatButton>(R.id.continueButton)
        val currentLayout = layoutList.find { it.visibility == View.VISIBLE }

        var isValid = false

        when (currentLayout?.id) {
            R.id.recipeWriteTitleLayout -> {
                val titleView = currentLayout.findViewById<EditText?>(R.id.recipeTitleWrite)
                val categoryView = currentLayout.findViewById<TextView?>(R.id.koreanFood)

                if (titleView != null && categoryView != null) {
                    val title = titleView.text.toString()
                    val category = categoryView.text.toString()
                    isValid = title.isNotBlank() && category.isNotBlank()
                }
            }

            R.id.recipeWriteMaterialLayout -> {
                var anyValid = false
                for (i in 0 until materialContainer.childCount) {
                    val row = materialContainer.getChildAt(i)
                    val name = row.findViewById<TextView?>(R.id.tvMaterialName)
                        ?.text?.toString()?.trim().orEmpty()
                    val qty  = row.findViewById<EditText?>(R.id.etMeasuring)
                        ?.text?.toString()?.trim().orEmpty()

                    // unit 뷰가 있으면 체크, 없으면 통과
                    val unitText = row.findViewById<TextView?>(R.id.unit)
                        ?.text?.toString()?.trim()
                    val unitOk = (unitText == null) || (unitText.isNotBlank() && unitText != "단위")

                    if (name.isNotEmpty() && qty.isNotEmpty() && unitOk) {
                        anyValid = true
                        break
                    }
                }
                isValid = anyValid
            }

            R.id.recipeWriteReplaceMaterialLayout -> {
                val nameView = currentLayout.findViewById<EditText?>(R.id.replaceMaterialName)
                val materialView = currentLayout.findViewById<EditText?>(R.id.replaceMaterial)

                if (nameView != null && materialView != null) {
                    val name = nameView.text.toString()
                    val material = materialView.text.toString()
                    isValid = name.isNotBlank() && material.isNotBlank()
                }
            }

            R.id.recipeWriteHandlingMethodLayout -> {
                val nameView = currentLayout.findViewById<EditText?>(R.id.handlingMethodName)
                val methodView = currentLayout.findViewById<EditText?>(R.id.handlingMethod)

                if (nameView != null && methodView != null) {
                    val name = nameView.text.toString()
                    val method = methodView.text.toString()
                    isValid = name.isNotBlank() && method.isNotBlank()
                }
            }

            R.id.recipeWriteDetailSettleLayout -> {
                val levelView = currentLayout.findViewById<TextView?>(R.id.elementaryLevel)
                val hourView  = currentLayout.findViewById<EditText?>(R.id.zero)
                val minView   = currentLayout.findViewById<EditText?>(R.id.halfHour)
                val tagView   = currentLayout.findViewById<EditText?>(R.id.detailSettleRecipeTitleWrite)

                if (levelView != null && hourView != null && minView != null && tagView != null) {
                    val lvlOk = levelView.text.toString().let { it.isNotBlank() && it !in listOf("난이도","선택") }
                    val timeOk = hourView.text.toString().isNotBlank() || minView.text.toString().isNotBlank()
                    val tagOk = tagView.text.toString().isNotBlank()
                    isValid = lvlOk && timeOk && tagOk
                }
            }
        }

        if (isValid) {
            continueButton.setBackgroundResource(R.drawable.btn_big_green)
            continueButton.setTextColor(Color.WHITE)
        } else {
            continueButton.setBackgroundResource(R.drawable.btn_number_of_people)
            continueButton.setTextColor(Color.parseColor("#A1A9AD"))
        }
        // 버튼은 항상 클릭 가능
        continueButton.isEnabled = true
    }

    //조리순서
    private fun checkCookOrderAndUpdateEndButton() {
        val stepEditText = findViewById<EditText?>(R.id.cookOrderRecipeWrite)
        val endFixButton = findViewById<AppCompatButton>(R.id.endFixButton)

        val stepTextFilled = stepEditText?.text?.toString()?.isNotBlank() == true

        if (stepTextFilled) {
            endFixButton.setBackgroundResource(R.drawable.btn_big_green)
            endFixButton.setTextColor(Color.WHITE)
            endFixButton.isEnabled = true
        } else {
            endFixButton.setBackgroundResource(R.drawable.btn_mypage_recipe_more)
            endFixButton.setTextColor(Color.parseColor("#9B9B9B"))
            endFixButton.isEnabled = false
        }
    }

    //재료, 대체 재료, 재료 처리 방법 추가
    private fun updateMaterialListView(materialView: View, ingredients: List<Pair<String, String>>, alternatives: List<Pair<String, String>>, handling: List<Pair<String, String>>) {
        val categoryGroup = materialView.findViewById<GridLayout>(R.id.categoryGroup)
        categoryGroup.removeAllViews() // 기존 뷰 제거

        // 공통으로 쓰이는 구분선 뷰 생성 함수
        fun createDivider(drawableId: Int): View {
            return View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2.dpToPx()
                ).apply {
                    topMargin = 12.dpToPx()
                }
                setBackgroundResource(drawableId)
            }
        }

        // 중간 제목 추가 함수
        fun addSectionTitle(title: String) {
            val titleLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 33.dpToPx()
                }
            }

            val titleText = TextView(this).apply {
                text = title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.parseColor("#2B2B2B"))
            }

            titleLayout.addView(titleText)
            categoryGroup.addView(titleLayout)
            categoryGroup.addView(createDivider(R.drawable.bar_recipe_see))
        }

        // 재료 항목 추가 함수
        fun addMaterialItem(name: String, amount: String) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 10.dpToPx()
                    leftMargin = 15.dpToPx()
                }
            }

            val nameText = TextView(this).apply {
                text = name
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(Color.parseColor("#2B2B2B"))
            }

            val amountText = TextView(this).apply {
                text = amount
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(Color.parseColor("#2B2B2B"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = 140.dpToPx()
                }
            }

            rowLayout.addView(nameText)
            rowLayout.addView(amountText)

            categoryGroup.addView(rowLayout)
            categoryGroup.addView(createDivider(R.drawable.bar_recipe_see_material))
        }

        // 🔽 섹션별로 추가
        if (ingredients.isNotEmpty()) {
            addSectionTitle("기본 재료")
            ingredients.forEach { (name, amount) ->
                addMaterialItem(name, amount)
            }
        }

        if (alternatives.isNotEmpty()) {
            addSectionTitle("대체 가능한 재료")
            alternatives.forEach { (original, replace) ->
                addMaterialItem(original, replace)
            }
        }

        if (handling.isNotEmpty()) {
            addSectionTitle("사용된 재료 처리 방법")
            handling.forEach { (ingredient, method) ->
                addMaterialItem(ingredient, method)
            }
        }
    }

    //조리 순서 추가
    private fun addCookingSteps(context: Context, steps: List<CookingStep>)  {
        val container = findViewById<LinearLayout>(R.id.stepSeeContainer)

        steps.forEachIndexed { index, step ->
            val context = this

            // STEP 제목
            val stepTitle = TextView(context).apply {
                text = "STEP ${index + 1}"
                textSize = 15f
                setTextColor(Color.BLACK)
                setPadding(20, 26, 0, 0)
            }

            container.addView(stepTitle)

            // 이미지 (URL 있을 경우에만)
            if (!step.mediaUrl.isNullOrBlank()) {
                val imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(20, 15, 20, 0)
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }

                Glide.with(context)
                    .load(RetrofitInstance.BASE_URL + step.mediaUrl.trim())
                    .into(imageView)

                container.addView(imageView)
            }

            // 설명 텍스트
            val description = TextView(context).apply {
                text = step.description
                textSize = 13f
                setTextColor(Color.BLACK)
                setPadding(20, 26, 20, 0)
            }

            container.addView(description)

            // 타이머 (있을 경우)
            if (step.timeInSeconds > 0) {
                val timerText = TextView(context).apply {
                    text = "타이머"
                    textSize = 15f
                    setTextColor(Color.BLACK)
                    setPadding(20, 20, 0, 0)
                }
                val timeFormatted = String.format(
                    "%02d:%02d",
                    step.timeInSeconds / 60,
                    step.timeInSeconds % 60
                )
                val timeValue = TextView(context).apply {
                    text = timeFormatted
                    textSize = 32f
                    setTextColor(Color.parseColor("#2B2B2B"))
                    setPadding(0, 10, 30, 0)
                    gravity = Gravity.END
                }

                container.addView(timerText)
                container.addView(timeValue)
            }

            // 아래 구분선
            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1.dpToPx()
                ).apply {
                    setMargins(20, 20, 20, 0)
                }
                setBackgroundResource(R.drawable.bar_rectangle)
            }
            container.addView(divider)
        }
    }

    // 이전으로 버튼 기능을 위해 현재 화면 저장
    private fun changeLayout(newLayout: ConstraintLayout) {
        if (newLayout != currentLayout) {
            showOnlyLayout(newLayout)
            checkAndUpdateContinueButton()
            currentLayout = newLayout
        }
    }

    //다른 레이아웃은 숨기고 target 화면만 보여줌
    private fun showOnlyLayout(target: ConstraintLayout) {
        layoutList.forEach { it.visibility = View.GONE }
        target.visibility = View.VISIBLE
        currentLayout = target
    }

    // 탭바 선택한 텍스트 아래로 바 이동
    private fun moveUnderlineBar(target: TextView) {
        underlineBar.post {
            val targetX = target.x + target.width / 2f - underlineBar.width / 2f
            underlineBar.animate().x(targetX).setDuration(200).start()
        }
    }

    // 레시피 타이틀 드롭다운 열기
    private fun openDropDown(categoryDropDown: ConstraintLayout, recipeName: ConstraintLayout) {
        categoryDropDown.visibility = View.VISIBLE

        // recipeName 위치 조정
        val params = recipeName.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = recipeName.dpToPx(267)
        recipeName.layoutParams = params
    }

    // 레시피 타이틀 드롭다운 닫기 및 recipeName 위치 복원
    private fun closeDropDown(categoryDropDown: ConstraintLayout, recipeName: ConstraintLayout) {
        categoryDropDown.visibility = View.GONE

        // recipeName 위치 복원
        val params = recipeName.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = recipeName.dpToPx(35)
        recipeName.layoutParams = params
    }

    // dp → px 변환 함수
    private fun View.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    // 재료 리스트에서 선택된 재료를 받아와서 행 추가
    private fun addNewItem(selected: IngredientResponse, sourceRect: View?): View {
        val view = layoutInflater.inflate(R.layout.item_recipe_material, materialContainer, false)

        val tvName = view.findViewById<TextView>(R.id.tvMaterialName)
        val etMeasuring = view.findViewById<EditText>(R.id.etMeasuring)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        tvName.text = selected.nameKo
        view.tag = (selected.id ?: -1L)

        // ▷ 타이핑/지울 때마다 계속하기 버튼 상태 갱신
        etMeasuring.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAndUpdateContinueButton()
                checkTabs()                            // ✅ 완료 여부 최신화
                updateSelectedTab(textViewList[selectedIndex])  // (선택) 현재 탭 색 즉시 반영
                updateProgressBars()                   // (선택) 바도 즉시 반영
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnDelete.setOnClickListener {
            materialContainer.removeView(view)
            itemCount--

            // 선택 하이라이트 원복(있다면)
            val rectToClear = (sourceRect ?: view.tag) as? View
            rectToClear?.let { rect ->
                rectToRow.remove(rect)
                if (selectedRects.remove(rect)) {
                    rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                }
            }

            // ▷ 삭제 후에도 버튼 상태 갱신
            checkAndUpdateContinueButton()
            checkTabs()
            updateSelectedTab(textViewList[selectedIndex])
            updateProgressBars()
        }

        materialContainer.addView(view)

        // ▷ 아이템 추가 직후에도 한번 갱신
        checkAndUpdateContinueButton()

        return view
    }

    // 임시 저장 복원용 (selected, sourceRect 없이도 호출 가능)
    private fun addNewItem(): View {
        val dummy = IngredientResponse(
            id = -1,
            nameKo = "",
            category = "",
            defaultUnitId = null,
            iconUrl = ""
        )
        return addNewItem(dummy, null)
    }
    // ================== DRAFT BINDING ==================

    private fun bindDraft(dto: RecipeDraftDto) {
        val gson = Gson()

        // 1) 제목/카테고리/난이도/태그/시간
        findViewById<EditText>(R.id.recipeTitleWrite).setText(dto.title.orEmpty())
        findViewById<TextView>(R.id.koreanFood).text = enumToCategory(dto.category)
        findViewById<TextView>(R.id.elementaryLevel).apply {
            text = dto.difficulty.orEmpty().ifBlank { text }  // "난이도" 기본값 유지
            setTextColor(Color.parseColor("#2B2B2B"))
        }
        findViewById<EditText>(R.id.detailSettleRecipeTitleWrite).setText(dto.tags.orEmpty())

        val totalMin = (dto.cookingTime ?: 0).coerceAtLeast(0)
        findViewById<EditText>(R.id.zero).setText((totalMin / 60).toString())
        findViewById<EditText>(R.id.halfHour).setText((totalMin % 60).toString())

        // 2) 대표 이미지
        mainImageUrl = dto.mainImageUrl.orEmpty()
        if (mainImageUrl.isNotBlank()) {
            val fullUrl = RetrofitInstance.BASE_URL + mainImageUrl.trim()
            addUrlImageToContainer(fullUrl, representImageContainer)
        } else {
            representImageContainer.removeAllViews()
        }

        bindIngredientsFromRes(dto.ingredients.orEmpty())

        // ── 대체재료 / 처리방법 / 조리순서 복원 로직은 그대로 ──
        val altMaps = safeParseMapList(gson, dto.alternativeIngredients)
        restoreAlternativeIngredientsFromMaps(altMaps)

        val handlingPairs: List<Pair<String,String>> = parseHandlingPairs(gson, dto.handlingMethods)
        restoreHandlingMethods(handlingPairs)

        val type = object : TypeToken<List<CookingStep>>() {}.type
        val steps: List<CookingStep> = try { Gson().fromJson(dto.cookingSteps, type) } catch (_: Exception) { emptyList() }
        restoreSteps(steps)

        checkTabs()
        checkAndUpdateContinueButton()
        updateSelectedTab(textViewList[selectedIndex])
        updateProgressBars()
    }

    private fun enumToCategory(enum: String?): String = when (enum) {
        "koreaFood" -> "한식"
        "westernFood" -> "양식"
        "japaneseFood" -> "일식"
        "chineseFood" -> "중식"
        "vegetarianDiet" -> "채식"
        "snack" -> "간식"
        "alcoholSnack" -> "안주"
        "sideDish" -> "반찬"
        "etc", null, "" -> "기타"
        else -> "기타"
    }

    private fun bindIngredientsFromRes(list: List<RecipeIngredientRes>) {
        materialContainer.removeAllViews()

        list.forEach { ri ->
            val row = addNewItem()                  // 동적 행 생성
            val vg  = row as ViewGroup

            val (nameV, qtyV) = vg.findNameAndQtyViews()

            // 이름: id 매칭 우선, 없으면 서버 name, 둘 다 없으면 placeholder
            val displayName = allIngredients.firstOrNull { it.id == (ri.id ?: -1L) }?.nameKo
                ?: ri.name
                ?: "(알 수 없음)"
            nameV?.text = displayName

            // 수량: Double → 문자열(정수면 3, 아니면 3.5)
            val amountText = ri.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: ""
            qtyV?.text = amountText

            if (nameV == null || qtyV == null) {
                Log.w("BindIngredient", "행에서 이름/계량 뷰를 못 찾음. 힌트/태그/구조 확인 필요")
            }
        }
    }

    private fun ViewGroup.findNameAndQtyViews(): Pair<TextView?, TextView?> {
        var nameV: TextView? = null
        var qtyV: TextView?  = null

        fun isNameHint(h: String?) = h?.contains("재료", ignoreCase = true) == true || h == "재료명"
        fun isQtyHint(h: String?)  = h?.contains("계량", ignoreCase = true) == true || h?.contains("수량") == true

        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is TextView) {
                val tag = (v.tag as? String)?.lowercase()
                val hint = v.hint?.toString()

                when {
                    tag == "name" || isNameHint(hint) -> nameV = v
                    tag == "qty"  || isQtyHint(hint)  -> qtyV  = v
                }
            }
        }

        // 힌트/태그로 못 찾은 경우 순서 fallback
        if (nameV == null || qtyV == null) {
            val tvs = (0 until childCount)
                .map { getChildAt(it) }
                .filterIsInstance<TextView>()
            if (nameV == null) nameV = tvs.firstOrNull()
            if (qtyV  == null) qtyV  = tvs.getOrNull(1)
        }
        return nameV to qtyV
    }

    data class CookingStep(
        val step: Int? = null,
        val description: String? = null,
        val mediaType: String? = null,
        val mediaUrl: String? = null,
        val timeInSeconds: Int = 0   // ← non-null + 기본값
    )

    private fun ensureStepsObjectJson(raw: String?): String {
        if (raw.isNullOrBlank()) return "[]"
        val g = Gson()
        return try {
            val tObj = object : com.google.gson.reflect.TypeToken<List<CookingStep>>() {}.type
            g.fromJson<List<CookingStep>>(raw, tObj)   // 이미 객체 배열
            raw
        } catch (_: Exception) {
            val tStr = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            val texts = runCatching { g.fromJson<List<String>>(raw, tStr) }.getOrDefault(emptyList())
            val objs = texts.filter { it.isNotBlank() }
                .mapIndexed { idx, s ->
                    CookingStep(step = idx + 1, description = s, timeInSeconds = 0) // ← 0으로 보정
                }
            g.toJson(objs)
        }
    }

    /** Gson으로 List<T> 안전 파싱 */
    private inline fun <reified T> safeParse(gson: Gson, json: String?): List<T> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    /** 처리방법은 저장형이 제각각일 수 있어 유연 파싱 */
    private fun parseHandlingPairs(gson: Gson, json: String?): List<Pair<String,String>> {
        if (json.isNullOrBlank()) return emptyList()

        // 1) Map 기반 (키 이름이 제각각이어도 대응)
        try {
            val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
            val maps: List<Map<String, Any?>> = gson.fromJson(json, type) ?: emptyList()

            fun getStr(m: Map<String, Any?>, keys: List<String>): String =
                keys.firstNotNullOfOrNull { k ->
                    m[k]?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                } ?: ""

            val nameKeys   = listOf("name","ingredient","ingredientName","재료명","left","key")
            val methodKeys = listOf("value","method","handling","방법","description","right","val")

            val pairs = maps.map { m ->
                val n = getStr(m, nameKeys)
                val v = getStr(m, methodKeys)
                n to v
            }.filter { it.first.isNotBlank() && it.second.isNotBlank() }

            if (pairs.isNotEmpty()) return pairs
        } catch (_: Exception) { /* ignore */ }

        // 2) Pair<String, String> 직렬화
        try {
            val type = object : TypeToken<List<Pair<String,String>>>() {}.type
            val pairs: List<Pair<String,String>> = gson.fromJson(json, type) ?: emptyList()
            if (pairs.isNotEmpty()) return pairs
        } catch (_: Exception) { /* ignore */ }

        // 3) List<String> "이름 : 방법" 형태
        try {
            val type = object : TypeToken<List<String>>() {}.type
            val strs: List<String> = gson.fromJson(json, type) ?: emptyList()
            val pairs = strs.mapNotNull { s ->
                val idx = s.indexOf(':')
                if (idx <= 0) null else s.substring(0, idx).trim() to s.substring(idx + 1).trim()
            }
            if (pairs.isNotEmpty()) return pairs
        } catch (_: Exception) { /* ignore */ }

        return emptyList()
    }

    /** 대표 이미지 URL을 LinearLayout 컨테이너에 넣기 */
    private fun addUrlImageToContainer(url: String, container: LinearLayout) {
        container.removeAllViews()
        val iv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        Glide.with(this).load(url).into(iv)
        container.addView(iv)
    }

    private fun safeParseMapList(gson: Gson, json: String?): List<Map<String, String>> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            gson.fromJson<List<Map<String, String>>>(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    private fun extractName(m: Map<String, String>): String =
        m["name"] ?: m["ingredient"] ?: m["ingredientName"] ?: m["재료명"] ?: ""

    private fun extractAmountWithUnit(m: Map<String, String>): String {
        // 서버/클라이언트 저장 포맷 다양성 대응
        val amount = m["value"] ?: m["amount"] ?: m["quantity"] ?: m["measuring"] ?: m["계량"] ?: ""
        val unit   = m["unit"] ?: m["단위"] ?: ""
        return if (unit.isNotBlank() && !amount.endsWith(" $unit")) "$amount $unit" else amount
    }


    private fun collectIngredientReqs(): List<RecipeIngredientReq> {
        val out = mutableListOf<RecipeIngredientReq>()

        // 1) 고정칸
        run {
            val name   = findViewById<EditText>(R.id.material)?.text?.toString()?.trim().orEmpty()
            val qtyTxt = findViewById<EditText>(R.id.measuring)?.text?.toString()?.trim().orEmpty()
            if (name.isNotBlank() && qtyTxt.isNotBlank()) {
                val id = allIngredients.firstOrNull { it.nameKo == name }?.id
                val (qtyStr, unitStr) = splitAmount(qtyTxt)
                val qty = qtyStr.toDoubleOrNull()
                if (id != null && id > 0 && qty != null) {
                    out += RecipeIngredientReq(
                        id = id,
                        quantity = qty
                    )
                }
            }
        }

        // 2) 동적칸
        for (i in 0 until materialContainer.childCount) {
            val row = materialContainer.getChildAt(i) as? ViewGroup ?: continue

            // 뷰 아이디가 다를 수 있으니 두 방식 모두 시도
            val name = row.findViewById<TextView>(R.id.tvMaterialName)
                ?.text?.toString()?.trim().orEmpty()
            val qtyTxt = (row.findViewById<EditText>(R.id.etMeasuring) ?: row.getChildAt(1) as? EditText)
                ?.text?.toString()?.trim().orEmpty()

            if (name.isBlank() || qtyTxt.isBlank()) continue

            val id = allIngredients.firstOrNull { it.nameKo == name }?.id
            val (qtyStr, unitStr) = splitAmount(qtyTxt)
            val qty = qtyStr.toDoubleOrNull()
            if (id != null && id > 0 && qty != null) {
                out += RecipeIngredientReq(
                    id = id,
                    quantity = qty
                )
            }
        }

        return out // 비어있어도 OK (null 금지)
    }

    // id -> 이름 복구용(없으면 "(알 수 없음)")
    private fun resolveIngredientNameById(id: Long?): String {
        if (id == null) return "(알 수 없음)"
        val n = allIngredients.firstOrNull { it.id == id }?.nameKo
        return if (!n.isNullOrBlank()) n else "(알 수 없음)"
    }

    private fun bindIngredientsFromReqs(list: List<RecipeIngredientRes>?) {
        if (list.isNullOrEmpty()) return
        materialContainer.removeAllViews()

        // 고정칸
        list.first().let { ri ->
            findViewById<EditText>(R.id.material).setText(ri.name)
            findViewById<EditText>(R.id.measuring).setText(
                if (ri.amount % 1.0 == 0.0) ri.amount.toLong().toString() else ri.amount.toString()
            )
            findViewById<TextView?>(R.id.unit)?.text = "단위"
        }
        // 동적칸
        list.drop(1).forEach { ri ->
            val row = addNewItem() as ViewGroup
            (row.getChildAt(0) as? EditText)?.setText(ri.name)
            (row.getChildAt(1) as? EditText)?.setText(
                if (ri.amount % 1.0 == 0.0) ri.amount.toLong().toString() else ri.amount.toString()
            )
        }
    }

    private fun buildStepObjectsFromCurrentUI(): List<CookingStepReq> {
        // "STEP N: ..." 형태의 텍스트들을 재사용
        val texts = saveRecipeSteps()
        return texts.mapIndexed { idx, desc ->
            val stepNo = idx + 1
            val (h, m) = stepTimerMap[stepNo] ?: (0 to 0)
            val sec = h * 3600 + m * 60
            val url = stepImages[stepNo]?.trim().orEmpty()
            val type = if (url.isNotBlank()) "IMAGE" else "NONE"
            CookingStepReq(
                step = stepNo,
                description = desc,     // 예: "STEP 1: 물을 끓인다 → ..."
                mediaType = type,
                mediaUrl = url,
                timeInSeconds = sec
            )
        }
    }


    private fun restoreIngredientsFromMaps(items: List<Map<String,String>>) {
        materialContainer.removeAllViews()

        items.forEach { m ->
            val name = extractName(m)
            val (qty) = splitQtyUnit(extractAmountWithUnit(m))

            // IngredientResponse 형태로 임시 변환
            val ing = IngredientResponse(
                id = -1,
                nameKo = name,
                category = "",
                defaultUnitId = null,
                iconUrl = ""
            )

            val row = addNewItem(ing, null) // 새로운 행 추가

            // ID로 EditText를 안전하게 찾아서 텍스트 설정
            val qtyEt = row.findViewById<EditText>(R.id.etMeasuring)
            qtyEt?.setText(qty)
        }
    }


    private fun restoreAlternativeIngredientsFromMaps(items: List<Map<String,String>>) {
        replaceMaterialContainer.removeAllViews()

        Log.d("ALT_JSON", "alts=${items.joinToString { it.toString() }}")

        fun left(m: Map<String,String>) =
            m["name"] ?: m["original"] ?: m["from"] ?: m["ingredient"] ?: m["ingredientName"] ?: m["재료명"] ?: m["left"] ?: ""
        fun right(m: Map<String,String>) =
            m["value"]
                ?: m["amount"]
                ?: m["replaceMaterial"]
                ?: m["replacement"]
                ?: m["alternative"]
                ?: m["replace"]
                ?: m["to"]
                ?: m["quantity"]
                ?: m["measuring"]
                ?: m["대체재료"]
                ?: m["right"]
                ?: m["val"]
                ?: ""


        val first = items.getOrNull(0)
        val second = items.getOrNull(1)

        findViewById<EditText>(R.id.replaceMaterialName).setText(first?.let { left(it) }.orEmpty())
        findViewById<EditText>(R.id.replaceMaterial).setText(first?.let { right(it) }.orEmpty())

        if (second != null) {
            findViewById<EditText>(R.id.replaceMaterialMaterialTwo).setText(left(second))
            findViewById<EditText>(R.id.replaceMaterialTwo).setText(right(second))
        }

        items.drop(2).forEach { m ->
            replaceMaterialAddNewItem()
            val row = replaceMaterialContainer.getChildAt(replaceMaterialContainer.childCount - 1) as ViewGroup
            val nameEt = row.getChildAt(0) as EditText
            val replEt = row.getChildAt(1) as EditText
            nameEt.setText(left(m))
            replEt.setText(right(m))
        }
    }

    private fun restoreHandlingMethods(items: List<Pair<String,String>>) {
        handlingMethodContainer.removeAllViews()
        val first = items.getOrNull(0)
        val second = items.getOrNull(1)

        findViewById<EditText>(R.id.handlingMethodName).setText(first?.first.orEmpty())
        findViewById<EditText>(R.id.handlingMethod).setText(first?.second.orEmpty())

        if (second != null) {
            findViewById<EditText>(R.id.handlingMethodMaterialTwo).setText(second.first)
            findViewById<EditText>(R.id.handlingMethodTwo).setText(second.second)
        }

        items.drop(2).forEach { (name, method) ->
            handlingMethodAddNewItem()
            val row = handlingMethodContainer.getChildAt(handlingMethodContainer.childCount - 1) as ViewGroup
            val nameEt = row.getChildAt(0) as EditText
            val methodEt = row.getChildAt(1) as EditText
            nameEt.setText(name)
            methodEt.setText(method)
        }
    }

    private suspend fun awaitUploadsIfAny() {
        while (uploadsInFlight > 0) {
            kotlinx.coroutines.delay(50)
        }
    }

    private fun restoreSteps(steps: List<CookingStep>) {
        if (steps.isEmpty()) {
            ensureFirstStepInflated()
            return
        }

        // 초기화
        stepContainer.removeAllViews()
        stepRecipeCountMap.clear()
        stepTimerMap.clear()
        stepImages.clear()
        currentStep = 1
        recipeStepCount = 1

        steps.forEachIndexed { idx, st ->
            val stepNo = (st.step ?: (idx + 1)).coerceAtLeast(1)

            if (idx == 0) addNewStep(stepNo) else addNewStep(stepNo)
            // addNewStep이 새 스텝 레이아웃을 마지막에 붙임
            val stepLayout = stepContainer.getChildAt(stepContainer.childCount - 1)
            val firstEt = stepLayout.findViewById<EditText>(R.id.cookOrderRecipeWrite)
            firstEt.setText(firstDesc(st.description))

            // 서브 스텝들
            val others = otherDescs(st.description)
            val dynamicContainer = stepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)
            var subIdx = 2
            stepRecipeCountMap[stepNo] = maxOf(stepRecipeCountMap[stepNo] ?: 2, 2)
            others.forEach { txt ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(45, 38, 45, 0) }
                }
                val prefix = TextView(this).apply {
                    text = "$stepNo-$subIdx"
                    setTextColor(Color.parseColor("#2B2B2B"))
                    textSize = 13f
                    setPadding(0, 0, 12, 0)
                }
                val et = EditText(this).apply {
                    id = View.generateViewId()
                    tag = "$stepNo-$subIdx"
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    hint = "레시피를 입력해주세요."
                    setText(txt)
                    textSize = 13f
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#A1A9AD"))
                }
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                        .apply { setMargins(45, 12, 45, 0) }
                    setBackgroundColor(Color.parseColor("#D9D9D9"))
                }
                row.addView(prefix)
                row.addView(et)
                dynamicContainer.addView(row)
                dynamicContainer.addView(divider)
                subIdx++
                stepRecipeCountMap[stepNo] = subIdx
            }

            // 타이머
            if ((st.timeInSeconds ?: 0) > 0) {
                val h = (st.timeInSeconds ?: 0) / 3600
                val m = ((st.timeInSeconds ?: 0) % 3600) / 60
                stepTimerMap[stepNo] = h to m
            }

            // 이미지
            if (!st.mediaUrl.isNullOrBlank()) {
                stepImages[stepNo] = st.mediaUrl!!
                val imgUrl = RetrofitInstance.BASE_URL + st.mediaUrl!!.trim()
                val container = stepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)
                val iv = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                Glide.with(this).load(imgUrl).into(iv)
                container.addView(iv, 0) // 상단에 보이도록
            }
        }
    }

    private fun splitQtyUnit(value: String?): Pair<String,String> {
        if (value.isNullOrBlank()) return "" to ""
        val parts = value.trim().split(" ")
        if (parts.size == 1) return parts[0] to ""
        val unit = parts.last()
        val qty  = parts.dropLast(1).joinToString(" ")
        return qty to unit
    }

    // "STEP X: a → b → c" 형태에서 첫 문장/나머지 추출(저장형식에 유연)
    private fun firstDesc(desc: String?): String {
        if (desc.isNullOrBlank()) return ""
        val cut = desc.substringAfter(": ", desc)
        return cut.split("→").map { it.trim() }.firstOrNull().orEmpty()
    }
    private fun otherDescs(desc: String?): List<String> {
        if (desc.isNullOrBlank()) return emptyList()
        val cut = desc.substringAfter(": ", desc)
        val list = cut.split("→").map { it.trim() }.filter { it.isNotBlank() }
        return if (list.size <= 1) emptyList() else list.drop(1)
    }

    // 레시피 대체재료 내용 추가하기 클릭시 내용 추가
    private fun replaceMaterialAddNewItem() {
        // 새로운 ConstraintLayout 생성
        val newItemLayout = ConstraintLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 재료명 EditText 생성
        val replaceMaterialMaterialTwo = EditText(this).apply {
            id = View.generateViewId()  // ID 생성하여 ConstraintLayout에서 사용할 수 있도록 함
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(18), dpToPx(10), 0, 0) // 위쪽 여백 설정, 시작 여백 설정
            }
            hint = "재료명"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 대체 가능한 재료명 EditText 생성
        val replaceMaterialTwo = EditText(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // 계량 EditText는 내용 크기만큼 표시
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                startToStart = replaceMaterialMaterialTwo.id  // 재료명 EditText 왼쪽 끝에 맞추기
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 맞추기
                topToTop = replaceMaterialMaterialTwo.id  // 재료명 EditText 위쪽 끝에 맞추기

                setMargins(534, 10, 0, 0) // 위쪽 여백 설정
            }
            hint = "대체 가능한 재료명"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 삭제 버튼 생성
        val replaceMaterialDeleteTwo = ImageButton(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 오른쪽 끝에 위치하도록 설정
                endToEnd = replaceMaterialMaterialTwo.id  // materialEditText의 오른쪽 끝에 맞추기
                topToTop = replaceMaterialMaterialTwo.id  // materialEditText의 위쪽 끝에 맞추기

                // 오른쪽 마진을 5dp로 설정하여 왼쪽으로 이동
                setMargins(0, 0, dpToPx(14), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
            }
            setImageResource(R.drawable.ic_delete) // 삭제 아이콘 설정
            setBackgroundResource(android.R.color.transparent) // 배경 투명
        }

        // 삭제 버튼 클릭 시 해당 레이아웃 삭제 & 버튼 위치 조정
        replaceMaterialDeleteTwo.setOnClickListener {
            replaceMaterialContainer.removeView(newItemLayout)
            itemCount--  // 아이템 수 감소
            replaceMaterialMoveButtonUp() // 아이템 삭제 시 버튼을 위로 이동
        }

        // 새로운 바 생성
        val divideRectangleBarThirteen = View(this).apply {
            id = View.generateViewId()  // ID 생성

            // LayoutParams 설정
            val layoutParams = ConstraintLayout.LayoutParams(
                0,  // width를 0으로 설정하여 부모의 width를 채우도록 함
                dpToPx(2)  // 2dp 높이를 px로 변환하여 설정
            ).apply {
                // 바를 materialEditText 아래로 배치
                topToBottom = replaceMaterialMaterialTwo.id  // materialEditText 아래에 위치
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID  // 왼쪽 끝에 붙임
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 붙임

                // 좌우 마진 5dp 설정
                setMargins(dpToPx(3), 0, dpToPx(3), 0)
            }

            this.layoutParams = layoutParams
            setBackgroundResource(R.drawable.bar_recipe_see_material)  // 배경 설정
        }

        // LinearLayout에 요소 추가
        newItemLayout.apply {
            addView(replaceMaterialMaterialTwo)
            addView(replaceMaterialTwo)
            addView(replaceMaterialDeleteTwo)
            addView(divideRectangleBarThirteen)
        }

        // 부모 레이아웃에 추가
        replaceMaterialContainer.addView(newItemLayout)
        itemCount++
    }

    // 레시피 대체재료 내용 추가 버튼 클릭시 버튼 아래로 이동
    private fun replaceMaterialMoveButtonDown() {
        val params = replaceMaterialAddFixButton.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin += buttonMarginIncrease // 버튼을 130px 아래로 이동
        replaceMaterialAddFixButton.layoutParams = params
    }

    // 레시피 대체재료 내용 추가 버튼 위로 이동
    private fun replaceMaterialMoveButtonUp() {
        val params = replaceMaterialAddFixButton.layoutParams as ConstraintLayout.LayoutParams
        if (params.topMargin > 0) {
            params.topMargin -= buttonMarginIncrease // 버튼을 130px 위로 이동
            replaceMaterialAddFixButton.layoutParams = params
        }
    }

    // 레시피 처리방법 내용 추가하기 클릭시 내용 추가
    private fun handlingMethodAddNewItem() {
        // 새로운 ConstraintLayout 생성
        val newItemLayout = ConstraintLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 재료명 EditText 생성
        val handlingMethodMaterialTwo = EditText(this).apply {
            id = View.generateViewId()  // ID 생성하여 ConstraintLayout에서 사용할 수 있도록 함
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(47, 10, 0, 0) // 위쪽 여백 설정
            }
            hint = "재료명"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 처리방법 EditText 생성
        val handlingMethodTwo = EditText(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // 계량 EditText는 내용 크기만큼 표시
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                startToStart = handlingMethodMaterialTwo.id  // 재료명 EditText 왼쪽 끝에 맞추기
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 맞추기
                topToTop = handlingMethodMaterialTwo.id  // 재료명 EditText 위쪽 끝에 맞추기

                setMargins(534, 1, 0, 0) // 위쪽 여백 설정
            }
            hint = "처리방법"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 삭제 버튼 생성
        val handlingMethodDeleteTwo = ImageButton(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 오른쪽 끝에 위치하도록 설정
                endToEnd = handlingMethodMaterialTwo.id  // materialEditText의 오른쪽 끝에 맞추기
                topToTop = handlingMethodMaterialTwo.id  // materialEditText의 위쪽 끝에 맞추기

                // 오른쪽 마진을 5dp로 설정하여 왼쪽으로 이동
                setMargins(0, 0, 13.dpToPx(), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
            }
            setImageResource(R.drawable.ic_delete) // 삭제 아이콘 설정
            setBackgroundResource(android.R.color.transparent) // 배경 투명
        }

        // 삭제 버튼 클릭 시 해당 레이아웃 삭제 & 버튼 위치 조정
        handlingMethodDeleteTwo.setOnClickListener {
            handlingMethodContainer.removeView(newItemLayout)
            itemCount--  // 아이템 수 감소
            handlingMethodMoveButtonUp() // 아이템 삭제 시 버튼을 위로 이동
        }

        // 새로운 바 생성
        val divideRectangleBarSixteen = View(this).apply {
            id = View.generateViewId()  // ID 생성

            // LayoutParams 설정
            val layoutParams = ConstraintLayout.LayoutParams(
                0,  // width를 0으로 설정하여 부모의 width를 채우도록 함
                dpToPx(2)  // 2dp 높이를 px로 변환하여 설정
            ).apply {
                // 바를 materialEditText 아래로 배치
                topToBottom = handlingMethodMaterialTwo.id  // materialEditText 아래에 위치
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID  // 왼쪽 끝에 붙임
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 붙임

                // 좌우 마진 5dp 설정
                setMargins(3.dpToPx(), 0, 3.dpToPx(), 0)
            }

            this.layoutParams = layoutParams
            setBackgroundResource(R.drawable.bar_recipe_see_material)  // 배경 설정
        }

        // LinearLayout에 요소 추가
        newItemLayout.apply {
            addView(handlingMethodMaterialTwo)
            addView(handlingMethodTwo)
            addView(handlingMethodDeleteTwo)
            addView(divideRectangleBarSixteen)
        }

        // 부모 레이아웃에 추가
        handlingMethodContainer.addView(newItemLayout)
        itemCount++
    }

    // 레시피 처리방법 내용 추가 버튼 클릭시 버튼 아래로 이동
    private fun handlingMethodMoveButtonDown() {
        val params = handlingMethodAddFixButton.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin += buttonMarginIncrease // 버튼을 130px 아래로 이동
        handlingMethodAddFixButton.layoutParams = params
    }

    // 레시피 처리방법 내용 추가 버튼 위로 이동
    private fun handlingMethodMoveButtonUp() {
        val params = handlingMethodAddFixButton.layoutParams as ConstraintLayout.LayoutParams
        if (params.topMargin > 0) {
            params.topMargin -= buttonMarginIncrease // 버튼을 130px 위로 이동
            handlingMethodAddFixButton.layoutParams = params
        }
    }

    // 레시피 조리순서 내용 추가 버튼 위로 이동
    private fun addRecipeStep(step: Int, subStep: Int) {
        val stepLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(45, 12, 45, 0)
            }
        }

        // 고정된 STEP 번호 텍스트뷰
        val stepPrefix = TextView(this).apply {
            text = "$step-$subStep"
            textSize = 13f
            setTextColor(Color.parseColor("#2B2B2B"))
            setPadding(15, 0, 12, 0)
        }

        val editText = EditText(this).apply {
            id = View.generateViewId()
            tag = "$step-$subStep"
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f // 남은 영역 채우기
            )
            hint = "레시피를 입력해주세요."
            textSize = 13f
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#A1A9AD"))
        }

        stepLayout.addView(stepPrefix)
        stepLayout.addView(editText)
        cookOrderRecipeContainer.addView(stepLayout)

        // 구분 바(View) 생성
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(45, 8, 45, 0)
            }
            setBackgroundColor(Color.parseColor("#D9D9D9"))
        }
        cookOrderRecipeContainer.addView(divider)

        // 기존 버튼 가져오기
        val addButton = findViewById<AppCompatButton>(R.id.contentAdd)
        val timerButton = findViewById<AppCompatButton>(R.id.timerAdd)

        // 🚀 버튼 위치 조정 (입력 칸과 70dp 떨어지게 설정)
        val buttonParams = addButton.layoutParams as ViewGroup.MarginLayoutParams
        buttonParams.topMargin += 15 // 🔽 입력 칸과 70dp 간격 유지
        addButton.requestLayout()

        val timerParams = timerButton.layoutParams as ViewGroup.MarginLayoutParams
        timerParams.topMargin += 15 // 🔽 동일하게 70dp 유지
        timerButton.requestLayout()
    }

    private fun addNewStep(step: Int) {
        // (선택) 이전 step 숨기는 로직: 필요 없으면 제거하세요
        for (i in 0 until stepContainer.childCount) {
            stepContainer.getChildAt(i).visibility = View.GONE
        }

        val newStepLayout = LayoutInflater.from(this).inflate(R.layout.item_step, stepContainer, false)

        // 기본 입력칸 태그/텍스트
        val cookOrderRecipeWrite = newStepLayout.findViewById<EditText>(R.id.cookOrderRecipeWrite)
        cookOrderRecipeWrite.tag = "$step-1"

        val stepTextView = newStepLayout.findViewById<TextView>(R.id.stepOne)
        stepTextView.text = "STEP $step"

        val stepLittleTextView = newStepLayout.findViewById<TextView>(R.id.stepLittleOne)
        stepLittleTextView.text = "$step-1"

        // 카메라 버튼
        val stepCamera = newStepLayout.findViewById<ImageButton>(R.id.stepCamera)
        val cookOrderRecipeContainerAdd = newStepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)

        stepCamera.visibility = View.VISIBLE
        stepCamera.isClickable = true
        stepCamera.setOnClickListener {
            // 이 스텝 컨테이너에 이미지 추가하고, 업로드 결과는 이 스텝에 매핑
            selectedContainer = cookOrderRecipeContainerAdd
            pendingStepForImage = step
            pickImageLauncherForStepCamera.launch("image/*")
        }

        // 내용추가
        val contentAddTwo = newStepLayout.findViewById<AppCompatButton>(R.id.contentAddTwo)
        val timerAddTwo   = newStepLayout.findViewById<AppCompatButton>(R.id.timerAddTwo)

        timerAddTwo.setOnClickListener {
            val dynamicContainer = cookOrderRecipeContainerAdd

            // 기존 타이머 제거(중복 방지)
            for (i in 0 until dynamicContainer.childCount) {
                val child = dynamicContainer.getChildAt(i)
                if (child.tag == "timer_$step") {
                    dynamicContainer.removeView(child)
                    break
                }
            }

            val timerLayout = LayoutInflater.from(this).inflate(R.layout.timer_step_layout, null).apply {
                tag = "timer_$step"
            }

            val hourPicker = timerLayout.findViewById<NumberPicker>(R.id.numberPicker1)
            val minutePicker = timerLayout.findViewById<NumberPicker>(R.id.numberPicker2)
            val storeBtn = timerLayout.findViewById<AppCompatButton>(R.id.storeBtn)

            hourPicker.minValue = 0
            hourPicker.maxValue = 24
            minutePicker.minValue = 0
            minutePicker.maxValue = 59
            minutePicker.setFormatter { i -> String.format("%02d", i) }

            storeBtn.setOnClickListener {
                val hour = hourPicker.value
                val minute = minutePicker.value
                stepTimerMap[step] = hour to minute
                Toast.makeText(this, "STEP $step 타이머 저장됨 (${hour}시간 ${minute}분)", Toast.LENGTH_SHORT).show()
            }

            dynamicContainer.addView(timerLayout)

            timerLayout.post {
                val baseMarginPx = 32.dpToPx()
                (contentAddTwo.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = baseMarginPx + timerLayout.height + 15.dpToPx()
                }.also { contentAddTwo.layoutParams = it }

                (timerAddTwo.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = baseMarginPx + timerLayout.height + 15.dpToPx()
                }.also { timerAddTwo.layoutParams = it }
            }
        }

        contentAddTwo.setOnClickListener {
            val currentRecipeStepCount = stepRecipeCountMap[step] ?: 2

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(45, 38, 45, 0) }
            }

            val stepPrefix = TextView(this).apply {
                text = "$step-$currentRecipeStepCount"
                textSize = 13f
                setTextColor(Color.parseColor("#2B2B2B"))
                setPadding(0, 0, 12, 0)
            }

            val edit = EditText(this).apply {
                id = View.generateViewId()
                tag = "$step-$currentRecipeStepCount"
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                hint = "레시피를 입력해주세요."
                textSize = 13f
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#A1A9AD"))
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1
                ).apply { setMargins(45, 12, 45, 0) }
                setBackgroundColor(Color.parseColor("#D9D9D9"))
            }

            stepRecipeCountMap[step] = currentRecipeStepCount + 1

            row.addView(stepPrefix)
            row.addView(edit)
            cookOrderRecipeContainerAdd.addView(row)
            cookOrderRecipeContainerAdd.addView(divider)

            divider.post {
                val bottom = divider.top + (divider.layoutParams as ViewGroup.MarginLayoutParams).height
                (contentAddTwo.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = bottom + 15.dpToPx()
                }.also { contentAddTwo.layoutParams = it }

                (timerAddTwo.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = bottom + 15.dpToPx()
                }.also { timerAddTwo.layoutParams = it }
            }
        }

        stepContainer.addView(newStepLayout)
    }

    // 레시피 조리순서 입력 데이터를 추출하고 저장하는 함수
    private fun saveRecipeSteps(): List<String> {
        val recipeSteps = mutableListOf<String>()
        val stepTextMap = mutableMapOf<String, String>()

        val containers = listOf(cookOrderRecipeContainer, stepContainer)

        containers.forEach { container ->
            traverseViews(container) { view ->
                if (view is EditText) {
                    val tag = view.tag?.toString()
                    val text = view.text.toString().trim()
                    if (!tag.isNullOrEmpty() && tag.contains("-") && text.isNotEmpty()) {
                        stepTextMap[tag] = text
                    }
                }
            }
        }

        val sortedKeys = stepTextMap.keys
            .filter { it.contains("-") && it.split("-").size == 2 }
            .sortedWith(compareBy(
                { it.split("-")[0].toIntOrNull() ?: 0 },
                { it.split("-")[1].toIntOrNull() ?: 0 }
            ))

        var currentStep = ""
        val stepBuffer = mutableListOf<String>()
        val resultSteps = mutableListOf<String>()

        for (key in sortedKeys) {
            val step = key.split("-")[0]
            val text = stepTextMap[key] ?: continue

            if (currentStep != step) {
                if (stepBuffer.isNotEmpty()) {
                    resultSteps.add("STEP $currentStep: ${stepBuffer.joinToString(" → ")}")
                    stepBuffer.clear()
                }
                currentStep = step
            }

            stepBuffer.add(text)
        }

        // 마지막 step 저장
        if (stepBuffer.isNotEmpty()) {
            resultSteps.add("STEP $currentStep: ${stepBuffer.joinToString(" → ")}")
        }

        // 디버깅 로그
        resultSteps.forEachIndexed { index, step ->
            Log.d("RecipeStep", "Step ${index + 1}: $step")
        }

        return resultSteps
    }

    // 뷰를 재귀적으로 탐색하는 함수
    private fun traverseViews(view: View, action: (View) -> Unit) {
        action(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                traverseViews(view.getChildAt(i), action)
            }
        }
    }

    // 뷰 전체를 순회하는 함수
    private fun traverseViews(viewGroup: ViewGroup, action: (View) -> Unit) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            action(child)
            if (child is ViewGroup) {
                traverseViews(child, action) // 재귀 호출로 하위 뷰 그룹까지 탐색
            }
        }
    }

    private fun startTimer() {
        // 입력된 시간 가져오기
        val hours = parseEditText(hourEditText)
        val minutes = parseEditText(minuteEditText)

        // 시간을 밀리초 단위로 변환
        timeInMillis = ((hours * 60L) + minutes) * 60 * 1000

        // 타이머가 0 이하라면 실행하지 않음
        if (timeInMillis <= 0) return

        // 기존 타이머 취소 (새로 시작할 때 중복 방지)
        countDownTimer?.cancel()

        // 타이머 실행
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val displayHours = totalSeconds / 3600
                val displayMinutes = (totalSeconds % 3600) / 60

                hourEditText.setText(String.format("%02d", displayHours))
                minuteEditText.setText(String.format("%02d", displayMinutes))
            }

            override fun onFinish() {
                resetTimer()
            }
        }.start()
    }

    private fun saveDraftThenPublish(isPublic: Boolean) {
        val token = App.prefs.token ?: ""
        val id = draftId ?: run {
            Toast.makeText(this, "초안 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val dto = buildDraftDto() // ← 현재 UI를 그대로 직렬화 (동적 대체재료 포함)
        RetrofitInstance.apiService.updateDraft("Bearer $token", id, dto)
            .enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                override fun onResponse(
                    call: retrofit2.Call<RecipeDraftDto>,
                    response: retrofit2.Response<RecipeDraftDto>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@RecipeWriteImageActivity, "발행 전 저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    // 저장 성공 후 발행
                    RetrofitInstance.apiService.publishDraft("Bearer $token", id, PublishRequest(isPublic = isPublic))
                        .enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                            override fun onResponse(
                                call: retrofit2.Call<RecipeDraftDto>,
                                response: retrofit2.Response<RecipeDraftDto>
                            ) {
                                if (response.isSuccessful) {
                                    val published = response.body()
                                    val recipeId = published?.recipeId
                                    Toast.makeText(this@RecipeWriteImageActivity, "발행 완료", Toast.LENGTH_SHORT).show()
                                    if (recipeId != null) {
                                        startActivity(Intent(this@RecipeWriteImageActivity, RecipeSeeMainActivity::class.java).apply {
                                            putExtra("recipeId", recipeId)
                                        })
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(this@RecipeWriteImageActivity, "발행 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: retrofit2.Call<RecipeDraftDto>, t: Throwable) {
                                Toast.makeText(this@RecipeWriteImageActivity, "네트워크 오류(발행): ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                override fun onFailure(call: retrofit2.Call<RecipeDraftDto>, t: Throwable) {
                    Toast.makeText(this@RecipeWriteImageActivity, "네트워크 오류(저장): ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun resetTimer() {
        countDownTimer?.cancel() // 실행 중인 타이머 중지
        countDownTimer = null // 타이머 객체 초기화

        hourEditText.setText("00") // 시간 초기화
        minuteEditText.setText("00") // 분 초기화
    }

    // 조리순서 타이머 EditText에서 숫자 가져오기 (비어있으면 0 반환)
    private fun parseEditText(editText: EditText): Int {
        return editText.text.toString().trim().toIntOrNull() ?: 0
    }


    // 레시피 세부설정 드롭다운 열기
    private fun detailSettleOpenDropDown(levelBoxChoice: ConstraintLayout, requiredTimeAndTag: ConstraintLayout) {
        levelBoxChoice.visibility = View.VISIBLE

        // requiredTimeAndTag 위치 조정
        val params = requiredTimeAndTag.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 37.dpToPx()
        requiredTimeAndTag.layoutParams = params
    }

    // 레시피 세부설정 드롭다운 닫기 및 recipeName 위치 복원
    private fun detailSettleCloseDropDown(levelBoxChoice: ConstraintLayout, requiredTimeAndTag: ConstraintLayout) {
        levelBoxChoice.visibility = View.GONE

        // requiredTimeAndTag 위치 복원
        val params = requiredTimeAndTag.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 20.dpToPx()
        requiredTimeAndTag.layoutParams = params
    }
    //이미지선택
    private fun displaySelectedImage(uri: Uri, targetContainer: LinearLayout) {
        val imageView = ImageView(this)
        imageView.setImageURI(uri)
        val layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
        imageView.layoutParams = layoutParams
        targetContainer.addView(imageView) // 선택한 컨테이너에 이미지 추가
        Log.d("RecipeWriteImageActivity", "이미지 추가 완료! 대상 컨테이너: ${targetContainer.id}")
    }

    //백엔드 서버에 이미지 업로드
    fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        uploadsInFlight++  // ⭐ 업로드 시작 카운트 +1

        val file = uriToFile(this, uri) ?: run {
            uploadsInFlight--          // ⭐ 예외 경로에서도 -1
            callback(null)
            return
        }
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = App.prefs.token ?: ""
        if (token.isEmpty()) {
            Log.e("Upload", "토큰이 없음!")
            uploadsInFlight--          // ⭐ -1
            callback(null)
            return
        }

        Log.d("Upload", "이미지 업로드 시작 - 파일명: ${file.name}, 크기: ${file.length()} 바이트")

        RetrofitInstance.apiService.uploadImage("Bearer $token", body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.string()
                        Log.d("Upload", "이미지 업로드 성공! URL: $imageUrl")
                        callback(imageUrl)
                    } else {
                        Log.e("Upload", "이미지 업로드 실패: 응답 코드 ${response.code()}, 오류 메시지: ${response.errorBody()?.string()}")
                        callback(null)
                    }
                    uploadsInFlight--   // ⭐ (B) onResponse 마지막에 -1
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "네트워크 요청 실패: ${t.message}")
                    callback(null)
                    uploadsInFlight--   // ⭐ (C) onFailure 마지막에 -1
                }
            })
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        var fileName: String? = null

        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                it.moveToFirst()
                fileName = it.getString(nameIndex)
            }
        }

        // 파일명이 비어있으면 기본 파일명 설정
        if (fileName.isNullOrEmpty()) {
            fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        }

        val file = File(context.cacheDir, fileName)

        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun ensureFirstStepInflated(): View {
        // stepContainer가 비어 있으면 item_step을 하나 붙여서 "STEP 1" 뷰 트리를 만든다
        return if (stepContainer.childCount == 0) {
            val v = LayoutInflater.from(this)
                .inflate(R.layout.item_step, stepContainer, false)

            // 기본 태그/라벨 세팅
            v.findViewById<TextView>(R.id.stepOne)?.text = "STEP 1"
            v.findViewById<TextView>(R.id.stepLittleOne)?.text = "1-1"
            v.findViewById<EditText>(R.id.cookOrderRecipeWrite)?.tag = "1-1"

            // 카메라 버튼 리스너도 붙여줌 (기존 addNewStep과 동일한 로직)
            v.findViewById<ImageButton>(R.id.stepCamera)?.apply {
                visibility = View.VISIBLE
                isClickable = true
                setOnClickListener {
                    val container = v.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)
                    selectedContainer = container
                    pickImageLauncherForStepCamera.launch("image/*")
                }
            }

            stepContainer.addView(v)
            v
        } else {
            stepContainer.getChildAt(0)
        }
    }

    private fun loadIngredients() {
        RetrofitInstance.pantryApi.listAll("Bearer $token").enqueue(object : Callback<List<IngredientResponse>> {
            override fun onResponse(

                call: Call<List<IngredientResponse>>,
                response: Response<List<IngredientResponse>>
            ) {
                Log.d("왜안돼","나도몰라")
                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    Log.d("RecipeWriteImageActivity", "재료 리스트 수: ${list.size}")
                    list.forEach { ing ->
                        Log.d("RecipeWriteImageActivity", "재료: id=${ing.id}, name=${ing.nameKo}, icon=${ing.iconUrl}")
                    }
                    allIngredients.clear()
                    allIngredients.addAll(list)
                    renderIngredientButtons(allIngredients) // 처음엔 전체 출력
                } else {
                    Toast.makeText(this@RecipeWriteImageActivity, "조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<IngredientResponse>>, t: Throwable) {
                Toast.makeText(this@RecipeWriteImageActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterIngredients(keyword: String) {
        val filtered = if (keyword.isBlank()) allIngredients
        else allIngredients.filter { it.nameKo.contains(keyword) }
        renderIngredientButtons(filtered)
    }

    private fun renderIngredientButtons(list: List<IngredientResponse>) {
        container.removeAllViews()

        for (item in list) {
            val view = layoutInflater.inflate(R.layout.item_ingredient_button, container, false)

            val rect = view.findViewById<View>(R.id.rect)
            val img = view.findViewById<ImageView>(R.id.image)
            val name = view.findViewById<TextView>(R.id.name)

            // 데이터 바인딩
            name.text = item.nameKo

            val fullImageUrl = RetrofitInstance.toIconUrl(item.iconUrl)

            Glide.with(img.context)
                .load(fullImageUrl)
                .error(R.drawable.image_juice_lemon)
                .into(img)

            // 선택/해제 토글
            view.setOnClickListener {
                val isSelected = selectedIngredients.contains(item.id)
                if (isSelected) {
                    rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    selectedIngredients.remove(item.id)

                    rectToRow[rect]?.let { row ->
                        materialContainer.removeView(row)
                        rectToRow.remove(rect)
                    }
                } else {
                    rect.setBackgroundResource(R.drawable.rounded_fridge_green)
                    selectedIngredients.add(item.id)

                    // ✅ 선택된 재료로 행 추가
                    val row = addNewItem(item, rect)
                    rectToRow[rect] = row
                }
            }

            container.addView(view)
        }
    }

    private fun splitAmount(src: String): Pair<String, String> {
        val trimmed = src.trim()
        val m = Regex("""^\s*([0-9]+(?:\.[0-9]+)?)\s*([^\d\s].*)?$""").find(trimmed)
        val qty = m?.groups?.get(1)?.value?.trim().orEmpty()
        val unit = m?.groups?.get(2)?.value?.trim().orEmpty()
        return qty to unit
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    fun sendRecipeToServer(recipe: RecipeRequest, onSuccess: (Long) -> Unit, onFailure: (() -> Unit)? = null) {
        val token = App.prefs.token
        RecipeRepository.uploadRecipe(token.toString(), recipe) { response ->
            if (response != null && response.recipeId != null) {
                createdRecipeId = response.recipeId.toLong()
                Toast.makeText(this, "레시피 업로드 성공!", Toast.LENGTH_SHORT).show()
                onSuccess(createdRecipeId!!)  // 서버에서 받은 id 전달
            } else {
                Toast.makeText(this, "레시피 업로드 실패3", Toast.LENGTH_SHORT).show()
                onFailure?.invoke()
            }
        }
    }

}
