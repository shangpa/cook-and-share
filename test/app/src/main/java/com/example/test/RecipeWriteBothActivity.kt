/*레시피 둘다*/
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.test.App.Companion.context
import com.example.test.RecipeWriteImageActivity.Companion
import com.example.test.Repository.RecipeRepository
import com.example.test.Utils.TabBarUtils
import com.example.test.model.CookingStep
import com.example.test.model.Ingredient
import com.example.test.model.RecipeRequest
import com.example.test.model.recipeDetail.ThumbnailResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Stack
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.test.model.MasterIngredient
import com.example.test.model.ingredients.IngredientResponse
import com.example.test.model.recipeDetail.PublishRequest
import com.example.test.model.recipeDetail.RecipeCreateResponse
import com.example.test.model.recipeDetail.RecipeDraftDto
import com.example.test.model.recipeDetail.RecipeIngredientReq
import com.example.test.model.recipeDetail.RecipeIngredientRes

private var draftId: Long? = null        // intent로 받아서 씀
private var recipeType: String = "BOTH"  // IMAGE | VIDEO | BOTH
private lateinit var materialContainer: LinearLayout
private lateinit var replaceMaterialContainer: LinearLayout
private lateinit var handlingMethodContainer: LinearLayout
private lateinit var cookOrderRecipeContainer: LinearLayout
private lateinit var addFixButton: AppCompatButton
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
private var recipe: RecipeRequest? = null
private var exoPlayer: ExoPlayer? = null

