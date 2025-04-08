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
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.example.test.Repository.RecipeRepository
import com.example.test.model.CookingStep
import com.example.test.model.Ingredient
import com.example.test.model.RecipeRequest
import com.google.gson.Gson

private lateinit var materialContainer: LinearLayout
private lateinit var replaceMaterialContainer: LinearLayout
private lateinit var handlingMethodContainer: LinearLayout
private lateinit var cookOrderRecipeContainer: LinearLayout
private lateinit var addFixButton: Button
private lateinit var replaceMaterialAddFixButton: Button
private lateinit var handlingMethodAddFixButton: Button
private lateinit var contentAdd: Button
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
private lateinit var camera: ImageButton
private lateinit var detailSettleCamera: ImageButton
private lateinit var imageContainer: LinearLayout
private lateinit var representImageContainer: LinearLayout
private var stepCount = 1 // 1-1부터 시작
private var currentStep = 1  // 현재 Step 번호 (ex. 1, 2, 3...)
private var recipeStepCount = 1 // 조리 순서 번호 관리 (1-1, 1-2, ...)
private val stepOrderMap = mutableMapOf<Int, Int>()  // 각 STEP의 조리순서 개수 저장

class RecipeWriteBothActivity : AppCompatActivity() {

    // 갤러리에서 선택한 이미지를 처리하는 콜백

