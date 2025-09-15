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
import com.example.test.model.CookingStep
import com.example.test.model.Ingredient
import com.example.test.model.RecipeRequest
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
private var recipe: RecipeRequest? = null  // 있으면 생략
@OptIn(UnstableApi::class)
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

    companion object {
        const val EDIT_IMAGE_REQUEST_CODE = 1001
    }

    private var filteredIngredients: List<Pair<String, String>> = emptyList()
    private var replaceIngredients: List<String> = emptyList()
    private var handlingMethods: List<String> = emptyList()
    private var cookingSteps: MutableList<String> = mutableListOf()

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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_image)
        TabBarUtils.setupTabBar(this)

        // 배열 초기화
        committedCompleted = BooleanArray(tabCompleted.size) { false }
        lastSelectedIndex = selectedIndex

        // 재료
        materialContainer = findViewById(R.id.materialContainer)
        addFixButton = findViewById(R.id.addFixButton)

        addFixButton.setOnClickListener {
            if (itemCount < maxItems) {
                addNewItem()
                moveButtonDown() // 버튼 아래로 이동
            }
        }

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
        val material = findViewById<EditText>(R.id.material)
        val measuring = findViewById<EditText>(R.id.measuring)
        val materialTwo = findViewById<EditText>(R.id.materialTwo)
        val measuringTwo = findViewById<EditText>(R.id.measuringTwo)
        val materialThree = findViewById<EditText>(R.id.materialThree)
        val measuringThree = findViewById<EditText>(R.id.measuringThree)
        val materialFour = findViewById<EditText>(R.id.materialFour)
        val measuringFour = findViewById<EditText>(R.id.measuringFour)
        val materialFive = findViewById<EditText>(R.id.materialFive)
        val measuringFive = findViewById<EditText>(R.id.measuringFive)
        val materialSix = findViewById<EditText>(R.id.materialSix)
        val measuringSix = findViewById<EditText>(R.id.measuringSix)
        val delete = findViewById<ImageButton>(R.id.delete)
        val deleteTwo = findViewById<ImageButton>(R.id.deleteTwo)
        val deleteThree = findViewById<ImageButton>(R.id.deleteThree)
        val deleteFour = findViewById<ImageButton>(R.id.deleteFour)
        val deleteFive = findViewById<ImageButton>(R.id.deleteFive)
        val deleteSix = findViewById<ImageButton>(R.id.deleteSix)
        val addFixButton = findViewById<AppCompatButton>(R.id.addFixButton)
        val divideRectangleBarFive = findViewById<View>(R.id.divideRectangleBarFive)
        val divideRectangleBarSix = findViewById<View>(R.id.divideRectangleBarSix)
        val divideRectangleBarSeven = findViewById<View>(R.id.divideRectangleBarSeven)
        val divideRectangleBarEight = findViewById<View>(R.id.divideRectangleBarEight)
        val divideRectangleBarNine = findViewById<View>(R.id.divideRectangleBarNine)
        val divideRectangleBarTen = findViewById<View>(R.id.divideRectangleBarTen)
        val unit = findViewById<TextView>(R.id.unit)
        val unitTwo = findViewById<TextView>(R.id.unitTwo)
        val unitThree = findViewById<TextView>(R.id.unitThree)
        val unitFour = findViewById<TextView>(R.id.unitFour)
        val unitFive = findViewById<TextView>(R.id.unitFive)
        val unitSix = findViewById<TextView>(R.id.unitSix)
        val dropDownTwo = findViewById<ImageButton>(R.id.dropDownTwo)
        val dropDownThree = findViewById<ImageButton>(R.id.dropDownThree)
        val dropDownFour = findViewById<ImageButton>(R.id.dropDownFour)
        val dropDownFive = findViewById<ImageButton>(R.id.dropDownFive)
        val dropDownSix = findViewById<ImageButton>(R.id.dropDownSix)
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

        fun mapCategoryToEnum(category: String): String {
            return when (category) {
                "한식" -> "koreaFood"
                "양식" -> "westernFood"
                "일식" -> "japaneseFood"
                "중식" -> "chineseFood"
                "채식" -> "vegetarianDiet"
                "간식" -> "snack"
                "안주" -> "alcoholSnack"
                "반찬" -> "sideDish"
                "기타" -> "etc"
                else -> "etc" // 예외 처리
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
            if (currentLayout.id == R.id.recipeWriteDetailSettleLayout) {//대표이미지 가져오기
                findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout).visibility = View.GONE
                findViewById<View>(R.id.cookOrderTapBar).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE

                val representativeImage = findViewById<ImageView>(R.id.representativeImage)
                val fullImageUrl = RetrofitInstance.BASE_URL + mainImageUrl.trim()
                Glide.with(this).load(fullImageUrl).into(representativeImage)
                // 선택된 카테고리 가져오기
                val categoryText = koreanFood.text.toString() // 사용자가 선택한 값 가져오기
                // 레시피 제목 가져오기
                val recipeTitle = recipeTitleWrite.text.toString()
                // 기존 재료 입력란 + 동적으로 추가된 재료 입력란 가져오기
                val ingredients = mutableListOf<Pair<String, String>>()

                // 정적 재료 입력란 추가
                ingredients.add(material.text.toString() to "${measuring.text} ${unit.text}")
                ingredients.add(materialTwo.text.toString() to "${measuringTwo.text} ${unitTwo.text}")
                ingredients.add(materialThree.text.toString() to "${measuringThree.text} ${unitThree.text}")
                ingredients.add(materialFour.text.toString() to "${measuringFour.text} ${unitFour.text}")
                ingredients.add(materialFive.text.toString() to "${measuringFive.text} ${unitFive.text}")
                ingredients.add(materialSix.text.toString() to "${measuringSix.text} ${unitSix.text}")


                // 동적으로 추가된 재료 입력란에서 데이터 수집
                for (i in 0 until materialContainer.childCount) {
                    val itemLayout = materialContainer.getChildAt(i) as? ConstraintLayout ?: continue
                    val materialEditText = itemLayout.getChildAt(0) as? EditText
                    val measuringEditText = itemLayout.getChildAt(1) as? EditText
                    val unitTextView = itemLayout.getChildAt(2) as? TextView

                    if (materialEditText != null && measuringEditText != null && unitTextView != null) {
                        val materialName = materialEditText.text.toString()
                        val amountWithUnit = "${measuringEditText.text} ${unitTextView.text}" //단위 추가

                        if (materialName.isNotBlank() && amountWithUnit.isNotBlank()) {
                            ingredients.add(materialName to amountWithUnit)
                        }
                    }
                }

                // 빈 값 제거
                val filteredIngredients =
                    ingredients.filter { it.first.isNotBlank() && it.second.isNotBlank() }

                // UI 업데이트 (RecyclerView 대신 기존 LinearLayout에 추가)
                updateMaterialList(materialContainer, filteredIngredients)

                // 대체 재료 가져오기
                val replaceIngredients = listOf(
                    "${replaceMaterialName.text.toString().trim()} → ${replaceMaterial.text.toString().trim()}",
                    "${replaceMaterialMaterialTwo.text.toString().trim()} → ${replaceMaterialTwo.text.toString().trim()}"
                ).filter { it.isNotBlank() }

                // 처리 방법 가져오기
                val handlingMethods = listOf(
                    "${handlingMethodName.text.toString().trim()} : ${handlingMethod.text.toString().trim()}",
                    "${handlingMethodMaterialTwo.text.toString().trim()} : ${handlingMethodTwo.text.toString().trim()}"
                ).filter { it.isNotBlank() }

                // 조리 순서 가져오기
                val cookingSteps = saveRecipeSteps()

                // 타이머 값 가져오기
                val cookingHour = zero.text.toString().takeIf { it.isNotBlank() }?.toInt() ?: 0
                val cookingMinute = halfHour.text.toString().takeIf { it.isNotBlank() }?.toInt() ?: 0

                //태그 값 가져오기
                val recipeTag = detailSettleRecipeTitleWrite.text.toString()

                // 화면에 표시할 TextView 찾기 (출력할 레이아웃이 있어야 함)
                findViewById<TextView>(R.id.contentCheckFoodName).text = recipeTitle
                findViewById<TextView>(R.id.contentCheckKoreanFood).text = categoryText
                findViewById<TextView>(R.id.contentCheckBeginningLevel).text = elementaryLevel.text
                findViewById<TextView>(R.id.foodNameTwo).text = recipeTag
                findViewById<TextView>(R.id.contentCheckZero).text = cookingHour.toString()
                findViewById<TextView>(R.id.contentCheckHalfHour).text = cookingMinute.toString()

                // 기존 레이아웃 변경 (가시성 설정 유지)
                findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.contentCheckTapBar).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout).visibility = View.GONE
                findViewById<View>(R.id.cookOrderTapBar).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE

                // 소요시간 (조리시간)
                val totalCookingTime = (cookingHour.toInt() * 60) + cookingMinute.toInt()
                //난이도
                val difficulty = elementaryLevel.text.toString()
                // 카테고리 Enum 변환
                val categoryEnum = mapCategoryToEnum(categoryText)
                // Gson 인스턴스 생성
                val gson = Gson()

                // RecipeRequest 객체 생성
                recipe = RecipeRequest(
                    title = recipeTitle,
                    category = categoryEnum,
                    ingredients = gson.toJson(filteredIngredients.map {
                        Ingredient(
                            it.first,
                            it.second
                        )
                    }),
                    alternativeIngredients = gson.toJson(replaceIngredients.filter { it.contains(" → ") }
                        .map {
                            val parts = it.split(" → ")
                            Ingredient(parts[0], parts[1])
                        }),
                    handlingMethods = gson.toJson(handlingMethods),
                    cookingSteps = gson.toJson(cookingSteps.mapIndexed { index, stepText ->
                        val step = index + 1
                        val (hour, minute) = stepTimerMap[step] ?: (0 to 0)
                        val totalSeconds = hour * 3600 + minute * 60

                        val imageUrl = stepImages[step] ?: ""

                        CookingStep(
                            step = step,
                            description = stepText,
                            mediaUrl = imageUrl,
                            mediaType = "IMAGE",
                            timeInSeconds = totalSeconds
                        )
                    }), // Step별 이미지 URL 추가하여 리스트 그대로 전달
                    mainImageUrl = mainImageUrl,
                    difficulty = difficulty,
                    tags = recipeTag,
                    cookingTime = totalCookingTime,
                    servings = 2,
                    isPublic = true
                )
                Log.d("RecipeRequest", gson.toJson(recipe))
                updateMaterialListView(
                    findViewById(R.id.materialList),
                    filteredIngredients,
                    replaceIngredients.map { it.split(" → ")[0] to it.split(" → ")[1] },
                    handlingMethods.map { it.split(" : ")[0] to it.split(" : ")[1] }
                )
                val localRecipe = recipe
                if (localRecipe != null) {
                    val type = object : TypeToken<List<CookingStep>>() {}.type
                    val cookingStepList: List<CookingStep> = gson.fromJson(localRecipe.cookingSteps, type)
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
            // ✅ recipe가 null이면 새로 생성
            if (recipe == null) {
                val gson = Gson()
                recipe = RecipeRequest(
                    title = recipeTitleWrite.text.toString(),
                    category = mapCategoryToEnum(koreanFood.text.toString()),
                    ingredients = gson.toJson(filteredIngredients.map {
                        Ingredient(it.first, it.second)
                    }),
                    alternativeIngredients = gson.toJson(replaceIngredients.filter { it.contains(" → ") }
                        .map {
                            val parts = it.split(" → ")
                            Ingredient(parts[0], parts[1])
                        }),
                    handlingMethods = gson.toJson(handlingMethods),
                    cookingSteps = gson.toJson(cookingSteps.mapIndexed { index, stepText ->
                        val step = index + 1
                        val (hour, minute) = stepTimerMap[step] ?: (0 to 0)
                        val totalSeconds = hour * 3600 + minute * 60

                        val imageUrl = stepImages[step] ?: ""

                        CookingStep(
                            step = step,
                            description = stepText,
                            mediaUrl = imageUrl,
                            mediaType = "IMAGE",   // ✅ 이미지 작성
                            timeInSeconds = totalSeconds
                        )
                    }),
                    mainImageUrl = mainImageUrl,
                    difficulty = elementaryLevel.text.toString(),
                    tags = detailSettleRecipeTitleWrite.text.toString(),
                    cookingTime = (zero.text.toString().toIntOrNull() ?: 0) * 60 + (halfHour.text.toString().toIntOrNull() ?: 0),
                    servings = 2,
                    isPublic = false,
                    videoUrl = "",                // ✅ 영상 없음
                    isDraft = true,               // ✅ 임시저장
                    recipeType = "IMAGE"          // ✅ 이미지 작성
                )
            } else {
                // 이미 생성된 recipe가 있으면 copy해서 draft로 저장
                recipe = recipe!!.copy(
                    isDraft = true,
                    isPublic = false,
                    recipeType = "IMAGE"
                )
            }

            val gson = Gson()
            Log.d("RecipeRequest", "임시저장 전송 JSON = ${gson.toJson(recipe)}")

            sendRecipeToServer(recipe!!, onSuccess = { recipeId ->
                Toast.makeText(this, "임시저장 성공!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
                finish()
            }, onFailure = {
                Toast.makeText(this, "임시저장 실패", Toast.LENGTH_SHORT).show()
            })
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

        /// 드롭다운 항목
        val unitOptions = listOf("개", "컵", "큰솔", "작은솔", "티스푼", "리터", "g", "kg", "덩어리", "묶음", "주머니", "봉지", "통", "팩", "한 줌", "한 술")

        // 버튼 ID → 텍스트뷰 ID 맵핑
        val buttonToUnitMap = mapOf(
            R.id.dropDown to R.id.unit,
            R.id.dropDownTwo to R.id.unitTwo,
            R.id.dropDownThree to R.id.unitThree,
            R.id.dropDownFour to R.id.unitFour,
            R.id.dropDownFive to R.id.unitFive,
            R.id.dropDownSix to R.id.unitSix
        )

        // 각 버튼에 대해 PopupMenu 설정
        buttonToUnitMap.forEach { (buttonId, unitId) ->
            val button = findViewById<ImageButton>(buttonId)
            val unit = findViewById<TextView>(unitId)

            button.setOnClickListener {
                val popup = PopupMenu(this@RecipeWriteImageActivity, button)

                unitOptions.forEach { option ->
                    popup.menu.add(option)
                }

                popup.setOnMenuItemClickListener { menuItem ->
                    unit.text = menuItem.title
                    unit.setTextColor(Color.parseColor("#2B2B2B"))
                    checkTabs()
                    checkAndUpdateContinueButton() // ✅ 선택 후 버튼 상태 갱신
                    true
                }

                popup.show()
            }
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

        //재료 채워지면 계속하기 버튼 바뀜
        material.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        measuring.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkAndUpdateContinueButton()
                checkTabs()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        unit.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> checkAndUpdateContinueButton() }

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

        // 레시피 재료 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        deleteTwo.setOnClickListener {
            materialTwo.visibility = View.GONE
            measuringTwo.visibility = View.GONE
            deleteTwo.visibility = ImageButton.GONE
            divideRectangleBarSix.visibility = View.GONE
            unitTwo.visibility = View.GONE
            dropDownTwo.visibility = ImageButton.GONE
        }

        // 레시피 재료 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        deleteThree.setOnClickListener {
            materialThree.visibility = View.GONE
            measuringThree.visibility = View.GONE
            deleteThree.visibility = ImageButton.GONE
            divideRectangleBarSeven.visibility = View.GONE
            unitThree.visibility = View.GONE
            dropDownThree.visibility = ImageButton.GONE
        }

        // 레시피 재료 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        deleteFour.setOnClickListener {
            materialFour.visibility = View.GONE
            measuringFour.visibility = View.GONE
            deleteFour.visibility = ImageButton.GONE
            divideRectangleBarEight.visibility = View.GONE
            unitFour.visibility = View.GONE
            dropDownFour.visibility = ImageButton.GONE
        }

        // 레시피 재료 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        deleteFive.setOnClickListener {
            materialFive.visibility = View.GONE
            measuringFive.visibility = View.GONE
            deleteFive.visibility = ImageButton.GONE
            divideRectangleBarNine.visibility = View.GONE
            unitFive.visibility = View.GONE
            dropDownFive.visibility = ImageButton.GONE
        }

        // 레시피 재료 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
        deleteSix.setOnClickListener {
            materialSix.visibility = View.GONE
            measuringSix.visibility = View.GONE
            deleteSix.visibility = ImageButton.GONE
            divideRectangleBarTen.visibility = View.GONE
            unitSix.visibility = View.GONE
            dropDownSix.visibility = ImageButton.GONE
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
            if (recipe != null) {
                val uploadRecipe = recipe!!.copy(isPublic = isPublic)

                sendRecipeToServer(uploadRecipe, onSuccess = { recipeId ->
                    val intent = Intent(this, RecipeSeeMainActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    startActivity(intent)
                    finish()
                }, onFailure = {
                    Toast.makeText(this, "레시피 업로드 실패", Toast.LENGTH_SHORT).show()
                })

            } else {
                Toast.makeText(this, "레시피를 먼저 작성해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener {
            if (recipe != null) {
                val uploadRecipe = recipe!!.copy(isPublic = isPublic)

                sendRecipeToServer(uploadRecipe, onSuccess = { recipeId ->
                    val intent = Intent(this, RecipeSeeMainActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    startActivity(intent)
                    finish()
                }, onFailure = {
                    Toast.makeText(this, "레시피 업로드 실패", Toast.LENGTH_SHORT).show()
                })

            } else {
                Toast.makeText(this, "레시피를 먼저 작성해주세요.", Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
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

        // ===== 2번 탭: 재료 =====
        val materialView = findViewById<EditText?>(R.id.material)
        val measuringView = findViewById<EditText?>(R.id.measuring)
        val unitView = findViewById<TextView?>(R.id.unit)

        if (materialView != null && measuringView != null && unitView != null) {
            val hasMaterial = materialView.text.isNotBlank()
            val hasMeasuring = measuringView.text.isNotBlank()
            val hasUnit = unitView.text.isNotBlank() && unitView.text != "단위"
            tabCompleted[1] = hasMaterial && hasMeasuring && hasUnit
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
    private fun addNewItem() {
        // 새로운 ConstraintLayout 생성
        val newItemLayout = ConstraintLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 재료명 EditText 생성
        val materialSix = EditText(this).apply {
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
        val measuringSix = EditText(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // 계량 EditText는 내용 크기만큼 표시
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                startToStart = materialSix.id  // 재료명 EditText 왼쪽 끝에 맞추기
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 맞추기
                topToTop = materialSix.id  // 재료명 EditText 위쪽 끝에 맞추기

                setMargins(dpToPx(204), dpToPx(1), 0, 0) // 적절한 여백 설정
            }
            hint = "계량"
            textSize = 13f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 단위 TextView 생성
        val unitSix = TextView(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // 계량 EditText는 내용 크기만큼 표시
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                startToStart = measuringSix.id  // 재료명 EditText 왼쪽 끝에 맞추기
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 맞추기
                topToTop = measuringSix.id  // 재료명 EditText 위쪽 끝에 맞추기

                setMargins(dpToPx(236), dpToPx(12), 0, 0) // 적절한 여백 설정
            }
            hint = "단위"
            textSize = 12f
            setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
        }

        // 삭제 버튼 생성
        val deleteSix = ImageButton(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 오른쪽 끝에 위치하도록 설정
                endToEnd = materialSix.id  // materialEditText의 오른쪽 끝에 맞추기
                topToTop = materialSix.id  // materialEditText의 위쪽 끝에 맞추기

                // 오른쪽 마진을 5dp로 설정하여 왼쪽으로 이동
                setMargins(0, 0, dpToPx(14), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
            }
            setImageResource(R.drawable.ic_delete) // 삭제 아이콘 설정
            setBackgroundResource(android.R.color.transparent) // 배경 투명
        }

        // 드롭다운 버튼 생성
        val dropDownSix = ImageButton(this).apply {
            id = View.generateViewId()  // ID 생성
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 오른쪽 끝에 위치하도록 설정
                endToEnd = deleteSix.id  // materialEditText의 오른쪽 끝에 맞추기
                topToTop = deleteSix.id  // materialEditText의 위쪽 끝에 맞추기

                // 오른쪽 마진을 5dp로 설정하여 왼쪽으로 이동
                setMargins(0, 0, dpToPx(80), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
            }
            setImageResource(R.drawable.ic_drop_down) // 삭제 아이콘 설정
            setBackgroundResource(android.R.color.transparent) // 배경 투명
        }

        // 삭제 버튼 클릭 시 해당 레이아웃 삭제 & 버튼 위치 조정
        deleteSix.setOnClickListener {
            materialContainer.removeView(newItemLayout)
            itemCount--  // 아이템 수 감소
            moveButtonUp() // 아이템 삭제 시 버튼을 위로 이동
        }

        // 새로운 바 생성
        val divideRectangleBarEight = View(this).apply {
            id = View.generateViewId()  // ID 생성

            // LayoutParams 설정
            val layoutParams = ConstraintLayout.LayoutParams(
                0,  // width를 0으로 설정하여 부모의 width를 채우도록 함
                dpToPx(1)  // 2dp 높이를 px로 변환하여 설정
            ).apply {
                // 바를 materialEditText 아래로 배치
                topToBottom = materialSix.id  // materialEditText 아래에 위치
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
            addView(materialSix)
            addView(measuringSix)
            addView(unitSix)
            addView(deleteSix)
            addView(dropDownSix)
            addView(divideRectangleBarEight)
        }

        // 부모 레이아웃에 추가
        materialContainer.addView(newItemLayout)
        itemCount++
    }

    // 레시피 재료 내용 추가 버튼 클릭시 버튼 아래로 이동
    private fun moveButtonDown() {
        val params = addFixButton.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin += buttonMarginIncrease // 버튼을 130px 아래로 이동
        addFixButton.layoutParams = params
    }

    // 레시피 재료 내용 추가 버튼 위로 이동
    private fun moveButtonUp() {
        val params = addFixButton.layoutParams as ConstraintLayout.LayoutParams
        if (params.topMargin > 0) {
            params.topMargin -= buttonMarginIncrease // 버튼을 130px 위로 이동
            addFixButton.layoutParams = params
        }
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
        buttonParams.topMargin += 15 // 🔽 입력 칸과 70dp 간격 유지
        addButton.requestLayout()

        val timerParams = timerButton.layoutParams as ViewGroup.MarginLayoutParams
        timerParams.topMargin += 15 // 🔽 동일하게 70dp 유지
        timerButton.requestLayout()
    }

    // 조리순서 step 추가 후 내용 추가하기
    val stepRecipeCountMap = mutableMapOf<Int, Int>()
    val stepTimerMap = mutableMapOf<Int, Pair<Int, Int>>()

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
            val dynamicRecipeInputContainer = newStepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)

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
                tag = "timer_$step" // 태그로 중복 방지
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

            // 타이머 뷰 추가
            dynamicRecipeInputContainer.addView(timerLayout)

            timerLayout.post {
                val baseMarginDp = 32

                val layoutParamsContent = contentAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                layoutParamsContent.topMargin = baseMarginDp.dpToPx() + timerLayout.height + 15.dpToPx()
                contentAddTwo.layoutParams = layoutParamsContent

                val layoutParamsTimer = timerAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                layoutParamsTimer.topMargin = baseMarginDp.dpToPx() + timerLayout.height + 15.dpToPx()
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
                buttonParams.topMargin = dividerBottom + dpToPx(15) // 구분선 아래 70dp
                contentAddTwo.requestLayout()

                val timerParams = timerAddTwo.layoutParams as ViewGroup.MarginLayoutParams
                timerParams.topMargin = dividerBottom + dpToPx(15) // 동일하게 조정
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
        val imageView = ImageView(this)
        imageView.setImageURI(uri)
        val layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
        imageView.layoutParams = layoutParams
        targetContainer.addView(imageView) // 선택한 컨테이너에 이미지 추가
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

}