@androidx.media3.common.util.UnstableApi
@OptIn(UnstableApi::class)
class RecipeWriteBothActivity : AppCompatActivity() {
    private val allIngredients = mutableListOf<IngredientResponse>()   // 전체 목록
    private val selectedIngredients = mutableSetOf<Long>() // 선택된 재료 ID
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
                startActivityForResult(intent, RecipeWriteImageActivity.EDIT_IMAGE_REQUEST_CODE)
            }
        }
    private lateinit var container: LinearLayout
    // 대표사진 이미지 업로드
    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                isPickingRepresentImage = true
                val destinationUri = Uri.fromFile(File(cacheDir, "cropped_represent_${System.currentTimeMillis()}.jpg"))
                val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                    putExtra("imageUri", it.toString())
                }
                startActivityForResult(intent,
                    com.example.test.RecipeWriteImageActivity.EDIT_IMAGE_REQUEST_CODE
                )
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
    // 레시피 데이터 수집용 (BothActivity 클래스 맨 위쪽에)
    private var filteredIngredients: List<Pair<String, String>> = emptyList()
    private var replaceIngredients: List<String> = emptyList()
    private var handlingMethods: List<String> = emptyList()
    private var cookingSteps: MutableList<String> = mutableListOf()

    // Step 관련 (타이머/이미지)
    private var stepTimerMap: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()
    //동영상
    private var selectedVideoUri: Uri? = null
    private var recipeVideoUrl: String? = null
    private val videoTrimLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val trimmedUri = result.data?.getParcelableExtra<Uri>("trimmedUri")
            trimmedUri?.let {
                selectedVideoUri = it
                showVideoInfo(it)
                showVideoPreview(it)
                uploadVideoToServer(it)  // ✅ 트리밍된 영상 업로드
            }
        }
    }

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, VideoTrimActivity::class.java)
            intent.putExtra("videoUri", it)
            videoTrimLauncher.launch(intent)
        }
    }

    companion object {
        const val EDIT_IMAGE_REQUEST_CODE = 1001
    }

    private val tabCompleted = BooleanArray(7) { false }
    private lateinit var progressBars: List<View>
    private var selectedIndex = 0
    private lateinit var committedCompleted: BooleanArray
    private var lastSelectedIndex = 0
    private var isSwitching = false
    private val rectToRow = mutableMapOf<View, View>()
    private val selectedRects = mutableSetOf<View>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both)

        // 배열 초기화
        committedCompleted = BooleanArray(tabCompleted.size) { false }
        lastSelectedIndex = selectedIndex

        draftId = intent.getLongExtra("draftId", -1).takeIf { it > 0 }
        intent.getStringExtra("recipeType")?.let { recipeType = it } // IMAGE | VIDEO | BOTH

        TabBarUtils.setupTabBar(this)

        // 재료
        materialContainer = findViewById(R.id.materialContainer)
        container = findViewById(R.id.categoryButtonContainer)
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
        val recipeWriteCookVideoLayout = findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout)


        // 레시피 재료 선언
        val recipeWriteMaterialLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout)
        val materialCook = findViewById<EditText>(R.id.materialCook)
        val material = findViewById<EditText>(R.id.material)
        val measuring = findViewById<EditText>(R.id.measuring)
        val divideRectangleBarSix = findViewById<View>(R.id.divideRectangleBarSix)
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
        val replaceMaterialAddFixButton = findViewById<AppCompatButton>(R.id.replaceMaterialAddFixButton)
        val replaceMaterialFoodName = findViewById<TextView>(R.id.replaceMaterialfoodName)
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
        val handlingMethodAddFixButton = findViewById<AppCompatButton>(R.id.handlingMethodAddFixButton)
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
        val cookVideoKoreanFood = findViewById<TextView>(R.id.cookVideoKoreanFood)

        // 레시피 조리영상 선언
        val cookVideoFoodName = findViewById<TextView>(R.id.cookVideoFoodName)
        val cookOrderKoreanFood = findViewById<TextView>(R.id.cookOrderKoreanFood)


        // 레시피 세부설정 선언
        val recipeWriteDetailSettleLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        val requiredTimeAndTag = findViewById<ConstraintLayout>(R.id.requiredTimeAndTag)
        val representImageContainer = findViewById<LinearLayout>(R.id.representImageContainer)
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
        val seven = findViewById<TextView>(R.id.seven)

        // 레시피 레이아웃 선언
        layoutList = listOf(
            recipeWriteTitleLayout,
            recipeWriteMaterialLayout,
            recipeWriteReplaceMaterialLayout,
            recipeWriteHandlingMethodLayout,
            recipeWriteCookVideoLayout,
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
        six.setOnClickListener { changeLayout(recipeWriteCookVideoLayout) }
        five.setOnClickListener { changeLayout(recipeWriteCookOrderLayout) }
        seven.setOnClickListener{ changeLayout(recipeWriteDetailSettleLayout)}

        progressBars = listOf(
            findViewById(R.id.barOne),
            findViewById(R.id.barTwo),
            findViewById(R.id.barThree),
            findViewById(R.id.barFour),
            findViewById(R.id.barFive),
            findViewById(R.id.barSix),
            findViewById(R.id.barSeven)
        )
        loadIngredients()

        // 레시피 이전으로 버튼 클릭시 이전 화면으로 이동
        beforeButton.setOnClickListener {
            // 타이틀 화면이면 메인으로 이동
            if (currentLayout.id == R.id.recipeWriteTitleLayout) {
                val intent = Intent(this, RecipeWriteMain::class.java)
                startActivity(intent)
                finish()
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

            // 마지막 화면이 detailSettle이면 contentCheck로 이동
            if (currentLayout.id == R.id.recipeWriteDetailSettleLayout) {
                findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout).visibility = View.GONE
                findViewById<View>(R.id.cookOrderTapBar).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE

                // 대표이미지 로드(빈값/절대경로 대비)
                val representativeImage = findViewById<ImageView>(R.id.representativeImage)
                val fullImageUrl = when {
                    mainImageUrl.isBlank() -> null
                    mainImageUrl.startsWith("http", ignoreCase = true) -> mainImageUrl.trim()
                    else -> (RetrofitInstance.BASE_URL + mainImageUrl.trim())
                }
                fullImageUrl?.let { Glide.with(this).load(it).into(representativeImage) }

                // 선택된 카테고리/제목
                val categoryText = koreanFood.text.toString()
                val recipeTitle = recipeTitleWrite.text.toString()

                // 재료(고정 + 동적)
                val ingredients = mutableListOf<Pair<String, String>>()

                // 동적 재료 수집
                for (i in 0 until materialContainer.childCount) {
                    val row = materialContainer.getChildAt(i)

                    // 각 행(row) 내부에서 ID로 View를 정확히 찾습니다.
                    val nameTextView = row.findViewById<TextView>(R.id.tvMaterialName)
                    val measuringEditText = row.findViewById<EditText>(R.id.etMeasuring)

                    if (nameTextView != null && measuringEditText != null) {
                        val materialName = nameTextView.text.toString()
                        val amount = measuringEditText.text.toString()

                        if (materialName.isNotBlank() && amount.isNotBlank()) {
                            ingredients.add(materialName to amount)
                        }
                    }
                }

                // 빈 값 제거
                val filteredIngredients = ingredients.filter { it.first.isNotBlank() && it.second.isNotBlank() }
                val ingredientsForRequest = ingredients.map { (name, qty) ->
                    // 이름으로 전체 재료 목록(allIngredients)에서 원본 데이터(id 포함) 찾기
                    val originalIngredient = allIngredients.find { it.nameKo == name }
                    val ingredientId = originalIngredient?.id

                    // 서버가 요구하는 MasterIngredient 모델로 변환
                    MasterIngredient(
                        id = ingredientId,
                        name = name,
                        amount = qty
                    )
                }
                // 미리보기 UI 업데이트
                updateMaterialList(materialContainer, filteredIngredients)

                // 대체 재료/처리방법 원시 문자열
                val replaceRaw = listOf(
                    "${replaceMaterialName.text.toString().trim()} → ${replaceMaterial.text.toString().trim()}",
                    "${replaceMaterialMaterialTwo.text.toString().trim()} → ${replaceMaterialTwo.text.toString().trim()}"
                ).filter { it.isNotBlank() }

                val handlingRaw = listOf(
                    "${handlingMethodName.text.toString().trim()} : ${handlingMethod.text.toString().trim()}",
                    "${handlingMethodMaterialTwo.text.toString().trim()} : ${handlingMethodTwo.text.toString().trim()}"
                ).filter { it.isNotBlank() }

                // ✅ 안전 분해 (limit=2, getOrNull)
                val replacePairs = replaceRaw.mapNotNull {
                    val parts = it.split(" → ", limit = 2)
                    val a = parts.getOrNull(0)?.trim().orEmpty()
                    val b = parts.getOrNull(1)?.trim().orEmpty()
                    if (a.isEmpty() && b.isEmpty()) null else a to b
                }
                val handlingPairs = handlingRaw.mapNotNull {
                    val parts = it.split(" : ", limit = 2)
                    val a = parts.getOrNull(0)?.trim().orEmpty()
                    val b = parts.getOrNull(1)?.trim().orEmpty()
                    if (a.isEmpty() && b.isEmpty()) null else a to b
                }

                // 조리 순서
                val cookingStepsText = saveRecipeSteps()

                // 시간/태그
                val cookingHour   = zero.text.toString().takeIf { it.isNotBlank() }?.toInt() ?: 0
                val cookingMinute = halfHour.text.toString().takeIf { it.isNotBlank() }?.toInt() ?: 0
                val recipeTag = detailSettleRecipeTitleWrite.text.toString()

                // 상단 확인 화면 표시
                findViewById<TextView>(R.id.contentCheckFoodName).text = recipeTitle
                findViewById<TextView>(R.id.contentCheckKoreanFood).text = categoryText
                findViewById<TextView>(R.id.contentCheckBeginningLevel).text = elementaryLevel.text
                findViewById<TextView>(R.id.foodNameTwo).text = recipeTag
                findViewById<TextView>(R.id.contentCheckZero).text = cookingHour.toString()
                findViewById<TextView>(R.id.contentCheckHalfHour).text = cookingMinute.toString()

                findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.contentCheckTapBar).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout).visibility = View.GONE
                findViewById<View>(R.id.cookOrderTapBar).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE

                val totalCookingTime = (cookingHour * 60) + cookingMinute
                val difficulty = elementaryLevel.text.toString()
                val categoryEnum = mapCategoryToEnum(categoryText)
                val gson = Gson()

                // ✅ RecipeRequest 구성도 안전 분해로 교체
                recipe = RecipeRequest(
                    title = recipeTitle,
                    category = categoryEnum,
                    //재료 수정중
                    ingredients = ingredientsForRequest,
                    alternativeIngredients = gson.toJson(
                        replacePairs.map { (orig, repl) -> Ingredient(orig, repl) }
                    ),
                    handlingMethods = gson.toJson(
                        handlingPairs.map { (ing, method) -> "$ing : $method" }
                    ),
                    cookingSteps = gson.toJson(
                        cookingStepsText.mapIndexed { index, stepText ->
                            val step = index + 1
                            val (h, m) = stepTimerMap[step] ?: (0 to 0)
                            val totalSeconds = h * 3600 + m * 60
                            val imageUrl = stepImages[step].orEmpty()
                            CookingStep(
                                step = step,
                                description = stepText,
                                mediaUrl = imageUrl,
                                mediaType = "IMAGE",
                                timeInSeconds = totalSeconds
                            )
                        }
                    ),
                    mainImageUrl = mainImageUrl,
                    difficulty = difficulty,
                    tags = recipeTag,
                    cookingTime = totalCookingTime,
                    servings = 2,
                    isPublic = true,
                    videoUrl = recipeVideoUrl.orEmpty()
                )

                Log.d("RecipeRequest", gson.toJson(recipe))

                // 미리보기 리스트
                updateMaterialListView(
                    findViewById(R.id.materialList),
                    filteredIngredients,
                    replacePairs,
                    handlingPairs
                )

                recipe?.let { local ->
                    val type = object : TypeToken<List<CookingStep>>() {}.type
                    val cookingStepList: List<CookingStep> = gson.fromJson(local.cookingSteps, type)
                    addCookingSteps(this, cookingStepList)
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
            val token = "Bearer ${App.prefs.token}"
            val dto = collectAllFields(isDraft = true)
            Log.d("DraftDTO", Gson().toJson(dto))
            if (draftId == null) {
                RetrofitInstance.apiService.createDraft(token, dto)
                    .enqueue(object: Callback<RecipeCreateResponse> {
                        override fun onResponse(c: Call<RecipeCreateResponse>, r: Response<RecipeCreateResponse>) {
                            if (r.isSuccessful) {
                                draftId = r.body()?.recipeId
                                toast("임시저장 성공")
                                // 홈으로 이동하거나, 모달 닫기
                                finish()
                            } else toast("임시저장 실패")
                        }
                        override fun onFailure(c: Call<RecipeCreateResponse>, t: Throwable) {
                            toast("네트워크 오류")
                        }
                    })
            } else {
                RetrofitInstance.apiService.updateDraft(token, draftId!!, dto)
                    .enqueue(object: Callback<RecipeDraftDto> {
                        override fun onResponse(c: Call<RecipeDraftDto>, r: Response<RecipeDraftDto>) {
                            if (r.isSuccessful) {
                                toast("임시저장 업데이트 완료")
                                finish()
                            } else toast("업데이트 실패")
                        }
                        override fun onFailure(c: Call<RecipeDraftDto>, t: Throwable) {
                            toast("네트워크 오류")
                        }
                    })
            }
        }
        // 레시피 탭바와 바 선언
        textViewList = listOf(one, two, three, four, five, six, seven)
        underlineBar = findViewById(R.id.divideRectangleBarTwentythree)
        underlineBar.post {
            val textView = findViewById<TextView>(R.id.one)
            val targetX = textView.x + (textView.width / 2) - (indicatorBar.width / 2)
            indicatorBar.x = targetX
        }

        // 레시피 탭바 텍스트 클릭시 해당 텍스트 색 바뀌고 바 아래로 움직임
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
                replaceMaterialFoodName.text = text
                handlingMethodFoodName.text = text
                cookOrderFoodName.text = text
                cookVideoFoodName.text = text
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
            cookVideoKoreanFood.text = text
            cookOrderKoreanFood.text = text
            detailSettleKoreanFood.text = text
        }

        // koreanFood 값이 변경될 때 자동 반영
        koreanFood.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateKoreanFoodTextViews(koreanFood.text.toString())
        }

        //재료 채워지면 계속하기 버튼 바뀜
        material.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        measuring.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //대체재료 채워지면 계속하기 바뀜
        replaceMaterialName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        replaceMaterial.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //처리방법 채워지면 계속하기 바뀜
        handlingMethodName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        handlingMethod.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkAndUpdateContinueButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //조리순서 채워지면 끝내기 바뀜
        val stepEditText = findViewById<EditText>(R.id.cookOrderRecipeWrite)

        stepEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTabs()
                checkCookOrderAndUpdateEndButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        //세부설정 채워지면 계속하기 바뀜
        val cookingTimeEditText = findViewById<EditText>(R.id.zero)
        val tagEditText = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite)

        cookingTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkAndUpdateContinueButton()
                checkTabs() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        tagEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkAndUpdateContinueButton()
                checkTabs() }
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

        stepContainer = findViewById(R.id.stepContainer) // stepContainer 초기화

        // stepCamera용 런처 (조리 순서 이미지)
        pickImageLauncherForStepCamera =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                        putExtra("imageUri", it.toString())
                        putExtra("isStepImage", true)
                    }
                    startActivityForResult(intent,
                        com.example.test.RecipeWriteImageActivity.EDIT_IMAGE_REQUEST_CODE
                    )
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
                timerLayout.visibility= View.GONE
            }
            stepContainer.addView(timerLayout)

            // 🧩 타이머 추가 후 버튼 위치 재조정
            timerLayout.post {
                val timerHeight = timerLayout.measuredHeight
                val layoutParamsTimer = timerAdd.layoutParams as ViewGroup.MarginLayoutParams
                val layoutParamsContent = contentAdd.layoutParams as ViewGroup.MarginLayoutParams
                timerAdd.layoutParams = layoutParamsTimer
                contentAdd.layoutParams = layoutParamsContent
            }
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
        //동영상 카메라
        val cookVideoCamera = findViewById<ImageButton>(R.id.cookVideoCamera)
        cookVideoCamera.setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }

        // 레시피 조리순서 끝내기 버튼 클릭시
        endFixButton.setOnClickListener {
            layoutHistory.push(currentLayout)

            val detailSettleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
            showOnlyLayout(detailSettleLayout)

            updateSelectedTab(seven)
            moveUnderlineBar(seven)
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
            val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
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
            onClickPublish()  // draftId 있으면 update→발행, 없으면 create→발행
        }

        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener {
            onClickPublish()  // 동일하게 처리
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

        draftId?.let { loadDraftAndBind(it) }
    }

    private fun loadIngredients() {
        val token = App.prefs.token ?: ""
        RetrofitInstance.pantryApi.listAll(token).enqueue(object : Callback<List<IngredientResponse>> {
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
                    Toast.makeText(this@RecipeWriteBothActivity, "조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<IngredientResponse>>, t: Throwable) {
                Toast.makeText(this@RecipeWriteBothActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
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
    // [수정] 기존 addNewItem 함수들을 모두 지우고 이걸로 교체하세요.
    private fun addNewItem(selected: IngredientResponse, sourceRect: View?): View {
        val view = layoutInflater.inflate(R.layout.item_recipe_material, materialContainer, false)

        val tvName = view.findViewById<TextView>(R.id.tvMaterialName)
        val etMeasuring = view.findViewById<EditText>(R.id.etMeasuring)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        tvName.text = selected.nameKo

        view.tag = (selected.id ?: -1L)

        btnDelete.setOnClickListener {
            materialContainer.removeView(view)
            itemCount--

            val rectToClear = (sourceRect ?: view.tag) as? View
            rectToClear?.let { rect ->
                rectToRow.remove(rect)
                if (selectedRects.remove(rect)) {
                    rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                }
            }
        }
        materialContainer.addView(view)
        return view
    }

    private fun loadDraftAndBind(id: Long) {
        showLoading(true)
        val token = "Bearer ${App.prefs.token}"

        RetrofitInstance.apiService.getMyDraftById(token, id)
            .enqueue(object: Callback<RecipeDraftDto> {
                override fun onResponse(
                    call: Call<RecipeDraftDto>,
                    response: Response<RecipeDraftDto>
                ) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val dto = response.body() ?: return
                        draftId = dto.recipeId   // ★ draftId 세팅
                        bindCommon(dto)
                        bindMediaFields(dto)
                    } else {
                        Toast.makeText(this@RecipeWriteBothActivity, "임시저장 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<RecipeDraftDto>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@RecipeWriteBothActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 3-1) 화면에 값 뿌리기
    private fun bindMediaFields(dto: RecipeDraftDto?) {
        // 대표 이미지 (미리보기 컨테이너에 이미지 추가)
        dto?.mainImageUrl?.takeIf { it.isNotBlank() }?.let { url ->
            // BASE_URL이 필요하면 붙여서 로드
            val full = if (url.startsWith("http")) url else RetrofitInstance.BASE_URL + url
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(336), dpToPx(261))
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            Glide.with(this).load(full).into(iv)
            findViewById<LinearLayout>(R.id.representImageContainer).addView(iv)
            mainImageUrl = url
        }

        // 동영상
        dto?.videoUrl?.takeIf { it.isNotBlank() }?.let { vUrl ->
            recipeVideoUrl = vUrl
            // 미리보기 텍스트/플레이어 UI 갱신
            showVideoInfo(Uri.parse(vUrl))
            // 네이티브 파일 Uri 아닐 수 있어 바로 Player에 못 넣을 수도 있다(서버 URL이면 가능)
            // 가능하면 showVideoPreview(Uri.parse(vUrl)) 호출
        }
        bindIngredients(dto?.ingredients)   // ✅ 이제 List 타입으로 맞음
        recipeType = dto?.recipeType ?: "BOTH"
    }

    private fun onClickSaveDraft() {
        val token = "Bearer ${App.prefs.token}"
        val dto = collectAllFields(isDraft = true)

        showLoading(true)
        if (draftId == null) {
            RetrofitInstance.apiService.createDraft(token, dto).enqueue(object : retrofit2.Callback<RecipeCreateResponse> {
                override fun onResponse(c: retrofit2.Call<RecipeCreateResponse>, r: retrofit2.Response<RecipeCreateResponse>) {
                    showLoading(false)
                    if (r.isSuccessful) {
                        draftId = r.body()?.recipeId
                        Toast.makeText(this@RecipeWriteBothActivity, "임시저장 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RecipeWriteBothActivity, "임시저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(c: retrofit2.Call<RecipeCreateResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@RecipeWriteBothActivity, t.message ?: "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            RetrofitInstance.apiService.updateDraft(token, draftId!!, dto).enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                override fun onResponse(c: retrofit2.Call<RecipeDraftDto>, r: retrofit2.Response<RecipeDraftDto>) {
                    showLoading(false)
                    if (r.isSuccessful) {
                        Toast.makeText(this@RecipeWriteBothActivity, "임시저장 업데이트 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RecipeWriteBothActivity, "업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(c: retrofit2.Call<RecipeDraftDto>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@RecipeWriteBothActivity, t.message ?: "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    // === collectAllFields 수정 ===
    private fun collectAllFields(isDraft: Boolean): RecipeDraftDto {
        val gson = Gson()

        val ingredientsList = collectIngredientsForDraftRes()
        val altJson         = collectAlternativeOrNull()?.let { gson.toJson(it) } ?: "[]"
        val handlingJson    = collectHandlingOrNull()?.let { gson.toJson(it) } ?: "[]"
        val stepsJson       = collectCookingStepsJson() ?: "[]"

        val titleTxt    = getEt(R.id.recipeTitleWrite)
        val categoryTxt = mapCategoryToEnum(getTv(R.id.koreanFood)).ifEmpty { "etc" }
        val difficulty  = getTv(R.id.elementaryLevel).ifEmpty { "" }
        val tagsTxt     = getEt(R.id.detailSettleRecipeTitleWrite)

        val hours   = getEt(R.id.zero).toIntOrNull() ?: 0
        val minutes = getEt(R.id.halfHour).toIntOrNull() ?: 0
        val cookingMin = (hours * 60 + minutes).takeIf { it > 0 }

        return RecipeDraftDto(
            recipeId = draftId,
            title = titleTxt,
            category = categoryTxt,
            ingredients = ingredientsList,            // ✅ String 대신 List로 세팅
            alternativeIngredients = altJson,         // 그대로 String(JSON)
            handlingMethods = handlingJson,           // 그대로 String(JSON)
            cookingSteps = stepsJson,                 // 그대로 String(JSON)
            mainImageUrl = mainImageUrl.ifBlank { "" },
            difficulty = difficulty,
            tags = tagsTxt,
            cookingTime = cookingMin,
            servings = null,
            isPublic = null,
            videoUrl = recipeVideoUrl.orEmpty(),
            recipeType = "BOTH",
            isDraft = isDraft
        )
    }

    // 5. 서버에 보낼 데이터를 위해 UI의 재료 정보를 읽어오는 로직 (collectAllFields 함수 내부)
    private fun collectIngredientsForDraftRes(): List<RecipeIngredientRes>? {
        val result = mutableListOf<RecipeIngredientRes>()

        // materialContainer의 모든 자식 뷰(재료 행)를 순회
        for (i in 0 until materialContainer.childCount) {
            val row = materialContainer.getChildAt(i)

            // 각 행에서 ID를 사용해 이름과 계량 값을 정확히 찾아옴
            val nameTextView = row.findViewById<TextView>(R.id.tvMaterialName)
            val measuringEditText = row.findViewById<EditText>(R.id.etMeasuring)

            if (nameTextView != null && measuringEditText != null) {
                val name = nameTextView.text.toString()
                val amountStr = measuringEditText.text.toString()

                if (name.isNotBlank() && amountStr.isNotBlank()) {
                    // 이름으로 allIngredients에서 id를 찾음
                    val originalIngredient = allIngredients.find { it.nameKo == name }
                    val id = originalIngredient?.id
                    val amount = amountStr.toDoubleOrNull()

                    if (id != null && amount != null) {
                        result.add(RecipeIngredientRes(id = id, name = name, amount = amount))
                    }
                }
            }
        }
        return result.takeIf { it.isNotEmpty() }
    }
    private fun parseCookingSteps(json: String?): List<CookingStep> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<CookingStep>>() {}.type
            com.google.gson.Gson().fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // === 3) 재료 모으기 (고정 6칸 + 동적 materialContainer) ===
    private fun collectIngredientsList(): List<Ingredient>? {
        val result = mutableListOf<Ingredient>()
        val container = findViewById<android.widget.LinearLayout?>(R.id.materialContainer)
        if (container != null) {
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                extractDynamicIngredientRow(row)?.let { result += it }
            }
        }

        return result.takeIf { it.isNotEmpty() }
    }

    // === 4) 대체 재료 수집 (고정 + 동적 replaceMaterialContainer) ===
    private fun collectAlternativeOrNull(): List<Ingredient>? {
        val result = mutableListOf<Ingredient>()

        // 고정 첫 줄
        val name0 = getEt(R.id.replaceMaterialName)
        val val0  = getEt(R.id.replaceMaterial)
        if (name0.isNotEmpty() || val0.isNotEmpty()) {
            result += Ingredient(name0, val0)
        }

        // 동적 영역 (replaceMaterialAddNewItem()이 0:name 1:value 순서라고 가정)
        val container = findViewById<android.widget.LinearLayout?>(R.id.replaceMaterialContainer)
        if (container != null) {
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                val cl = row as? androidx.constraintlayout.widget.ConstraintLayout ?: continue
                val nameEt = cl.getChildAt(0) as? android.widget.EditText
                val valEt  = cl.getChildAt(1) as? android.widget.EditText
                val n = nameEt?.text?.toString()?.trim().orEmpty()
                val v = valEt?.text?.toString()?.trim().orEmpty()
                if (n.isNotEmpty() || v.isNotEmpty()) result += Ingredient(n, v)
            }
        }

        return result.takeIf { it.isNotEmpty() }
    }


    private fun collectHandlingOrNull(): List<Ingredient>? {
        val out = mutableListOf<Ingredient>()

        // 고정 2줄: ingredient : method  →  name = ingredient, amount = method
        run {
            val ing  = findViewById<EditText>(R.id.handlingMethodName)?.text?.toString()?.trim().orEmpty()
            val meth = findViewById<EditText>(R.id.handlingMethod)?.text?.toString()?.trim().orEmpty()
            if (ing.isNotEmpty() && meth.isNotEmpty()) out += Ingredient(ing, meth)
        }
        run {
            val ing  = findViewById<EditText>(R.id.handlingMethodMaterialTwo)?.text?.toString()?.trim().orEmpty()
            val meth = findViewById<EditText>(R.id.handlingMethodTwo)?.text?.toString()?.trim().orEmpty()
            if (ing.isNotEmpty() && meth.isNotEmpty()) out += Ingredient(ing, meth)
        }

        // 동적: handlingMethodAddNewItem()에서 child 순서 0=ingredient, 1=method
        for (i in 0 until handlingMethodContainer.childCount) {
            val row = handlingMethodContainer.getChildAt(i) as? ConstraintLayout ?: continue
            val ingEt  = row.getChildAt(0) as? EditText ?: continue
            val methEt = row.getChildAt(1) as? EditText ?: continue

            val ing  = ingEt.text.toString().trim()
            val meth = methEt.text.toString().trim()
            if (ing.isNotEmpty() && meth.isNotEmpty()) {
                out += Ingredient(ing, meth)
            }
        }
        return if (out.isEmpty()) null else out
    }

    // 4. 임시저장된 데이터를 불러와 재료 UI를 복원하는 함수
    private fun bindIngredients(list: List<RecipeIngredientRes>?) {
        if (list.isNullOrEmpty()) return

        // 기존 UI 초기화
        materialContainer.removeAllViews()
        selectedIngredients.clear()
        rectToRow.clear()

        list.forEach { ingredientFromDraft ->
            // DTO의 id를 이용해 전체 재료 목록(allIngredients)에서 원본 정보 찾기
            val originalIngredient = allIngredients.find { it.id == ingredientFromDraft.id }

            if (originalIngredient != null) {
                // 선택 상태로 만듦
                selectedIngredients.add(originalIngredient.id)

                // UI 행 생성
                val row = addNewItem(originalIngredient, null) // sourceRect는 없으므로 null

                // 생성된 행에서 계량 EditText를 ID로 정확히 찾아 값을 설정
                val etMeasuring = row.findViewById<EditText>(R.id.etMeasuring)
                val amount = ingredientFromDraft.amount
                // Double 값을 정수/소수 형태에 맞게 변환하여 표시
                etMeasuring.setText(
                    if (amount != null && amount % 1.0 == 0.0) amount.toLong().toString()
                    else amount.toString()
                )
            }
        }
    }
    private fun mapCategoryToEnum(category: String): String = when (category) {
        "한식" -> "koreaFood"
        "양식" -> "westernFood"
        "일식" -> "japaneseFood"
        "중식" -> "chineseFood"
        "채식" -> "vegetarianDiet"
        "간식" -> "snack"
        "안주" -> "alcoholSnack"
        "반찬" -> "sideDish"
        "기타" -> "etc"
        else   -> "etc"
    }

    private fun bindCommon(dto: RecipeDraftDto) {
        // ── 제목/카테고리/난이도/태그/시간 ──
        findViewById<EditText>(R.id.recipeTitleWrite).setText(dto.title ?: "")
        findViewById<TextView>(R.id.koreanFood).text =
            dto.category?.let { mapEnumToKrCategory(it) } ?: "카테고리 선택"

        findViewById<TextView>(R.id.elementaryLevel).text = dto.difficulty ?: "난이도"
        findViewById<EditText>(R.id.detailSettleRecipeTitleWrite).setText(dto.tags ?: "")

        val totalMin = dto.cookingTime ?: 0
        findViewById<EditText>(R.id.zero).setText((totalMin / 60).toString())      // 시
        findViewById<EditText>(R.id.halfHour).setText((totalMin % 60).toString())  // 분

        // ── 재료(고정 + 동적) : List<RecipeIngredientReq>? 직접 바인딩 ──
        bindIngredients(dto.ingredients)

        // ── 대체 재료(문자열 JSON) ──
        val altList: List<Ingredient> = parseIngredients(dto.alternativeIngredients)
        findViewById<EditText>(R.id.replaceMaterialName).setText("")
        findViewById<EditText>(R.id.replaceMaterial).setText("")
        replaceMaterialContainer.removeAllViews()
        altList.forEachIndexed { idx, ing ->
            if (idx == 0) {
                findViewById<EditText>(R.id.replaceMaterialName).setText(ing.name)
                findViewById<EditText>(R.id.replaceMaterial).setText(ing.amount)
            } else {
                replaceMaterialAddNewItem()
                val row = replaceMaterialContainer.getChildAt(
                    replaceMaterialContainer.childCount - 1
                ) as? ConstraintLayout ?: return@forEachIndexed
                (row.getChildAt(0) as? EditText)?.setText(ing.name)
                (row.getChildAt(1) as? EditText)?.setText(ing.amount)
            }
        }

        // ── 사용된 재료 처리 방법(문자열 JSON) ──
        val handlingList: List<Ingredient> = parseIngredients(dto.handlingMethods)
        findViewById<EditText>(R.id.handlingMethodName).setText("")
        findViewById<EditText>(R.id.handlingMethod).setText("")
        handlingMethodContainer.removeAllViews()
        handlingList.forEachIndexed { idx, ing ->
            if (idx == 0) {
                findViewById<EditText>(R.id.handlingMethodName).setText(ing.name)
                findViewById<EditText>(R.id.handlingMethod).setText(ing.amount)
            } else {
                handlingMethodAddNewItem()
                val row = handlingMethodContainer.getChildAt(
                    handlingMethodContainer.childCount - 1
                ) as? ConstraintLayout ?: return@forEachIndexed
                (row.getChildAt(0) as? EditText)?.setText(ing.name)
                (row.getChildAt(1) as? EditText)?.setText(ing.amount)
            }
        }

        // ── 대표 이미지 ──
        dto.mainImageUrl?.takeIf { it.isNotBlank() }?.let { url ->
            val full = if (url.startsWith("http")) url else RetrofitInstance.BASE_URL + url
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(336), dpToPx(261))
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            Glide.with(this).load(full).into(iv)
            findViewById<LinearLayout>(R.id.representImageContainer).apply {
                removeAllViews()
                addView(iv)
            }
            mainImageUrl = url
        }

        // ── 조리 순서(문자열 JSON) ──
        dto.cookingSteps?.let { json ->
            val steps = parseCookingSteps(json)

            // 미리보기(읽기용)
            addCookingSteps(this, steps)

            // 편집 첫 줄
            findViewById<EditText>(R.id.cookOrderRecipeWrite)?.setText(
                steps.firstOrNull()?.description ?: ""
            )

            // 타이머/이미지 맵 복원
            stepTimerMap.clear()
            stepImages.clear()
            steps.forEach { s ->
                if (s.timeInSeconds > 0) {
                    stepTimerMap[s.step] = (s.timeInSeconds / 3600) to ((s.timeInSeconds % 3600) / 60)
                }
                if (!s.mediaUrl.isNullOrBlank()) {
                    stepImages[s.step] = s.mediaUrl ?: ""
                }
            }
        }
    }

    private fun mapEnumToKrCategory(enumName: String): String = when (enumName) {
        "koreaFood" -> "한식"
        "westernFood" -> "양식"
        "japaneseFood" -> "일식"
        "chineseFood" -> "중식"
        "vegetarianDiet" -> "채식"
        "snack" -> "간식"
        "alcoholSnack" -> "안주"
        "sideDish" -> "반찬"
        else -> "기타"
    }

    // "100 g" / "1 컵" 같이 수량+단위를 한 문자열로 저장했다면,
    // 가장 오른쪽 토큰을 단위로 추출(없으면 "개")
    private fun extractUnit(amountOrMeasure: String?): String {
        if (amountOrMeasure.isNullOrBlank()) return "개"
        val tokens = amountOrMeasure.trim().split("\\s+".toRegex())
        return if (tokens.size >= 2) tokens.last() else "개"
    }

    // "재료명 : 처리방법" 형태 문자열을 두 부분으로 안전 분리
    private fun splitHandling(s: String?): Pair<String, String> {
        if (s.isNullOrBlank()) return "" to ""
        val idx = s.indexOf(':').takeIf { it >= 0 } ?: s.indexOf('：').takeIf { it >= 0 } ?: -1
        return if (idx >= 0) {
            s.substring(0, idx).trim() to s.substring(idx + 1).trim()
        } else {
            // 구분자가 없으면 통째로 이름으로 보고 방법은 빈칸
            s.trim() to ""
        }
    }

    data class Ingredient(val name: String, val amount: String)

    data class CookingStep(
        val step: Int,
        val description: String,
        val mediaType: String? = "IMAGE",
        val mediaUrl: String? = null,
        val timeInSeconds: Int = 0
    )

    // Text/Unit 안전 추출
    private fun getEt(id: Int): String =
        (findViewById<android.widget.EditText?>(id)?.text?.toString()?.trim()).orEmpty()

    private fun getTv(id: Int): String =
        (findViewById<android.widget.TextView?>(id)?.text?.toString()?.trim()).orEmpty()

    // "수량 + 단위" 하나의 amount 문자열 만들기
    private fun joinAmount(qty: String, unit: String): String {
        val q = qty.trim(); val u = unit.trim()
        return when {
            q.isEmpty() && u.isEmpty() -> ""
            q.isNotEmpty() && u.isNotEmpty() -> "$q $u"
            else -> q.ifEmpty { u }           // 둘 중 하나만 있으면 그거만
        }
    }

    // ConstraintLayout(동적 재료 한 줄)에서 name/qty/unit 뽑기
    private fun extractDynamicIngredientRow(row: android.view.View): Ingredient? {
        // addNewItem()가 0:이름(EditText), 1:계량(EditText), 2:단위(TextView) 순서로 추가했다고 전제
        val cl = row as? androidx.constraintlayout.widget.ConstraintLayout ?: return null
        val nameEt = cl.getChildAt(0) as? android.widget.EditText
        val qtyEt  = cl.getChildAt(1) as? android.widget.EditText
        val unitTv = cl.getChildAt(2) as? android.widget.TextView
        val name = nameEt?.text?.toString()?.trim().orEmpty()
        val qty  = qtyEt?.text?.toString()?.trim().orEmpty()
        val unit = unitTv?.text?.toString()?.trim().orEmpty().let { if (it == "단위") "" else it }
        val amount = joinAmount(qty, unit)
        return if (name.isEmpty() && amount.isEmpty()) null else Ingredient(name, amount)
    }

    // 안전 파서: 문자열 배열 + 객체 배열 모두 커버
    private fun parseHandlingFlexible(json: String?): List<Pair<String, String>> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            // 1) 문자열 배열 먼저
            val strType = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            val arr = Gson().fromJson<List<String>>(json, strType) ?: emptyList()
            arr.mapNotNull { s ->
                if (s.isNullOrBlank()) return@mapNotNull null
                val parts = s.split(":", limit = 2)
                val name = parts.getOrNull(0)?.trim().orEmpty()
                val method = parts.getOrNull(1)?.trim().orEmpty()
                if (name.isEmpty() && method.isEmpty()) null else (name to method)
            }
        } catch (_: Exception) {
            try {
                // 2) 객체 배열
                val mapType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
                val list = Gson().fromJson<List<Map<String, Any?>>>(json, mapType)
                list.mapNotNull { m ->
                    val name = (m["name"] ?: m["first"])?.toString()?.trim().orEmpty()
                    val method = (m["amount"] ?: m["second"])?.toString()?.trim().orEmpty()
                    if (name.isEmpty() && method.isEmpty()) null else (name to method)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    // 재료/대체재료도 동일한 방식으로 안전 파싱
    private fun parseIngredientsFlexible(json: String?): List<Ingredient> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val objType = object : com.google.gson.reflect.TypeToken<List<Ingredient>>() {}.type
            com.google.gson.Gson().fromJson(json, objType)
        } catch (_: Exception) {
            try {
                val mapType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
                val list = com.google.gson.Gson().fromJson<List<Map<String, Any?>>>(json, mapType)
                list.mapNotNull { m ->
                    val name = (m["name"] ?: m["first"])?.toString()?.trim().orEmpty()
                    val amount = (m["amount"] ?: m["second"])?.toString()?.trim().orEmpty()
                    if (name.isEmpty() && amount.isEmpty()) null else Ingredient(name, amount)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    // === 6) 조리순서 수집 → 객체 배열 JSON ===
// UI에서 EditText.tag = "step-sub" 형태를 이미 쓰고 있으니 그 값 기준으로 정렬/수집
    private fun collectCookingStepsJson(): String? {
        val map = mutableMapOf<Pair<Int, Int>, String>()   // (step, sub) -> description

        val containers = listOf(
            findViewById<android.widget.LinearLayout?>(R.id.cookOrderRecipeContainer),
            findViewById<androidx.constraintlayout.widget.ConstraintLayout?>(R.id.stepContainer)
        )

        fun traverse(v: android.view.View, visit: (android.view.View) -> Unit) {
            visit(v)
            if (v is android.view.ViewGroup) {
                for (i in 0 until v.childCount) traverse(v.getChildAt(i), visit)
            }
        }

        containers.forEach { root ->
            if (root == null) return@forEach
            traverse(root) { view ->
                val et = view as? android.widget.EditText ?: return@traverse
                val tag = et.tag?.toString() ?: return@traverse
                if (!tag.contains("-")) return@traverse
                val parts = tag.split("-")
                val step = parts.getOrNull(0)?.toIntOrNull() ?: return@traverse
                val sub  = parts.getOrNull(1)?.toIntOrNull() ?: return@traverse
                val text = et.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) map[step to sub] = text
            }
        }

        if (map.isEmpty()) return null

        val list = map.toList()
            .sortedWith(compareBy({ it.first.first }, { it.first.second }))
            .map { (k, desc) ->
                val (step, _) = k
                val secs = (stepTimerMap[step]?.let { (h, m) -> h * 3600 + m * 60 }) ?: 0
                CookingStep(
                    step = step,
                    description = desc,
                    mediaType = "IMAGE",
                    mediaUrl = stepImages[step].orEmpty(),
                    timeInSeconds = secs
                )
            }

        return com.google.gson.Gson().toJson(list)
    }


    // 실제 조리순서 수집 로직
    private fun collectCookingSteps(): List<CookingStep> {
        // 태그 형식 "step-subStep" 인 EditText 들을 긁어 모음
        val perStepTexts = mutableMapOf<Int, MutableList<Pair<Int, String>>>()

        fun grabFrom(container: ViewGroup?) {
            if (container == null) return
            for (i in 0 until container.childCount) {
                val v = container.getChildAt(i)
                if (v is ViewGroup) {
                    grabFrom(v)
                } else if (v is EditText) {
                    val tagStr = v.tag?.toString() ?: continue
                    // ex) "1-1", "2-3" 형태만 집계
                    val m = Regex("^(\\d+)-(\\d+)$").matchEntire(tagStr) ?: continue
                    val step = m.groupValues[1].toInt()
                    val sub  = m.groupValues[2].toInt()
                    val text = v.text?.toString()?.trim().orEmpty()
                    if (text.isNotEmpty()) {
                        perStepTexts.getOrPut(step) { mutableListOf() }.add(sub to text)
                    }
                }
            }
        }

        // 기존 화면 컨테이너들에서 모두 수집
        grabFrom(cookOrderRecipeContainer)
        grabFrom(stepContainer)

        // step 오름차순, subStep 오름차순으로 정렬 후 한 줄로 합치기
        val result = mutableListOf<CookingStep>()
        perStepTexts.keys.sorted().forEach { step ->
            val joined = perStepTexts[step]
                ?.sortedBy { it.first }
                ?.joinToString(" → ") { it.second }
                ?.let { "STEP $step: $it" }
                ?: return@forEach

            // 시간/이미지 맵에서 보강 (이미 전역에 있으시죠: stepTimerMap, stepImages)
            val (h, m) = (stepTimerMap[step] ?: (0 to 0))
            val sec = h * 3600 + m * 60
            val media = stepImages[step]  // 없으면 null

            result.add(
                CookingStep(
                    step = step,
                    description = joined,
                    mediaType = "IMAGE",          // 필요 시 분기
                    mediaUrl = media,
                    timeInSeconds = sec
                )
            )
        }
        return result
    }

    // 안전 파서
    private fun parseIngredients(json: String?): List<Ingredient> {
        if (json.isNullOrBlank()) return emptyList()
        val gson = Gson()
        return try {
            gson.fromJson(json, object : com.google.gson.reflect.TypeToken<List<Ingredient>>() {}.type)
        } catch (_: Exception) {
            try {
                val list = gson.fromJson<List<Map<String, Any?>>>(json,
                    object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
                )
                list.mapNotNull { m ->
                    val name = (m["name"] ?: m["first"])?.toString()?.trim().orEmpty()
                    val amount = (m["amount"] ?: m["second"])?.toString()?.trim().orEmpty()
                    if (name.isNotEmpty() || amount.isNotEmpty()) Ingredient(name, amount) else null
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    // "2 컵" 같은 문자열 → (수량, 단위)
    private fun splitAmount(amount: String): Pair<String, String> {
        val trimmed = amount.trim()
        val idx = trimmed.indexOf(' ')
        return if (idx > 0) {
            trimmed.substring(0, idx) to trimmed.substring(idx + 1)
        } else {
            val m = Regex("^([0-9]+)\\s*([^0-9].*)$").find(trimmed)
            if (m != null) m.groupValues[1] to m.groupValues[2] else trimmed to ""
        }
    }

    private fun parseAlternatives(json: String?): List<Ingredient> = parseIngredients(json)
    private fun parseHandling(json: String?): List<Ingredient> = parseIngredients(json)

    private fun showLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar)?.visibility = if (show) View.VISIBLE else View.GONE
    }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private inline fun <reified T> parseListOrEmpty(json: String?): List<T> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            Gson().fromJson<List<T>>(json, object : com.google.gson.reflect.TypeToken<List<T>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun onClickPublish() {
        // 1) 화면에서 값 수집 (이미 만들어둔 함수)
        val dto = collectAllFields(isDraft = true)  // 먼저 draft 형태로 만든다

        showLoading(true)

        if (draftId == null) {
            // 2) 초안이 없으면 먼저 초안 생성 -> 생성된 id로 바로 발행
            val token = "Bearer ${App.prefs.token}"
            RetrofitInstance.apiService.createDraft(token, dto).enqueue(object : retrofit2.Callback<RecipeCreateResponse> {
                override fun onResponse(c: retrofit2.Call<RecipeCreateResponse>, r: retrofit2.Response<RecipeCreateResponse>) {
                    if (r.isSuccessful) {
                        // 생성된 draftId로 바로 발행
                        draftId = r.body()?.recipeId
                        publishDraftNow(draftId!!)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@RecipeWriteBothActivity, "임시저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(c: retrofit2.Call<RecipeCreateResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@RecipeWriteBothActivity, t.message ?: "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // 3) 초안이 이미 있으면, 최신 내용으로 초안 업데이트 후 발행
            val token = "Bearer ${App.prefs.token}"
            RetrofitInstance.apiService.updateDraft(token, draftId!!, dto).enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                override fun onResponse(c: retrofit2.Call<RecipeDraftDto>, r: retrofit2.Response<RecipeDraftDto>) {
                    if (r.isSuccessful) {
                        publishDraftNow(draftId!!)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@RecipeWriteBothActivity, "임시저장 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(c: retrofit2.Call<RecipeDraftDto>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@RecipeWriteBothActivity, t.message ?: "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 실제 발행 호출
    private fun publishDraftNow(id: Long) {
        val token = "Bearer ${App.prefs.token}"
        val body = PublishRequest(isPublic = isPublic)
        RetrofitInstance.apiService.publishDraft(token, id, body).enqueue(object : retrofit2.Callback<RecipeDraftDto> {
            override fun onResponse(c: retrofit2.Call<RecipeDraftDto>, r: retrofit2.Response<RecipeDraftDto>) {
                showLoading(false)
                if (r.isSuccessful) {
                    Toast.makeText(this@RecipeWriteBothActivity, "발행 완료", Toast.LENGTH_SHORT).show()
                    // 필요 시 상세 페이지 이동
                    val recipeId = r.body()?.recipeId
                    if (recipeId != null) {
                        startActivity(Intent(this@RecipeWriteBothActivity, RecipeSeeMainActivity::class.java).apply {
                            putExtra("recipeId", recipeId)
                        })
                        finish()
                    } else {
                        finish()
                    }
                } else {
                    Toast.makeText(this@RecipeWriteBothActivity, "발행 실패", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(c: retrofit2.Call<RecipeDraftDto>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@RecipeWriteBothActivity, t.message ?: "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == com.example.test.RecipeWriteImageActivity.EDIT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val editedUriStr = data?.getStringExtra("editedImageUri")
            val editedUri = editedUriStr?.let { Uri.parse(it) }

            editedUri?.let {
                // 어디에 넣을지 판단
                if (isPickingRepresentImage) {
                    displaySelectedImage(it, representImageContainer)
                    uploadImageToServer(it) { imageUrl ->
                        if (imageUrl != null) {
                            mainImageUrl = imageUrl
                        }
                    }
                    isPickingRepresentImage = false
                } else if (selectedContainer != null) {
                    displaySelectedImage(it, selectedContainer!!)
                    uploadImageToServer(it) { imageUrl ->
                        if (imageUrl != null) {
                            stepImages[currentStep] = imageUrl
                        }
                    }
                    selectedContainer = null
                } else {
                    // fallback
                    displaySelectedImage(it, imageContainer)
                    uploadImageToServer(it) { imageUrl ->
                        if (imageUrl != null) {
                            stepImages[currentStep] = imageUrl
                        }
                    }
                }
            }
        }
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
        val timeView = findViewById<EditText?>(R.id.halfHour)
        val tagView = findViewById<EditText?>(R.id.detailSettleRecipeTitleWrite)
        if (levelView != null && timeView != null && tagView != null) {
            val hasLevel = levelView.text.isNotBlank() && levelView.text !in listOf("난이도", "선택")
            val hasTime = timeView.text.isNotBlank()
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
                val materialView = currentLayout.findViewById<EditText?>(R.id.material)
                val measuringView = currentLayout.findViewById<EditText?>(R.id.measuring)
                val unitView = currentLayout.findViewById<TextView?>(R.id.unit)

                if (materialView != null && measuringView != null && unitView != null) {
                    val material = materialView.text.toString()
                    val measuring = measuringView.text.toString()
                    val unit = unitView.text.toString()
                    isValid = material.isNotBlank() && measuring.isNotBlank() && unit != "단위"
                }
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

            R.id.recipeWriteCookVideoLayout -> {
                val container = currentLayout.findViewById<LinearLayout?>(R.id.VideoContainer)
                isValid = container?.childCount ?: 0 > 0
            }

            R.id.recipeWriteDetailSettleLayout -> {
                val levelView = currentLayout.findViewById<TextView?>(R.id.elementaryLevel)
                val timeView = currentLayout.findViewById<EditText?>(R.id.zero)
                val tagView = currentLayout.findViewById<EditText?>(R.id.detailSettleRecipeTitleWrite)

                if (levelView != null && timeView != null && tagView != null) {
                    val level = levelView.text.toString()
                    val time = timeView.text.toString()
                    val tag = tagView.text.toString()
                    isValid = level.isNotBlank() && level != "난이도" && time.isNotBlank() && tag.isNotBlank()
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

    //동영상 표시
    private fun showVideoInfo(uri: Uri) {
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "편집된 동영상"

        val container = findViewById<LinearLayout>(R.id.VideoContainer)
        container.removeAllViews()

        val textView = TextView(this).apply {
            text = "선택한 동영상: $fileName"
            textSize = 16f
            setTextColor(Color.BLACK)
        }
        container.addView(textView)
    }

    //동영상 업로드
    private fun uploadVideoToServer(uri: Uri) {
        Log.d("Upload", "영상 업로드 시작")

        val inputStream = contentResolver.openInputStream(uri) ?: return
        val file = File(cacheDir, "upload_video.mp4")
        file.outputStream().use { inputStream.copyTo(it) }

        val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("video", file.name, requestFile)

        val token = App.prefs.token ?: ""
        Log.d("JWT", "보내는 토큰: Bearer $token")

        RetrofitInstance.apiService.uploadVideo(body, "Bearer $token")
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        val videoUrl = responseBody.string()
                        Log.d("Upload", "영상 업로드 성공: $videoUrl")
                        recipeVideoUrl = videoUrl
                        Log.d("Upload", "recipeVideoUrl 저장됨: $recipeVideoUrl")

                    } else {
                        Log.e("Upload", "업로드 실패 - 응답 없음 또는 실패 응답: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "업로드 실패: ${t.message}")
                }
            })
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
            layoutHistory.push(currentLayout)
            showOnlyLayout(newLayout)

            // ✅ 항상 버튼 상태 다시 계산
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

    // 레시피 재료 내용 추가하기 클릭시 내용 추가
    private fun addNewItem(): View = addNewItem(null)

    private fun addNewItem(sourceRect: View?): View {
        // 새로운 ConstraintLayout 생성
        val newItemLayout = ConstraintLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.tag = sourceRect
        }

        // 재료명 EditText 생성
        val material = EditText(this).apply {
            id = View.generateViewId()  // ID 생성하여 ConstraintLayout에서 사용할 수 있도록 함
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(18), dpToPx(5), 0, 0) // 위쪽 여백 설정
            }
            hint = "재료명"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 계량 EditText 생성
        val measuring = EditText(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // 계량 EditText는 내용 크기만큼 표시
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                startToStart = material.id  // 재료명 EditText 왼쪽 끝에 맞추기
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 맞추기
                topToTop = material.id  // 재료명 EditText 위쪽 끝에 맞추기

                setMargins(dpToPx(204), dpToPx(1), 0, 0) // 적절한 여백 설정
            }
            hint = "계량"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 삭제 버튼 생성
        val delete = ImageButton(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 오른쪽 끝에 위치하도록 설정
                endToEnd = material.id  // materialEditText의 오른쪽 끝에 맞추기
                topToTop = material.id  // materialEditText의 위쪽 끝에 맞추기

                // 오른쪽 마진을 5dp로 설정하여 왼쪽으로 이동
                setMargins(0, 0, dpToPx(14), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
            }
            setImageResource(R.drawable.ic_delete) // 삭제 아이콘 설정
            setBackgroundResource(android.R.color.transparent) // 배경 투명
        }

        // 삭제 버튼 클릭 시 해당 레이아웃 삭제 & 버튼 위치 조정
        delete.setOnClickListener {
            materialContainer.removeView(newItemLayout)
            itemCount--

            // ✅ sourceRect가 null이어도 tag에서 복구
            val rectToClear = (sourceRect ?: newItemLayout.tag) as? View

            rectToClear?.let { rect ->
                rectToRow.remove(rect)
                if (selectedRects.remove(rect)) {
                    rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                }
            }
        }

        // 새로운 바 생성
        val divideRectangleBarFive = View(this).apply {
            id = View.generateViewId()  // ID 생성

            // LayoutParams 설정
            val layoutParams = ConstraintLayout.LayoutParams(
                0,  // width를 0으로 설정하여 부모의 width를 채우도록 함
                dpToPx(1)  // 2dp 높이를 px로 변환하여 설정
            ).apply {
                // 바를 materialEditText 아래로 배치
                topToBottom = material.id  // materialEditText 아래에 위치
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID  // 왼쪽 끝에 붙임
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 붙임

                // 좌우 마진 5dp 설정
                setMargins(dpToPx(3), dpToPx(0), dpToPx(1), 0)
            }

            this.layoutParams = layoutParams
            setBackgroundResource(R.drawable.bar_recipe_see_material)  // 배경 설정
        }

        // LinearLayout에 요소 추가
        newItemLayout.apply {
            addView(material)
            addView(measuring)
            addView(delete)
            addView(divideRectangleBarFive)
        }

        // 부모 레이아웃에 추가
        materialContainer.addView(newItemLayout)
        itemCount++
        return newItemLayout
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
        val divideRectangleBarSix = View(this).apply {
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
            addView(divideRectangleBarSix)
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
                setMargins(0, 0, dpToPx(13), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
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
                setMargins(dpToPx(3), 0, dpToPx(3), 0)
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
        addButton.requestLayout()

        val timerParams = timerButton.layoutParams as ViewGroup.MarginLayoutParams
        timerButton.requestLayout()

    }

    // 조리순서 step 추가 후 내용 추가하기
    val stepRecipeCountMap = mutableMapOf<Int, Int>()

    private fun addNewStep(step: Int) {
        for (i in 0 until stepContainer.childCount) {
            stepContainer.getChildAt(i).visibility = View.GONE
        }

        val newStepLayout = LayoutInflater.from(this).inflate(R.layout.item_step, stepContainer, false)
        // 기존에 XML에 있던 cookOrderRecipeWrite도 매번 태그 업데이트
        val cookOrderRecipeWrite = newStepLayout.findViewById<EditText>(R.id.cookOrderRecipeWrite)
        cookOrderRecipeWrite.tag = "$step-1"
        // Step 번호 설정
        val stepTextView = newStepLayout.findViewById<TextView>(R.id.stepOne)
        stepTextView.text = "STEP $step"

        // SubStep 번호 초기화
        val stepLittleTextView = newStepLayout.findViewById<TextView>(R.id.stepLittleOne)
        stepLittleTextView.text = "$step-1"

        // 카메라 버튼 찾기
        val stepCamera = newStepLayout.findViewById<ImageButton>(R.id.stepCamera)

        // 내용추가 버튼 선언
        val contentAddTwo = newStepLayout.findViewById<AppCompatButton>(R.id.contentAddTwo)

        val timerAddTwo = newStepLayout.findViewById<AppCompatButton>(R.id.timerAddTwo)

        timerAddTwo.setOnClickListener {
            val dynamicRecipeInputContainer = newStepLayout.findViewById<LinearLayout>(R.id.timerInput)

            // 🔁 기존 타이머가 있다면 제거 (중복 방지)
            for (i in 0 until dynamicRecipeInputContainer.childCount) {
                val child = dynamicRecipeInputContainer.getChildAt(i)
                if (child.tag == "timer_$step") {
                    dynamicRecipeInputContainer.removeView(child)
                    break
                }
            }

            // 🔧 새 타이머 뷰 생성
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
                timerLayout.visibility = View.GONE
            }

            // 타이머 뷰 추가
            dynamicRecipeInputContainer.addView(timerLayout)

            // 레이아웃 마진 조정
            timerLayout.post {
                val layoutParamsContent = contentAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                contentAddTwo.layoutParams = layoutParamsContent

                val layoutParamsTimer = timerAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                timerAddTwo.layoutParams = layoutParamsTimer
            }
        }


        // 버튼이 보이도록 설정
        stepCamera.visibility = View.VISIBLE
        stepCamera.isClickable = true
        val cookOrderRecipeContainerAdd = newStepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)

        // 카메라 버튼 클릭 시 갤러리 열기
        stepCamera.setOnClickListener {
            selectedContainer = cookOrderRecipeContainerAdd
            pickImageLauncherForStepCamera.launch("image/*")
        }

        // 내용추가 버튼 클릭 시 내용추가
        contentAddTwo.setOnClickListener {
            // 현재 STEP에 해당하는 recipeStepCount 가져오기
            val currentRecipeStepCount = stepRecipeCountMap[step] ?: 2

            val stepLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(45, 38, 45, 0)
                }
            }

            val stepPrefix = TextView(this).apply {
                text = "$step-$currentRecipeStepCount"
                textSize = 13f
                setTextColor(Color.parseColor("#2B2B2B"))
                setPadding(0, 0, 12, 0)
            }

            val editText = EditText(this).apply {
                id = View.generateViewId()
                tag = "$step-$currentRecipeStepCount"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                hint = "레시피를 입력해주세요."
                textSize = 13f
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#A1A9AD"))
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    setMargins(45, 12, 45, 0)
                }
                setBackgroundColor(Color.parseColor("#D9D9D9"))
            }

            // STEP 순서 번호 증가
            stepRecipeCountMap[step] = currentRecipeStepCount + 1
            // 현재 STEP의 recipeStepCount 증가

            stepLayout.addView(stepPrefix)
            stepLayout.addView(editText)

            // 동적으로 추가된 EditText와 Divider를 cookOrderRecipeContainerAdd에 추가
            val container = newStepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)
            container.addView(stepLayout)
            container.addView(divider)

            // dp 값으로 변환하는 함수
            fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

            // 🚀 구분선의 아래 위치를 구한 후 버튼 위치 조정
            divider.post {
                val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
                val dividerBottom = divider.top + dividerParams.height // 구분선의 끝 위치

                // 🚀 버튼 위치 조정 (구분선 아래 70dp 위치)
                val buttonParams = contentAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                contentAddTwo.requestLayout()

                val timerParams = timerAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                timerAddTwo.requestLayout()
            }
        }

        // 새로운 Step을 stepContainer에 추가
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
        params.topMargin = requiredTimeAndTag.dpToPx(37)
        requiredTimeAndTag.layoutParams = params
    }

    // 레시피 세부설정 드롭다운 닫기 및 recipeName 위치 복원
    private fun detailSettleCloseDropDown(levelBoxChoice: ConstraintLayout, requiredTimeAndTag: ConstraintLayout) {
        levelBoxChoice.visibility = View.GONE

        // requiredTimeAndTag 위치 복원
        val params = requiredTimeAndTag.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = requiredTimeAndTag.dpToPx(20)
        requiredTimeAndTag.layoutParams = params
    }
    //이미지선택
    private fun displaySelectedImage(uri: Uri, targetContainer: LinearLayout) {
        val imageView = ImageView(this).apply {
            setImageURI(uri)
            layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx()).apply {
                setMargins(0, 16.dpToPx(), 0, 16.dpToPx())
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
        }
        targetContainer.addView(imageView)
        Log.d("RecipeWriteImageActivity", "이미지 추가 완료! 대상 컨테이너: ${targetContainer.id}")
    }


    //백엔드 서버에 이미지 업로드
    fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        val file = uriToFile(this, uri) ?: return
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = App.prefs.token ?: ""
        if (token.isEmpty()) {
            Log.e("Upload", "토큰이 없음!")
            callback(null) // 실패 시 null 반환
            return
        }

        Log.d("Upload", "이미지 업로드 시작 - 파일명: ${file.name}, 크기: ${file.length()} 바이트")

        RetrofitInstance.apiService.uploadImage("Bearer $token", body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.string()
                        Log.d("Upload", "이미지 업로드 성공! URL: $imageUrl")
                        callback(imageUrl) // ✅ 성공 시 URL 반환
                    } else {
                        Log.e("Upload", "이미지 업로드 실패: 응답 코드 ${response.code()}, 오류 메시지: ${response.errorBody()?.string()}")
                        callback(null) // 실패 시 null 반환
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "네트워크 요청 실패: ${t.message}")
                    callback(null) // 실패 시 null 반환
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

    private fun extractNumber(amount: String): Double? {
        val m = Regex("[-+]?\\d*\\.?\\d+").find(amount)
        return m?.value?.toDoubleOrNull()
    }

    private fun collectIngredientsForDraft(): List<RecipeIngredientReq>? {
        val simple = collectIngredientsList() ?: return null // Ingredient(name, amount="200 g")
        return simple.map { ing ->
            val matched = allIngredients.firstOrNull { it.nameKo == ing.name }
            val (qtyStr, unit) = splitAmount(ing.amount)     // ("200","g")
            RecipeIngredientReq(
                id = matched?.id,
                quantity = qtyStr.toDoubleOrNull()
            )
        }
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
                Toast.makeText(this, "레시피 업로드 실패", Toast.LENGTH_SHORT).show()
                onFailure?.invoke()
            }
        }
    }
    //썸네일 생성
    private fun showProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }
    private fun showVideoPreview(uri: Uri) {
        val playerView = findViewById<PlayerView>(R.id.videoPlayerView)
        playerView.visibility = View.VISIBLE

        exoPlayer?.release() // 이전 player 해제
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = false // 자동 재생 X
    }

}