    // 첫 번째 pickImageLauncher (camera 버튼용)
    private val pickImageLauncherForCamera =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // 이미지를 동적으로 추가
                val imageView = ImageView(this)
                imageView.setImageURI(it) // 이미지 URI 설정
                val layoutParams =
                    LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx()) // 이미지 크기 설정
                imageView.layoutParams = layoutParams
                imageContainer.addView(imageView) // LinearLayout에 이미지 추가
                representImageContainer.addView(imageView) // LinearLayout에 이미지 추가
            }
        }

    // 두 번째 pickImageLauncher (camera 버튼용)
    private val pickImageLauncherForVideoSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // 이미지를 동적으로 추가
                val imageView = ImageView(this)
                imageView.setImageURI(it) // 이미지 URI 설정
                val layoutParams =
                    LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx()) // 이미지 크기 설정
                imageView.layoutParams = layoutParams
                imageContainer.addView(imageView) // LinearLayout에 이미지 추가
                representImageContainer.addView(imageView) // LinearLayout에 이미지 추가
            }
        }

    // 세 번째 pickImageLauncher (세부설정 카메라 버튼용)
    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // 이미지를 동적으로 추가
                val imageView = ImageView(this)
                imageView.setImageURI(it) // 이미지 URI 설정
                val layoutParams =
                    LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx()) // 이미지 크기 설정
                imageView.layoutParams = layoutParams
                imageContainer.addView(imageView) // LinearLayout에 이미지 추가
                representImageContainer.addView(imageView) // LinearLayout에 이미지 추가
            }
        }

    private lateinit var stepContainer: LinearLayout // STEP을 추가할 컨테이너
    private lateinit var pickImageLauncherForStepCamera: ActivityResultLauncher<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both)

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
        val categoryDropDown = findViewById<ConstraintLayout>(R.id.categoryDropDown)
        val recipeName = findViewById<ConstraintLayout>(R.id.recipeName)
        val koreanFood = findViewById<TextView>(R.id.koreanFood)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val beforeButton = findViewById<Button>(R.id.beforeButton)

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
        val addFixButton = findViewById<Button>(R.id.addFixButton)
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
        val replaceMaterialAddFixButton = findViewById<Button>(R.id.replaceMaterialAddFixButton)

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
        val handlingMethodAddFixButton = findViewById<Button>(R.id.handlingMethodAddFixButton)

        // 레시피 조리순서 선언
        val recipeWriteCookOrderLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout)
        val cookOrderAddButton = findViewById<ConstraintLayout>(R.id.cookOrderAddButton)
        val cookOrderTimer = findViewById<ConstraintLayout>(R.id.cookOrderTimer)
        val cookOrderTapBar = findViewById<ConstraintLayout>(R.id.cookOrderTapBar)
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)
        val stepLittleOne = findViewById<TextView>(R.id.stepLittleOne)
        val cookOrderRecipeWrite = findViewById<EditText>(R.id.cookOrderRecipeWrite)
        val camera = findViewById<ImageButton>(R.id.camera)
        val timerAdd = findViewById<Button>(R.id.timerAdd)
        val endFixButton = findViewById<Button>(R.id.endFixButton)
        val stepAddFixButton = findViewById<Button>(R.id.stepAddFixButton)
        val timer = findViewById<TextView>(R.id.timer)
        val hour = findViewById<EditText>(R.id.hour)
        val time = findViewById<TextView>(R.id.time)
        val minute = findViewById<EditText>(R.id.minute)
        val start = findViewById<TextView>(R.id.start)
        val middle = findViewById<TextView>(R.id.middle)
        val timerDelete = findViewById<TextView>(R.id.timerDelete)
        val stepOne = findViewById<TextView>(R.id.stepOne)
        val five = findViewById<TextView>(R.id.five)
        hourEditText = findViewById(R.id.hour)
        minuteEditText = findViewById(R.id.minute)
        startTextView = findViewById(R.id.start)
        timeSeparator = findViewById(R.id.time)
        deleteTextView = findViewById(R.id.timerDelete)

        // 레시피 조리영상 선언
        val recipeWriteCookVideoLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout)
        val videoImageContainer = findViewById<LinearLayout>(R.id.videoImageContainer)
        val cookVideoCamera = findViewById<ImageButton>(R.id.cookVideoCamera)

        // 레시피 세부설정 선언
        val recipeWriteDetailSettleLayout =
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        val levelBoxChoice = findViewById<ConstraintLayout>(R.id.levelBoxChoice)
        val requiredTimeAndTag = findViewById<ConstraintLayout>(R.id.requiredTimeAndTag)
        val representImageContainer = findViewById<LinearLayout>(R.id.representImageContainer)
        val detailSettleCamera = findViewById<ImageButton>(R.id.detailSettleCamera)
        val elementaryLevel = findViewById<TextView>(R.id.elementaryLevel)
        val detailSettleDownArrow = findViewById<ImageButton>(R.id.detailSettleDownArrow)
        val zero = findViewById<EditText>(R.id.zero)
        val halfHour = findViewById<EditText>(R.id.halfHour)
        val detailSettleRecipeTitleWrite = findViewById<View>(R.id.detailSettleRecipeTitleWrite)

        // 레시피 작성한 내용 선언
        val contentCheckLayout = findViewById<ConstraintLayout>(R.id.contentCheckLayout)
        val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
        val contentCheckTapBar = findViewById<ConstraintLayout>(R.id.contentCheckTapBar)
        val tapBar = findViewById<ConstraintLayout>(R.id.tapBar)
        val shareFixButton = findViewById<Button>(R.id.shareFixButton)
        val registerFixButton = findViewById<Button>(R.id.registerFixButton)
        val uncheck = findViewById<ImageButton>(R.id.uncheck)
        val uncheckTwo = findViewById<ImageButton>(R.id.uncheckTwo)
        val cancel = findViewById<Button>(R.id.cancel)
        val settle = findViewById<Button>(R.id.settle)
        val cancelTwo = findViewById<Button>(R.id.cancelTwo)
        val register = findViewById<Button>(R.id.register)

        // 레시피 등록한 레시피 확인 선언
        val registerRecipeUpLayout = findViewById<ConstraintLayout>(R.id.registerRecipeUpLayout)
        val registerRecipeSeeLayout = findViewById<ConstraintLayout>(R.id.registerRecipeSeeLayout)

        // 카테고리 TextView 리스트
        val textViews = listOf(
            findViewById<TextView>(R.id.one),
            findViewById<TextView>(R.id.two),
            findViewById<TextView>(R.id.three),
            findViewById<TextView>(R.id.four),
            findViewById<TextView>(R.id.five),
            findViewById<TextView>(R.id.six),
            findViewById<TextView>(R.id.seven)
        )

        // ConstraintLayout 리스트 (TextView와 1:1 매칭)
        val layouts = listOf(
            findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteReplaceMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteHandlingMethodLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        )

        // 카테고리 TextView 클릭 시 해당 화면으로 이동 & 바 위치 변경
        textViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                // 모든 ConstraintLayout 숨김
                layouts.forEach { it.visibility = View.GONE }

                // 클릭된 TextView에 해당하는 ConstraintLayout만 표시
                layouts[index].visibility = View.VISIBLE

                // 모든 TextView 색상 초기화
                textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }

                // 클릭된 TextView만 색상 변경 (#2B2B2B)
                textView.setTextColor(Color.parseColor("#2B2B2B"))

                // 바(View)의 위치를 클릭한 TextView의 중앙으로 이동
                val targetX = textView.x + (textView.width / 2) - (indicatorBar.width / 2)
                indicatorBar.x = targetX
            }
        }

        // 현재 활성화된 화면 인덱스 추적 변수
        var currentIndex = 0

        // "계속하기" 버튼 클릭 시 화면 이동
        continueButton.setOnClickListener {
            if (currentIndex < layouts.size - 1) {
                // 현재 화면 숨기기
                layouts[currentIndex].visibility = View.GONE
                // 다음 화면 표시
                currentIndex++
                layouts[currentIndex].visibility = View.VISIBLE

                // 해당 TextView 색상 변경
                textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }
                textViews[currentIndex].setTextColor(Color.parseColor("#2B2B2B"))

                // 바(View)의 위치 변경
                val targetX =
                    textViews[currentIndex].x + (textViews[currentIndex].width / 2) - (indicatorBar.width / 2)
                indicatorBar.x = targetX
            } else {
                // 마지막 화면이면 contentCheckLayout 이동
                layouts[currentIndex].visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.contentCheckTapFix).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE
                findViewById<View>(R.id.tapBar).visibility = View.GONE
            }
        }

        // "이전으로" 버튼 클릭 시 화면 이동
        beforeButton.setOnClickListener {
            val recipeWriteTitleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout)

            // 현재 화면이 recipeWriteTitleLayout이면 RecipeWriteMain.kt로 이동
            if (recipeWriteTitleLayout.visibility == View.VISIBLE) {
                val intent = Intent(this, RecipeWriteMain::class.java)
                startActivity(intent)
                finish()  // 현재 액티비티 종료 (선택 사항)
            } else {
                // 현재 보이는 레이아웃 찾기
                val currentIndex = layouts.indexOfFirst { it.visibility == View.VISIBLE }

                // 현재 화면이 첫 번째가 아니라면 이전 화면으로 이동
                if (currentIndex > 0) {
                    layouts[currentIndex].visibility = View.GONE  // 현재 화면 숨기기
                    layouts[currentIndex - 1].visibility = View.VISIBLE  // 이전 화면 보이기

                    // TextView 색상 변경
                    textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }
                    textViews[currentIndex - 1].setTextColor(Color.parseColor("#2B2B2B"))

                    // 바(View)의 위치 변경
                    val targetX =
                        textViews[currentIndex - 1].x + (textViews[currentIndex - 1].width / 2) - (indicatorBar.width / 2)
                    indicatorBar.x = targetX
                }
            }
        }

        // 레시피 타이틀 드롭다운 버튼 클릭 시 열기/닫기 토글
        downArrow.setOnClickListener {
            if (categoryDropDown.visibility == View.VISIBLE) {
                closeDropDown(categoryDropDown, recipeName)
            } else {
                openDropDown(categoryDropDown, recipeName)
            }
        }

        // 레시피 타이틀 드롭다운 내부의 TextView(카테고리) 클릭 이벤트 설정
        for (i in 0 until categoryDropDown.childCount) {
            val child = categoryDropDown.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    koreanFood.text = child.text.toString() // 선택한 값으로 변경
                    koreanFood.setTextColor(Color.parseColor("#2B2B2B")) // 색깔 변경
                    closeDropDown(categoryDropDown, recipeName)
                }
            }
        }

        // 레시피 재료 materialDropDown을 findViewById로 제대로 연결
        val materialDropDown = findViewById<ConstraintLayout>(R.id.materialDropDown)

        // 레시피 재료 드롭다운 버튼과 연결할 unit을 관리하는 Map
        val buttonToUnitMap = mapOf(
            R.id.dropDown to R.id.unit,
            R.id.dropDownTwo to R.id.unitTwo,
            R.id.dropDownThree to R.id.unitThree,
            R.id.dropDownFour to R.id.unitFour,
            R.id.dropDownFive to R.id.unitFive,
            R.id.dropDownSix to R.id.unitSix
        )

        // 레시피 재료 드롭다운 버튼 클릭 시 동작 설정
        buttonToUnitMap.forEach { (buttonId, unitId) ->
            val button = findViewById<ImageButton>(buttonId)
            val unit = findViewById<TextView>(unitId)
            val materialDropDown = findViewById<ConstraintLayout>(R.id.materialDropDown)

            button.setOnClickListener {
                // 드롭다운 열기
                materialDropDown.visibility = View.VISIBLE

                // 드롭다운에서 텍스트를 선택할 때의 이벤트 처리
                for (i in 0 until materialDropDown.childCount) {
                    val child = materialDropDown.getChildAt(i)
                    if (child is TextView) {
                        child.setOnClickListener {
                            // 선택된 텍스트를 해당 unit에 설정
                            unit.text = child.text.toString()
                            unit.setTextColor(Color.parseColor("#2B2B2B")) // 색상 변경

                            // 드롭다운 닫기
                            materialDropDown.visibility = View.GONE
                        }
                    }
                }
            }
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

        // 레시피 조리순서 내용 추가하기 눌렀을때 내용 추가
        cookOrderRecipeContainer = findViewById(R.id.cookOrderRecipeContainer) // 레이아웃 ID
        contentAdd = findViewById(R.id.contentAdd)

        contentAdd.setOnClickListener {
            if (recipeStepCount < 10) { // 최대 1-9까지 허용 (원하는 개수 조절 가능)
                recipeStepCount++
                addRecipeStep(recipeStepCount)
            }
        }

        // 레시피 조리순서 step 추가
        stepContainer = findViewById(R.id.stepContainer) // onCreate에서 초기화

        stepAddFixButton.setOnClickListener {
            addNewStep()  // stepContainer 사용 가능
        }

        // 레시피 조리순서 step을 추가하는 함수
        fun addNewStep() {
            // 새로운 step을 추가하고 서브step 번호를 초기화
            currentStep++
            currentSubStep = 1  // 서브step은 1부터 시작
        }

        // 레시피 조리순서 카메라 버튼 클릭 시 갤러리 열기
        camera.setOnClickListener {
            pickImageLauncherForCamera.launch("image/*")
        }

        stepContainer = findViewById(R.id.stepContainer) // stepContainer 초기화

        // 레시피 조리순서 갤러리에서 이미지 선택하는 런처 초기화
        pickImageLauncherForStepCamera =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val imageView = ImageView(this)
                    imageView.setImageURI(it) // 선택한 이미지 설정
                    val layoutParams =
                        LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx()) // 크기 설정
                    imageView.layoutParams = layoutParams
                    stepContainer.addView(imageView) // 이미지 추가
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
            // 버튼들 사라지게 하기
            cookOrderAddButton.visibility = View.GONE

            // 타이머 관련 요소들 나타나게 하기
            linearLayout2.visibility = View.VISIBLE
            cookOrderStoreButton.visibility = View.VISIBLE
        }

        // NumberPicker 초기화
        val hourPicker = findViewById<NumberPicker>(R.id.numberPicker1)
        val minutePicker = findViewById<NumberPicker>(R.id.numberPicker2)
        val storeBtn = findViewById<Button>(R.id.storeBtn)

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
        endFixButton.setOnClickListener {
            findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.contentCheckTapBar).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.recipeWriteCookOrderLayout).visibility = View.GONE
            findViewById<View>(R.id.cookOrderTapBar).visibility = View.GONE
            findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
            findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
            findViewById<View>(R.id.divideRectangleBarTwentythree).visibility = View.GONE
        }

        // 레시피 조리순서 다른 레이아웃 목록을 먼저 선언
        val otherLayouts = listOf(
            findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteReplaceMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteHandlingMethodLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        )

        // 레시피 조리순서 초기 상태: cookOrderTapBar 숨김
        cookOrderTapBar.visibility = View.GONE

        // 레시피 조리순서 "five" 버튼 클릭 시 조리순서 화면을 표시하고, 다른 화면은 숨김
        findViewById<TextView>(R.id.five).setOnClickListener {
            // 다른 화면 숨기기
            otherLayouts.forEach { it.visibility = View.GONE }

            // 조리순서 화면만 보이게 설정
            recipeWriteCookOrderLayout.visibility = View.VISIBLE
            cookOrderTapBar.visibility = View.VISIBLE
        }

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

        // 레시피 조리영상 카메라 버튼 클릭 시 갤러리 열기
        cookVideoCamera.setOnClickListener {
            pickImageLauncherForVideoSettle.launch("image/*")
        }

        // 레시피 세부설정 카메라 버튼 클릭 시 갤러리 열기
        detailSettleCamera.setOnClickListener {
            pickImageLauncherForDetailSettle.launch("image/*")
        }

        // 레시피 세부설정 드롭다운 버튼 클릭 시 열기/닫기 토글
        detailSettleDownArrow.setOnClickListener {
            if (levelBoxChoice.visibility == View.VISIBLE) {
                detailSettleCloseDropDown(levelBoxChoice, requiredTimeAndTag)
            } else {
                detailSettleOpenDropDown(levelBoxChoice, requiredTimeAndTag)
            }
        }

        // 레시피 세부설정 드롭다운 내부의 TextView 클릭 이벤트 설정
        for (i in 0 until levelBoxChoice.childCount) {
            val child = levelBoxChoice.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    elementaryLevel.text = child.text.toString() // 선택한 값으로 변경
                    elementaryLevel.setTextColor(Color.parseColor("#2B2B2B")) // 색깔 변경
                    detailSettleCloseDropDown(levelBoxChoice, requiredTimeAndTag)
                }
            }
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
        }

        uncheckTwo.setOnClickListener {
            isCheckedTwo = !isCheckedTwo
            uncheckTwo.setImageResource(if (isCheckedTwo) R.drawable.ic_check else R.drawable.ic_uncheck)
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
            if (createdRecipeId != null) {
                val intent = Intent(this, RecipeSeeActivity::class.java)
                intent.putExtra("recipeId", createdRecipeId!!)
                startActivity(intent)
            } else {
                Toast.makeText(this, "레시피 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 레시피 등록한 레시피 확인 (작은 등록하기 클릭시 화면 이동)
        register.setOnClickListener {
            if (createdRecipeId != null) {
                val intent = Intent(this, RecipeSeeActivity::class.java)
                intent.putExtra("recipeId", createdRecipeId!!)
                startActivity(intent)
            } else {
                Toast.makeText(this, "레시피 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener {
            registerRecipeUpLayout.visibility = View.VISIBLE
            registerRecipeSeeLayout.visibility = View.VISIBLE
            tapBar.visibility = View.GONE
            contentCheckTapBar.visibility = View.GONE
            recipeRegister.visibility = View.GONE
            contentCheckLayout.visibility = View.GONE
            recipeWrite.visibility = View.GONE
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

    // 레이아웃 위치 업데이트 함수
    private fun updateDropdownPosition(button: ImageButton) {
        val layout = findViewById<ConstraintLayout>(R.id.materialDropDown)
        val layoutParams = layout.layoutParams as? ConstraintLayout.LayoutParams

        if (layoutParams != null) {
            layoutParams.topToBottom = button.id  // 버튼의 아래로 레이아웃을 배치
            layoutParams.topMargin = (7 * resources.displayMetrics.density).toInt()  // 7dp 간격을 px로 변환
            layout.layoutParams = layoutParams
        }
    }

    // 텍스트뷰 위치 업데이트 함수
    private fun updateDropdownTextPosition(textView: TextView) {
        val layout = findViewById<ConstraintLayout>(R.id.materialDropDown)
        val layoutParams = layout.layoutParams as? ConstraintLayout.LayoutParams

        if (layoutParams != null) {
            layoutParams.topToBottom = textView.id  // 텍스트뷰의 아래로 레이아웃을 배치
            layoutParams.topMargin = (7 * resources.displayMetrics.density).toInt()  // 7dp 간격을 px로 변환
            layout.layoutParams = layoutParams
        }
    }

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

        // 🔹 드롭다운 버튼 클릭 이벤트 설정
        dropDownSix.setOnClickListener {
            showDropdownMenu(unitSix) // 드롭다운 표시
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

    // 🔹 드롭다운을 표시하는 함수
    private fun showDropdownMenu(unitView: TextView) {
        val materialDropDown = findViewById<ConstraintLayout>(R.id.materialDropDown)

        // 드롭다운 열기
        materialDropDown.visibility = View.VISIBLE

        // 드롭다운 내부의 TextView(옵션) 클릭 이벤트 설정
        for (i in 0 until materialDropDown.childCount) {
            val child = materialDropDown.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    // 선택한 텍스트를 unitView에 설정
                    unitView.text = child.text.toString()
                    unitView.setTextColor(Color.parseColor("#2B2B2B")) // 색상 변경
                    materialDropDown.visibility = View.GONE // 드롭다운 닫기
                }
            }
        }
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
    private fun addRecipeStep(step: Int) {

        val editText = EditText(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(45, 38, 45, 0) // 기존처럼 38dp 상단 마진 설정
            }
            setText("1-$recipeStepCount")
            hint = "레시피를 입력해주세요."
            textSize = 13f
            backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#A1A9AD"))// Step 번호에 따라 텍스트 설정 (예: 2-2, 2-3)
        }

        // 구분 바(View) 생성
        val divider = View(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(45, 12, 45, 0) // 기존처럼 12dp 상단 마진 설정
            }
            setBackgroundColor(Color.parseColor("#D9D9D9")) // 배경 색상 설정
        }

        // 기존 버튼 가져오기
        val addButton = findViewById<Button>(R.id.contentAdd)
        val timerButton = findViewById<Button>(R.id.timerAdd)

        // 🚀 버튼 위치 조정 (입력 칸과 70dp 떨어지게 설정)
        val buttonParams = addButton.layoutParams as ViewGroup.MarginLayoutParams
        buttonParams.topMargin += 15 // 🔽 입력 칸과 70dp 간격 유지
        addButton.requestLayout()

        val timerParams = timerButton.layoutParams as ViewGroup.MarginLayoutParams
        timerParams.topMargin += 15 // 🔽 동일하게 70dp 유지
        timerButton.requestLayout()

        // 🔽 UI에 추가
        cookOrderRecipeContainer.addView(editText)
        cookOrderRecipeContainer.addView(divider)
    }

    // 조리순서 step 추가 후 내용 추가하기
    val stepRecipeCountMap = mutableMapOf<Int, Int>()

    private fun addNewStep() {
        // 기존 stepContainer 내부의 모든 뷰 제거
        stepContainer.removeAllViews()

        // STEP 번호 증가
        stepCount++

        // 새로운 STEP 레이아웃 인플레이트
        val newStepLayout =
            LayoutInflater.from(this).inflate(R.layout.item_step, stepContainer, false)

        // STEP 번호 업데이트
        val stepTextView = newStepLayout.findViewById<TextView>(R.id.stepOne)
        stepTextView.text = "STEP $stepCount"

        // 세부 단계 번호 업데이트 (stepCount와 stepRecipeCount 값을 기반으로)
        val stepLittleTextView = newStepLayout.findViewById<TextView>(R.id.stepLittleOne)
        stepLittleTextView.text = "$stepCount-${stepRecipeCountMap[stepCount] ?: 1}"

        // 카메라 버튼 찾기
        val stepCamera = newStepLayout.findViewById<ImageButton>(R.id.stepCamera)

        // 내용추가 버튼 선언
        val contentAddTwo = newStepLayout.findViewById<Button>(R.id.contentAddTwo)
        val timerAddTwo = newStepLayout.findViewById<Button>(R.id.timerAddTwo)

        // 버튼이 보이도록 설정
        stepCamera.visibility = View.VISIBLE
        stepCamera.isClickable = true

        // 카메라 버튼 클릭 시 갤러리 열기
        stepCamera.setOnClickListener {
            Log.d("StepCamera", "카메라 버튼 클릭됨!") // ✅ 로그 추가
            pickImageLauncherForStepCamera.launch("image/*")
        }

        // 내용추가 버튼 클릭 시 내용추가
        contentAddTwo.setOnClickListener {
            // 현재 STEP에 해당하는 recipeStepCount 가져오기
            val currentRecipeStepCount = stepRecipeCountMap[stepCount] ?: 2

            // 동적으로 EditText 생성
            val editText = EditText(this).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(51, 38, 45, 0) // 기존처럼 38dp 상단 마진 설정
                }
                // stepCount와 recipeStepCount로 초기화
                setText("${stepCount}-${currentRecipeStepCount}")
                hint = "레시피를 입력해주세요."
                textSize = 13f
                backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#A1A9AD")) // 배경 색상 설정
            }

            // 동적으로 구분선(View) 생성
            val divider = View(this).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    setMargins(45, 12, 45, 0) // 기존처럼 12dp 상단 마진 설정
                }
                setBackgroundColor(Color.parseColor("#D9D9D9")) // 배경 색상 설정
            }

            // STEP 순서 번호 증가
            stepRecipeCountMap[stepCount] = currentRecipeStepCount + 1 // 현재 STEP의 recipeStepCount 증가

            // 동적으로 추가된 EditText와 Divider를 cookOrderRecipeContainerAdd에 추가
            val dynamicRecipeInputContainer = newStepLayout.findViewById<LinearLayout>(R.id.cookOrderRecipeContainerAdd)

            dynamicRecipeInputContainer.apply {
                addView(editText)   // EditText 추가
                addView(divider)    // Divider 추가
            }

            // dp 값으로 변환하는 함수
            fun dpToPx(dp: Int): Int {
                val density = resources.displayMetrics.density
                return (dp * density).toInt()
            }

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

    // EditText에서 숫자 가져오기 (비어있으면 0 반환)
    private fun parseEditText(editText: EditText): Int {
        return editText.text.toString().trim().toIntOrNull() ?: 0
    }

    //dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
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
}
