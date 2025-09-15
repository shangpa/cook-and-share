/*레시피 동영상*/
package com.example.test

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import com.bumptech.glide.Glide
import com.example.test.Repository.RecipeRepository
import com.example.test.model.CookingStep
import com.example.test.model.Ingredient
import com.example.test.model.RecipeRequest
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.test.Utils.TabBarUtils
import java.util.Stack

private lateinit var materialContainer: LinearLayout
private lateinit var replaceMaterialContainer: LinearLayout
private lateinit var handlingMethodContainer: LinearLayout
private lateinit var cookOrderRecipeContainer: LinearLayout
private lateinit var addFixButton: AppCompatButton
private lateinit var replaceMaterialAddFixButton: AppCompatButton
private lateinit var handlingMethodAddFixButton: AppCompatButton
private var itemCount = 0 // 추가된 개수 추적
private val maxItems = 10 // 최대 10개 제한
private val buttonMarginIncrease = 130 // 버튼을 아래로 내릴 거리 (px)
private lateinit var videoCameraLauncher: ActivityResultLauncher<Intent>
private lateinit var representImageContainer: LinearLayout
private lateinit var levelChoice: ConstraintLayout
private lateinit var requiredTimeAndTag: ConstraintLayout
private lateinit var root: ConstraintLayout  // 전체 레이아웃
private var createdRecipeId: Long? = null
private var isPublic: Boolean = true //공개설정용
private var recipe: RecipeRequest? = null
private var currentIndex = 0
private lateinit var layouts: List<ConstraintLayout>
private val layoutHistoryStack = Stack<ConstraintLayout>()
private var lastPushedLayout: ConstraintLayout? = null
private var isNavigatingBack = false

@androidx.media3.common.util.UnstableApi
@OptIn(UnstableApi::class)
class RecipeWriteVideoActivity : AppCompatActivity() {
    //메인 이미지
    private var mainImageUrl: String = "" // 대표 이미지 저장용 변수
    private var isVideoUploading = false // 업로드 중 여부 체크
    private var targetContainer: LinearLayout? = null  // 선택한 이미지가 추가될 컨테이너 저장
    private var selectedVideoUri: Uri? = null
    private var recipeVideoUrl: String? = null  // 서버에 업로드된 영상 URL 저장용
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            targetContainer?.let { container ->
                addImageToContainer(it, container)  // 선택한 컨테이너에 이미지 추가
                moveLayoutsDown(265) // 이미지 추가 시 레이아웃 이동
            }
        }
    }

    private var filteredIngredients: List<Pair<String, String>> = emptyList()
    private var replaceIngredients: List<String> = emptyList()
    private var handlingMethods: List<String> = emptyList()
    // 대표사진 이미지 업로드
    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                displaySelectedImage(it, representImageContainer) // 대표 이미지 표시
                uploadImageToServer(it) { imageUrl ->
                    if (imageUrl != null) {
                        Log.d("Upload", "대표 이미지 업로드 성공! URL: $imageUrl")
                        mainImageUrl = imageUrl // 대표 이미지 저장
                    } else {
                        Log.e("Upload", "대표 이미지 업로드 실패")
                    }
                }
            }
        }
    private val videoTrimLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val trimmedUri = result.data?.getParcelableExtra<Uri>("trimmedUri")
            trimmedUri?.let {
                selectedVideoUri = it
                showVideoInfo(it)
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

    private val tabCompleted = BooleanArray(6) { false }
    private lateinit var progressBars: List<View>
    private var selectedIndex = 0
    private lateinit var textViews: List<TextView>


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_video)

        //화면 저장
        layoutHistoryStack.clear()
        val firstLayout = findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout)
        layoutHistoryStack.push(firstLayout)
        lastPushedLayout = firstLayout

        TabBarUtils.setupTabBar(this)

        // 재료
        materialContainer = findViewById(R.id.materialContainer)
        addFixButton = findViewById(R.id.addFixButton)

        addFixButton.setOnClickListener {
            if (itemCount < maxItems) {
                addNewItem()
                moveButtonDown() // 버튼 아래로 이동
            }
        }
        representImageContainer = findViewById(R.id.representImageContainer)
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
        val uncheck = findViewById<ImageButton>(R.id.uncheck)
        val uncheckTwo = findViewById<ImageButton>(R.id.uncheckTwo)
        val settle = findViewById<AppCompatButton>(R.id.settle)
        val cancelTwo = findViewById<AppCompatButton>(R.id.cancelTwo)
        // 카테고리 선언
        val recipeWriteCategory = findViewById<ConstraintLayout>(R.id.recipeWriteCategory)
        val recipeWrite = findViewById<ConstraintLayout>(R.id.recipeWrite)
        val indicatorBar = findViewById<View>(R.id.divideRectangleBarThirtythree)
        indicatorBar.post {
            val textView = findViewById<TextView>(R.id.one)
            val targetX = textView.x + (textView.width / 2) - (indicatorBar.width / 2)
            indicatorBar.x = targetX
        }
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
        val btnCancel = findViewById<AppCompatButton>(R.id.cancelThree)
        val btnStore = findViewById<AppCompatButton>(R.id.store)

        // 레시피 재료 선언
        val recipeWriteMaterialLayout = findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout)
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


        // 레시피 조리영상 선언
        val root = findViewById<ConstraintLayout>(R.id.root)
        val recipeWriteCookVideoLayout = findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout)
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)
        val cookVideoCamera = findViewById<ImageButton>(R.id.cookVideoCamera)
        val cookVideoFoodName = findViewById<TextView>(R.id.cookVideoFoodName)
        val cookVideoKoreanFood = findViewById<TextView>(R.id.cookVideoKoreanFood)


        // 레시피 세부설정 선언
        val recipeWriteDetailSettleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        val requiredTimeAndTag = findViewById<ConstraintLayout>(R.id.requiredTimeAndTag)
        val detailSettleCamera = findViewById<ImageButton>(R.id.detailSettleCamera)
        val elementaryLevel = findViewById<TextView>(R.id.elementaryLevel)
        val detailSettleDownArrow = findViewById<ImageButton>(R.id.detailSettleDownArrow)
        val zero = findViewById<EditText>(R.id.zero)
        val halfHour = findViewById<EditText>(R.id.halfHour)
        val detailSettleRecipeTitleWrite = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite)
        val detailSettleFoodName = findViewById<TextView>(R.id.detailSettleFoodName)
        val detailSettleKoreanFood = findViewById<TextView>(R.id.detailSettleKoreanFood)


        // 작성한 내용 확인 선언
        val contentCheckLayout = findViewById<ConstraintLayout>(R.id.contentCheckLayout)
        val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
        val contentCheckTapBar = findViewById<ConstraintLayout>(R.id.contentCheckTapBar)
        val shareFixButton = findViewById<AppCompatButton>(R.id.shareFixButton)
        val registerFixButton = findViewById<AppCompatButton>(R.id.registerFixButton)
        val register = findViewById<AppCompatButton>(R.id.register)

        videoCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val videoUri = result.data?.data
                if (videoUri != null) {
                    selectedVideoUri = videoUri
                    showVideoInfo(videoUri)
                    uploadVideoToServer(videoUri)
                }
            }
        }

        // 카테고리 TextView 리스트
        textViews = listOf(
            findViewById<TextView>(R.id.one),
            findViewById<TextView>(R.id.two),
            findViewById<TextView>(R.id.three),
            findViewById<TextView>(R.id.four),
            findViewById<TextView>(R.id.five),
            findViewById<TextView>(R.id.six)
        )

        layouts = listOf(
            findViewById(R.id.recipeWriteTitleLayout),
            findViewById(R.id.recipeWriteMaterialLayout),
            findViewById(R.id.recipeWriteReplaceMaterialLayout),
            findViewById(R.id.recipeWriteHandlingMethodLayout),
            findViewById(R.id.recipeWriteCookVideoLayout),
            findViewById(R.id.recipeWriteDetailSettleLayout)
        )

        progressBars = listOf(
            findViewById(R.id.barOne),
            findViewById(R.id.barTwo),
            findViewById(R.id.barThree),
            findViewById(R.id.barFour),
            findViewById(R.id.barFive),
            findViewById(R.id.barSix)
        )

        // 카테고리 TextView 클릭 시 해당 화면으로 이동 & 바 위치 변경
        textViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                showOnlyLayout(layouts[index])

                // 인덱스 갱신
                selectedIndex = index
                currentIndex = index

                // 인디케이터 이동
                val targetX = tv.x + (tv.width / 2f) - (indicatorBar.width / 2f)
                indicatorBar.x = targetX

                // ✅ 색상은 항상 완료/선택 규칙으로만
                updateSelectedTab(tv)      // 또는 checkTabs() 호출(내부에서 updateSelectedTab 호출됨)
                checkAndUpdateContinueButton()
            }
        }

        // 현재 활성화된 화면 인덱스 추적 변수
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

        // "계속하기" 버튼 클릭 시 화면 이동
        continueButton.setOnClickListener {
            if (currentIndex < layouts.size - 1 && currentIndex < textViews.size - 1) {
                currentIndex++
                showOnlyLayout(layouts[currentIndex])

                // 해당 TextView 색상 변경
                textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }
                textViews[currentIndex].setTextColor(Color.parseColor("#2B2B2B"))

                // 바(View)의 위치 변경
                val targetX =
                    textViews[currentIndex].x + (textViews[currentIndex].width / 2) - (indicatorBar.width / 2)
                indicatorBar.x = targetX

                // 새로운 레이아웃으로 전환된 후 버튼 상태를 재확인
                checkAndUpdateContinueButton()
            } else {
                // 마지막 화면이면 contentCheckLayout
                //대표이미지 가져오기
                showOnlyLayout(findViewById(R.id.contentCheckLayout))
                val representativeImage = findViewById<ImageView>(R.id.representativeImage)
                val fullImageUrl = RetrofitInstance.BASE_URL + mainImageUrl.trim()
                Glide.with(this).load(fullImageUrl).into(representativeImage)
                // todo 동영상 업로드된거 가져와야함
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

                // 타이머 값 가져오기
                val cookingHour = zero.text.toString().takeIf { it.isNotBlank() }?.toInt() ?: 0
                val cookingMinute = halfHour.text.toString().takeIf { it.isNotBlank() }?.toInt() ?: 0

                //태그 값 가져오기
                val recipeTag = detailSettleRecipeTitleWrite.text.toString()

                // 화면에 표시할 TextView 찾기 (출력할 레이아웃이 있어야 함)
                findViewById<TextView>(R.id.checkFoodName).text = recipeTitle
                findViewById<TextView>(R.id.checkKoreanFood).text = categoryText
                findViewById<TextView>(R.id.foodNameTwo).text = recipeTag
                findViewById<TextView>(R.id.checkZero).text = cookingHour.toString()
                findViewById<TextView>(R.id.checkHalfHour).text = cookingMinute.toString()

                // 기존 레이아웃 변경 (가시성 설정 유지)
                layouts[currentIndex].visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.contentCheckTapBar).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarThirtythree).visibility = View.GONE
                findViewById<View>(R.id.tapBar).visibility = View.GONE
                // 소요시간 (조리시간)
                val totalCookingTime = (cookingHour.toInt() * 60) + cookingMinute.toInt()
                //난이도
                val difficulty = elementaryLevel.text.toString()
                // 카테고리 Enum 변환
                val categoryEnum = mapCategoryToEnum(categoryText)
                val videoThumbImageView = findViewById<ImageView>(R.id.image)
                val playButton = findViewById<ImageButton>(R.id.btnVideo)
                val playerView = findViewById<PlayerView>(R.id.videoPlayerView)
                if (!recipeVideoUrl.isNullOrEmpty()) {
                    // 썸네일 표시 (Glide의 .frame(0) 지원: videoUrl에서 첫 프레임을 가져옴)
                    Glide.with(this)
                        .load(RetrofitInstance.BASE_URL + recipeVideoUrl!!.trim())
                        .frame(0)
                        .into(videoThumbImageView)

                    videoThumbImageView.visibility = View.VISIBLE
                    playButton.visibility = View.VISIBLE
                    playerView.visibility = View.GONE // 미리보기일 땐 숨김

                    // 2. 재생 버튼 클릭 시 ExoPlayer로 영상 재생
                    playButton.setOnClickListener {
                        val player = ExoPlayer.Builder(this).build()
                        playerView.player = player
                        val mediaItem = MediaItem.fromUri(RetrofitInstance.BASE_URL + recipeVideoUrl!!.trim())
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                        playerView.visibility = View.VISIBLE
                        videoThumbImageView.alpha = 0f
                        playButton.visibility = View.GONE
                    }
                } else {
                    videoThumbImageView.visibility = View.GONE
                    playButton.visibility = View.GONE
                    playerView.visibility = View.GONE
                }
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
                    cookingSteps = gson.toJson(emptyList<CookingStep>()),//동영상만 보낼거라 조리순서 X
                    mainImageUrl = mainImageUrl,
                    difficulty = difficulty,
                    tags = recipeTag,
                    cookingTime = totalCookingTime,
                    servings = 2,
                    isPublic = true,
                    videoUrl = recipeVideoUrl ?: ""
                )
                Log.d("RecipeRequest", "전체 객체: ${gson.toJson(recipe)}")
                updateMaterialListView(
                    findViewById(R.id.materialList),
                    filteredIngredients,
                    replaceIngredients.map { it.split(" → ")[0] to it.split(" → ")[1] },
                    handlingMethods.map { it.split(" : ")[0] to it.split(" : ")[1] }
                )
            }

            selectedIndex = currentIndex
            checkTabs()
            return@setOnClickListener

            if (currentIndex in 0 until layouts.lastIndex) {
                val nextIndex = currentIndex + 1
                val nextLayout = layouts[nextIndex]
                val nextTab = textViews[nextIndex]

                // 히스토리 저장
                layoutHistoryStack.push(layouts[currentIndex])

                // 레이아웃 전환
                showOnlyLayout(nextLayout)

                // 탭 색상 업데이트
                textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }
                nextTab.setTextColor(Color.parseColor("#2B2B2B"))

                // 인디케이터 바 이동
                val targetX = nextTab.x + (nextTab.width / 2f) - (indicatorBar.width / 2f)
                indicatorBar.x = targetX

                // ✅ 인덱스 갱신 + 바/색 즉시 반영
                selectedIndex = nextIndex
                checkTabs()

                // 버튼 상태 갱신
                checkAndUpdateContinueButton()
            } else {
                // 마지막 위치면 그래도 탭/바 즉시 재반영
                selectedIndex = currentIndex
                checkTabs()
            }
        }

        // "이전으로" 버튼 클릭 시 화면 이동
        beforeButton.setOnClickListener {
            if (layoutHistoryStack.isNotEmpty()) {
                isNavigatingBack = true

                val previousLayout = layoutHistoryStack.pop()
                showOnlyLayout(previousLayout)

                isNavigatingBack = false

                val index = layouts.indexOf(previousLayout)
                if (index != -1) {
                    currentIndex = index
                    textViews.forEach { it.setTextColor(Color.parseColor("#A1A9AD")) }
                    textViews[currentIndex].setTextColor(Color.parseColor("#35A825"))

                    val targetX =
                        textViews[currentIndex].x + (textViews[currentIndex].width / 2) - (indicatorBar.width / 2)
                    indicatorBar.x = targetX
                }

                beforeButton.post {
                    checkAndUpdateContinueButton()
                }
            } else {
                // 스택 비었으면 맨 처음으로
                startActivity(Intent(this, RecipeWriteMain::class.java))
                finish()
            }
        }

        // 임시저장 버튼 클릭시 여부 나타남
        temporaryStorageBtn.setOnClickListener {
            transientStorageLayout.visibility = View.VISIBLE
        }

        // 임시저장 취소 클릭시 임시저장 여부 없어짐
        btnCancel.setOnClickListener {
            transientStorageLayout.visibility = View.GONE
        }

        // 임시저장 저장 클릭시 홈으로 이동
        btnStore.setOnClickListener {
            if (isVideoUploading) {
                Toast.makeText(this, "동영상 업로드가 끝날 때까지 기다려 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ recipe가 null이면 여기서 새로 만들어줌
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
                    cookingSteps = gson.toJson(emptyList<CookingStep>()), // 동영상만 보낼거라 조리순서 X
                    mainImageUrl = mainImageUrl,
                    difficulty = elementaryLevel.text.toString(),
                    tags = detailSettleRecipeTitleWrite.text.toString(),
                    cookingTime = (zero.text.toString().toIntOrNull() ?: 0) * 60 + (halfHour.text.toString().toIntOrNull() ?: 0),
                    servings = 2,
                    isPublic = false,
                    videoUrl = recipeVideoUrl ?: "",
                    isDraft = true,           // ✅ 임시저장
                    recipeType = "VIDEO"      // ✅ 비디오 작성
                )
            } else {
                // 이미 있는 recipe면 copy해서 draft로 바꿈
                recipe = recipe!!.copy(
                    isDraft = true,
                    isPublic = false,
                    recipeType = "VIDEO"
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
        recipeTitleWrite.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                foodName.text = text
                replaceMaterialFoodName.text = text
                handlingMethodFoodName.text = text
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
                val popup = PopupMenu(this@RecipeWriteVideoActivity, button)

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
            cookVideoKoreanFood.text = text
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


        cookVideoCamera.setOnClickListener {
            targetContainer = imageContainer
            // AlertDialog로 두 가지 선택지 제공
            AlertDialog.Builder(this)
                .setTitle("동영상 가져오기")
                .setItems(arrayOf("카메라 촬영", "앨범에서 선택")) { _, which ->
                    when (which) {
                        0 -> {
                            launchVideoCamera() // 위에서 만든 함수 그대로 사용!
                        }
                        1 -> {
                            // 🔵 갤러리에서 동영상 선택
                            videoPickerLauncher.launch("video/*")
                        }
                    }
                }.show()
        }

        detailSettleCamera.setOnClickListener {
            targetContainer = representImageContainer  // 이 버튼을 누르면 representImageContainer에 추가
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

        var isCheckedOne = false
        var isCheckedTwo = false
        uncheck.setOnClickListener {
            isCheckedOne = !isCheckedOne
            uncheck.setImageResource(if (isCheckedOne) R.drawable.ic_check else R.drawable.ic_uncheck)
            isCheckedTwo = false
            uncheckTwo.setImageResource(R.drawable.ic_uncheck)
            isPublic = true
            recipe = recipe?.copy(isPublic = true) // recipe 객체도 같이 업데이트
        }

        uncheckTwo.setOnClickListener {
            isCheckedTwo = !isCheckedTwo
            uncheckTwo.setImageResource(if (isCheckedTwo) R.drawable.ic_check else R.drawable.ic_uncheck)
            isCheckedOne = false
            uncheck.setImageResource(R.drawable.ic_uncheck)
            isPublic = false
            recipe = recipe?.copy(isPublic = false) // recipe 객체도 같이 업데이트
        }
        settle.setOnClickListener {
            shareSettle.visibility = View.GONE
            recipeRegister.visibility = View.VISIBLE
        }
        cancelTwo.setOnClickListener {
            recipeRegister.visibility = View.GONE
        }

        // 레시피 작성내용 확인 공유 설정 클릭시 상자 나타남
        shareFixButton.setOnClickListener {
            val shareSettleLayout = findViewById<ConstraintLayout>(R.id.shareSettle)
            shareSettleLayout.visibility = View.VISIBLE
        }

        // 레시피 등록한 레시피 확인 (작은 등록하기 클릭시 화면 이동)
        register.setOnClickListener {
            if (isVideoUploading) {
                Toast.makeText(this, "동영상 업로드가 끝날 때까지 기다려 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (recipe != null) {
                sendRecipeToServer(recipe!!, onSuccess = { recipeId ->
                    val intent = Intent(this, RecipeSeeMainActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    startActivity(intent)
                    finish()
                }, onFailure = {
                    Toast.makeText(this, "레시피 업로드 실패", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(this, "레시피 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener {
            if (isVideoUploading) {
                Toast.makeText(this, "동영상 업로드가 끝날 때까지 기다려 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (recipe != null) {
                sendRecipeToServer(recipe!!, onSuccess = { recipeId ->
                    val intent = Intent(this, RecipeSeeMainActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    startActivity(intent)
                    finish()
                }, onFailure = {
                    Toast.makeText(this, "레시피 업로드 실패", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(this, "레시피 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.backArrow).setOnClickListener {
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

        // ===== 5번 탭: 조리영상 =====
        val imageContainer = findViewById<LinearLayout?>(R.id.imageContainer)

        if (imageContainer != null) {
            // imageContainer 안에 자식 View(이미지)가 하나라도 있으면 완료 처리
            val hasImage = imageContainer.childCount > 0
            tabCompleted[4] = hasImage
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

        //바
        progressBars.forEachIndexed { index, bar ->
            if (index < tabCompleted.size) {
                bar.visibility = if (tabCompleted[index]) View.VISIBLE else View.GONE
            }
        }

        // ✅ 조건 만족 즉시 색상 반영
        if (::textViews.isInitialized && selectedIndex in textViews.indices) {
            updateSelectedTab(textViews[selectedIndex])
        }
    }

    // 탭 색상 업데이트
    private fun updateSelectedTab(selected: TextView) {
        textViews.forEachIndexed { index, tab ->
            val color = when {
                tabCompleted[index] -> "#2B2B2B" // 완료됨
                tab == selected -> "#35A825" // 현재 선택
                else -> "#A1A9AD" // 기본
            }
            tab.setTextColor(Color.parseColor(color))
        }
    }

    //계속하기 버튼 색 바뀜
    private fun checkAndUpdateContinueButton() {
        val continueButton = findViewById<AppCompatButton>(R.id.continueButton)
        val currentLayout = layouts.find { it.visibility == View.VISIBLE }

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

    private fun showOnlyLayout(targetLayout: ConstraintLayout) {
        val allLayouts = listOf(
            findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteReplaceMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteHandlingMethodLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout),
            findViewById<ConstraintLayout>(R.id.contentCheckLayout)
        )

        // 버튼과 탭바 등
        val continueButton = findViewById<AppCompatButton>(R.id.continueButton)
        val beforeButton = findViewById<AppCompatButton>(R.id.beforeButton)
        val register = findViewById<AppCompatButton>(R.id.register)
        val shareFixButton = findViewById<AppCompatButton>(R.id.shareFixButton)
        val contentCheckTapBar = findViewById<View>(R.id.contentCheckTapBar)
        val tapBar = findViewById<ConstraintLayout>(R.id.tapBar)
        val divideRectangleBarTwo = findViewById<View>(R.id.divideRectangleBarTwo)

        // 현재 보여지고 있는 레이아웃을 히스토리에 저장
        val currentlyVisible = allLayouts.find { it.visibility == View.VISIBLE }
        if (
            !isNavigatingBack &&
            currentlyVisible != null &&
            currentlyVisible.visibility == View.VISIBLE &&
            currentlyVisible != targetLayout &&
            currentlyVisible.id != R.id.contentCheckLayout &&
            currentlyVisible != lastPushedLayout // 이 조건 꼭 있어야 함
        ) {
            layoutHistoryStack.push(currentlyVisible)
            lastPushedLayout = currentlyVisible // 마지막으로 push한 레이아웃 저장

            Log.d("HistoryStack", "push: ${resources.getResourceEntryName(currentlyVisible.id)}")
            Log.d("HistoryStack", "stack: ${layoutHistoryStack.map { resources.getResourceEntryName(it.id) }}")
        }

        // 모든 레이아웃 숨기고 target만 표시
        allLayouts.forEach { it.visibility = if (it == targetLayout) View.VISIBLE else View.GONE }

        // contentCheckLayout이면 버튼 및 탭바 설정 다르게
        if (targetLayout.id == R.id.contentCheckLayout) {
            continueButton.visibility = View.GONE
            beforeButton.visibility = View.VISIBLE
            register.visibility = View.VISIBLE
            shareFixButton.visibility = View.VISIBLE
            contentCheckTapBar.visibility = View.VISIBLE
            tapBar.visibility = View.GONE
            divideRectangleBarTwo.visibility = View.GONE
        } else {
            continueButton.visibility = View.VISIBLE
            beforeButton.visibility = View.VISIBLE
            register.visibility = View.GONE
            shareFixButton.visibility = View.GONE
            contentCheckTapBar.visibility = View.GONE
            tapBar.visibility = View.VISIBLE
            divideRectangleBarTwo.visibility = View.VISIBLE
        }
    }

    private fun showVideoInfo(uri: Uri) {
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "이름 없음"

        val container = findViewById<LinearLayout>(R.id.imageContainer)
        container.removeAllViews()

        val textView = TextView(this).apply {
            text = "선택한 동영상: $fileName"
            textSize = 16f
            setTextColor(Color.BLACK)
        }
        container.addView(textView)
    }
    private fun uploadVideoToServer(uri: Uri) {
        Log.d("Upload", "영상 업로드 시작")
        isVideoUploading = true
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
                    isVideoUploading = false
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        val videoUrl = responseBody.string()
                        Log.d("Upload", "영상 업로드 성공: $videoUrl")
                        recipeVideoUrl = videoUrl
                        Log.d("Upload", "recipeVideoUrl 저장됨: $recipeVideoUrl")
                        Toast.makeText(this@RecipeWriteVideoActivity, "동영상 업로드 성공!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("Upload", "업로드 실패 - 응답 없음 또는 실패 응답: ${response.code()}")
                        Toast.makeText(this@RecipeWriteVideoActivity, "동영상 업로드 실패!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "업로드 실패: ${t.message}")
                    Toast.makeText(this@RecipeWriteVideoActivity, "동영상 업로드 실패!", Toast.LENGTH_SHORT).show()
                }
            })
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

    // 세부설정 카메라 클릭시 갤러리 열리기
    private fun addImageToContainer(imageUri: Uri, container: LinearLayout) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(336),  // 가로 336dp
                dpToPx(261)   // 세로 261dp
            ).apply {
                setMargins(0, dpToPx(10), 0, dpToPx(10))  // 이미지 간 간격
            }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP  // 중앙 정렬
        }

        container.addView(imageView)  // 동적으로 이미지 추가
    }

    // 레시피 세부설정 카메라 클릭시 난이도, 소요시간, 태그 아래로 내려감
    private fun moveLayoutsDown(offset: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(root)

        // levelChoice 위치 이동
        constraintSet.connect(
            levelChoice.id,
            ConstraintSet.TOP,
            representImageContainer.id,
            ConstraintSet.BOTTOM,
            dpToPx(offset) // 265dp 아래로 이동
        )

        // requiredTimeAndTag 위치 이동
        constraintSet.connect(
            requiredTimeAndTag.id,
            ConstraintSet.TOP,
            levelChoice.id,
            ConstraintSet.BOTTOM,
            dpToPx(20) // 원래 설정값 유지
        )

        constraintSet.applyTo(root)  // 변경 적용
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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
        fun Int.dpToPx(): Int {
            return (this * resources.displayMetrics.density).toInt()
        }
        val imageView = ImageView(this)
        imageView.setImageURI(uri)
        val layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
        imageView.layoutParams = layoutParams
        targetContainer.addView(imageView) // 선택한 컨테이너에 이미지 추가
        Log.d("RecipeWriteImageActivity", "이미지 추가 완료! 대상 컨테이너: ${targetContainer.id}")
    }
    private fun launchVideoCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCameraLauncher.launch(intent)
    }
    private fun updateMaterialListView(materialView: View, ingredients: List<Pair<String, String>>, alternatives: List<Pair<String, String>>, handling: List<Pair<String, String>>) {
        val categoryGroup = materialView.findViewById<GridLayout>(R.id.categoryGroup)
        categoryGroup.removeAllViews() // 기존 뷰 제거
        fun Int.dpToPx(): Int {
            return (this * resources.displayMetrics.density).toInt()
        }
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

        val filteredAlternatives = alternatives.filter { it.first.isNotBlank() && it.second.isNotBlank() }
        if (filteredAlternatives.isNotEmpty()) {
            addSectionTitle("대체 가능한 재료")
            filteredAlternatives.forEach { (original, replace) ->
                addMaterialItem(original, replace)
            }
        }

        val filteredHandling = handling.filter { it.first.isNotBlank() && it.second.isNotBlank() }
        if (filteredHandling.isNotEmpty()) {
            addSectionTitle("사용된 재료 처리 방법")
            filteredHandling.forEach { (ingredient, method) ->
                addMaterialItem(ingredient, method)
            }
        }
    }
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
