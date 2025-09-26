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
import com.example.test.model.recipeDetail.PublishRequest
import com.example.test.model.recipeDetail.RecipeCreateResponse
import com.example.test.model.recipeDetail.RecipeDraftDto
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
private var draftId: Long? = null

@androidx.media3.common.util.UnstableApi
@OptIn(UnstableApi::class)
class RecipeWriteVideoActivity : AppCompatActivity() {
    private var currentDraftId: Long? = null      // 드래프트에서 들어온 경우 여기만 신뢰
    private var currentRecipeType: String = "VIDEO"   // 이 화면은 영상 작성 화면이므로 기본 VIDEO 고정
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
    private val committedCompleted = BooleanArray(6) { false }
    private lateinit var progressBars: List<View>
    private var selectedIndex = 0
    private lateinit var textViews: List<TextView>
    private lateinit var indicatorBar: View
    private val rectToRow = mutableMapOf<View, View>()
    private val selectedRects = mutableSetOf<View>()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_video)

        currentDraftId = intent.getLongExtra("draftId", -1L).takeIf { it > 0L }

        // 영상 작성 화면은 타입 고정
        currentRecipeType = "VIDEO"

        if (currentDraftId != null) {
            loadDraftIfNeeded()   // 성공 시 안에서 currentDraftId 보강(서버값) + currentRecipeType 갱신
        }
        //화면 저장
        layoutHistoryStack.clear()
        val firstLayout = findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout)
        layoutHistoryStack.push(firstLayout)
        lastPushedLayout = firstLayout

        TabBarUtils.setupTabBar(this)

        // 재료
        materialContainer = findViewById(R.id.materialContainer)

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
        indicatorBar = findViewById(R.id.divideRectangleBarThirtythree)

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
        val materialCook = findViewById<EditText>(R.id.materialCook)
        val delete = findViewById<ImageButton>(R.id.delete)
        val divideRectangleBarFive = findViewById<View>(R.id.divideRectangleBarFive)
        val divideRectangleBarSix = findViewById<View>(R.id.divideRectangleBarSix)
        val divideRectangleBarSeven = findViewById<View>(R.id.divideRectangleBarSeven)
        val divideRectangleBarEight = findViewById<View>(R.id.divideRectangleBarEight)
        val divideRectangleBarNine = findViewById<View>(R.id.divideRectangleBarNine)
        val divideRectangleBarTen = findViewById<View>(R.id.divideRectangleBarTen)
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
        root = findViewById(R.id.root)
        val recipeWriteCookVideoLayout = findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout)
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)
        val cookVideoCamera = findViewById<ImageButton>(R.id.cookVideoCamera)
        val cookVideoFoodName = findViewById<TextView>(R.id.cookVideoFoodName)
        val cookVideoKoreanFood = findViewById<TextView>(R.id.cookVideoKoreanFood)


        // 레시피 세부설정 선언
        val recipeWriteDetailSettleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        requiredTimeAndTag = findViewById(R.id.requiredTimeAndTag)
        val detailSettleCamera = findViewById<ImageButton>(R.id.detailSettleCamera)
        val elementaryLevel = findViewById<TextView>(R.id.elementaryLevel)
        val detailSettleDownArrow = findViewById<ImageButton>(R.id.detailSettleDownArrow)
        val zero = findViewById<EditText>(R.id.zero)
        val halfHour = findViewById<EditText>(R.id.halfHour)
        val detailSettleRecipeTitleWrite = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite)
        val detailSettleFoodName = findViewById<TextView>(R.id.detailSettleFoodName)
        val detailSettleKoreanFood = findViewById<TextView>(R.id.detailSettleKoreanFood)
        levelChoice = findViewById(R.id.levelChoice)


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
                val leaving = currentIndex
                checkTabs()
                committedCompleted[leaving] = tabCompleted[leaving]
                updateProgressBars()

                // 인덱스 갱신
                selectedIndex = index
                currentIndex  = index
                showOnlyLayout(layouts[index])

                updateSelectedTab(tv)

                indicatorBar.post {
                    val x = tv.x + (tv.width / 2f) - (indicatorBar.width / 2f)
                    indicatorBar.x = x
                }
                checkAndUpdateContinueButton()
            }
        }

        selectedIndex = 0
        currentIndex  = 0
        showOnlyLayout(layouts[selectedIndex])
        updateSelectedTab(textViews[selectedIndex])

        indicatorBar.post {
            val tv = textViews[selectedIndex]
            indicatorBar.x = tv.x + (tv.width / 2f) - (indicatorBar.width / 2f)
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
                val leaving = currentIndex

                // 계산 최신화 후, 떠나는 탭 커밋
                checkTabs()
                committedCompleted[leaving] = tabCompleted[leaving]
                updateProgressBars()

                // 전환
                currentIndex++
                selectedIndex = currentIndex
                val nextLayout = layouts[selectedIndex]
                val nextTab = textViews[selectedIndex]
                showOnlyLayout(nextLayout)

                updateSelectedTab(nextTab)

                indicatorBar.post {
                    val x = nextTab.x + (nextTab.width / 2f) - (indicatorBar.width / 2f)
                    indicatorBar.x = x
                }
                checkAndUpdateContinueButton()
            } else {
                // 마지막 화면이면 contentCheckLayout
                val leaving = currentIndex
                checkTabs()
                committedCompleted[leaving] = tabCompleted[leaving]
                updateProgressBars()
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
                ingredients.add(material.text.toString() to "${measuring.text}")


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

            checkAndUpdateContinueButton()
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

            val dto = buildDraftDtoFromUI()

            val token = App.prefs.token ?: ""
            if (currentDraftId == null) {
                // ✅ 생성
                RetrofitInstance.apiService.createDraft("Bearer $token", dto)
                    .enqueue(object: retrofit2.Callback<RecipeCreateResponse> {
                        override fun onResponse(
                            call: Call<RecipeCreateResponse>,
                            res: Response<RecipeCreateResponse>
                        ) {
                            if (res.isSuccessful) {
                                val newId = res.body()?.recipeId
                                currentDraftId = newId           // ✅ 여기서 반드시 세팅
                                Toast.makeText(this@RecipeWriteVideoActivity, "임시저장 완료", Toast.LENGTH_SHORT).show()
                                goHome()
                            } else {
                                Toast.makeText(this@RecipeWriteVideoActivity, "임시저장 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<RecipeCreateResponse>, t: Throwable) {
                            Toast.makeText(this@RecipeWriteVideoActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                // ✅ 수정
                val draftId = currentDraftId!!
                RetrofitInstance.apiService.updateDraft("Bearer $token", draftId, dto)
                    .enqueue(object: retrofit2.Callback<RecipeDraftDto> {
                        override fun onResponse(
                            call: Call<RecipeDraftDto>,
                            res: Response<RecipeDraftDto>
                        ) {
                            if (res.isSuccessful) {
                                val body = res.body()
                                Toast.makeText(this@RecipeWriteVideoActivity, "임시저장 업데이트 완료", Toast.LENGTH_SHORT).show()
                                goHome()
                            } else {
                                Toast.makeText(this@RecipeWriteVideoActivity, "업데이트 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<RecipeDraftDto>, t: Throwable) {
                            Toast.makeText(this@RecipeWriteVideoActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
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

        // 재료 리스트 선택시 테두리 색 바뀌고 재료 글 추가
        val pairs = listOf(
            R.id.eggplantLayout to R.id.rect,
            R.id.potatoLayout to R.id.rectTwo,
            R.id.sweetPotatoLayout to R.id.rectThree,
            R.id.carrotLayout to R.id.rectFour,
            R.id.tomatoLayout to R.id.rectFive
        )

        fun toggle(rect: View) {
            val wasSelected = selectedRects.contains(rect)

            if (wasSelected) {
                // 해제
                rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                selectedRects.remove(rect)

                rectToRow[rect]?.let { row ->
                    materialContainer.removeView(row)
                    rectToRow.remove(rect)
                }
            } else {
                // 선택
                rect.setBackgroundResource(R.drawable.rounded_fridge_green)
                selectedRects.add(rect)

                if (selectedRects.size >= 2) {
                    val row = addNewItem(rect)  // ✅ rect 전달
                    rectToRow[rect] = row
                }
            }
        }

        pairs.forEach { (layoutId, rectId) ->
            val layout = findViewById<View>(layoutId)
            val rect = findViewById<View>(rectId)
            layout.setOnClickListener { toggle(rect) }
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

            val draftId = currentDraftId
            if (draftId != null) {
                // ✅ 기존 초안 발행(업데이트): 새 레코드 만들지 않음
                publishDraft(draftId, isPublic) {
                    // 발행 후 상세로 이동 (서버의 최종 recipeId를 응답에서 받는다면 그것을 사용)
                    val intent = Intent(this, RecipeSeeMainActivity::class.java)
                    intent.putExtra("recipeId", draftId) // 응답에 최종 id가 있으면 그 값으로 대체
                    startActivity(intent)
                    finish()
                }
            } else {
                // ✅ 초안 없이 바로 발행: 생성 요청 (recipeType = VIDEO 보장)
                val req = buildRecipeRequest(
                    isDraft = false,
                    recipeType = currentRecipeType // 이 화면은 VIDEO 고정
                )
                sendRecipeToServer(
                    req,
                    onSuccess = { recipeId ->
                        val intent = Intent(this, RecipeSeeMainActivity::class.java)
                        intent.putExtra("recipeId", recipeId)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = {
                        Toast.makeText(this, "레시피 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener { v ->
            v.isEnabled = false

            val publishPublic = isPublic

            val draftId = currentDraftId
            if (draftId != null) {
                // ✅ 드래프트에서 온 경우: publish 로 전환(새 글 만들지 않음)
                publishDraft(draftId, publishPublic) {
                    v.isEnabled = true
                    // TODO: 완료 후 이동 처리
                }
            } else {
                // ✅ 새로 작성한 경우: createRecipe 호출
                val req = buildRecipeRequest(
                    isDraft = false,
                    // 이 화면은 VIDEO 고정, 혹시 바꾸고 싶으면 currentRecipeType 사용
                    recipeType = currentRecipeType
                )
                sendRecipeToServer(
                    req,
                    onSuccess = { recipeId ->
                        v.isEnabled = true
                        // TODO: 완료 후 이동 처리
                    },
                    onFailure = {
                        v.isEnabled = true
                    }
                )
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

        updateProgressBars()
        updateSelectedTab(textViews[selectedIndex])

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

    private fun buildDraftDtoFromUI(): RecipeDraftDto {
        val gson = Gson()

        // 재료(정적 + 동적) 수집
        val ingredientsPairs = mutableListOf<Pair<String,String>>().apply {
            fun et(id:Int)= findViewById<EditText>(id).text.toString()
            // 정적 6칸
            add(et(R.id.material) to "${et(R.id.measuring)}")
            // 동적
            for (i in 0 until materialContainer.childCount) {
                val row = materialContainer.getChildAt(i) as? ViewGroup ?: continue
                val nameEt = row.getChildAt(0) as? EditText
                val qtyEt  = row.getChildAt(1) as? EditText
                if (nameEt!=null && qtyEt!=null ) {
                    val n = nameEt.text.toString().trim()
                    val a = "${qtyEt.text}".trim()
                    if (n.isNotEmpty() && a.isNotEmpty()) add(n to a)
                }
            }
        }.filter { it.first.isNotBlank() && it.second.isNotBlank() }

        // 대체재료 → 객체 리스트(JSON 문자열)
        val altPairs = listOf(
            findViewById<EditText>(R.id.replaceMaterialName).text.toString().trim()
                    to findViewById<EditText>(R.id.replaceMaterial).text.toString().trim(),
            findViewById<EditText>(R.id.replaceMaterialMaterialTwo).text.toString().trim()
                    to findViewById<EditText>(R.id.replaceMaterialTwo).text.toString().trim()
        ).filter { it.first.isNotBlank() || it.second.isNotBlank() }

        val handlingPairs = listOf(
            findViewById<EditText>(R.id.handlingMethodName).text.toString().trim()
                    to findViewById<EditText>(R.id.handlingMethod).text.toString().trim(),
            findViewById<EditText>(R.id.handlingMethodMaterialTwo).text.toString().trim()
                    to findViewById<EditText>(R.id.handlingMethodTwo).text.toString().trim()
        ).filter { it.first.isNotBlank() || it.second.isNotBlank() }

        // 서버 컬럼은 “문자열(JSON)”이므로 그대로 직렬화
        val ingredientsJson = gson.toJson(ingredientsPairs.map { Ingredient(it.first, it.second) })
        val alternativeIngredientsJson = gson.toJson(altPairs.map { Ingredient(it.first, it.second) })
        val handlingMethodsJson = gson.toJson(handlingPairs.map { "${it.first} : ${it.second}" })

        // 동영상 작성에선 cookingSteps 비움(비디오만)
        val cookingStepsJson = gson.toJson(emptyList<CookingStep>())

        val title = findViewById<EditText>(R.id.recipeTitleWrite).text.toString()
        val categoryText = findViewById<TextView>(R.id.koreanFood).text.toString()
        val categoryEnum = mapCategoryToEnum(categoryText)
        val difficulty = findViewById<TextView>(R.id.elementaryLevel).text.toString()
        val tags = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite).text.toString()
        val hour = findViewById<EditText>(R.id.zero).text.toString().toIntOrNull() ?: 0
        val minute = findViewById<EditText>(R.id.halfHour).text.toString().toIntOrNull() ?: 0
        val cookingTime = hour*60 + minute

        return RecipeDraftDto(
            recipeId = draftId,
            title = title,
            category = categoryEnum,
            ingredients = ingredientsJson,
            alternativeIngredients = alternativeIngredientsJson,
            handlingMethods = handlingMethodsJson,
            cookingSteps = cookingStepsJson,
            mainImageUrl = mainImageUrl,
            difficulty = difficulty,
            tags = tags,
            cookingTime = cookingTime,
            servings = 2,
            isPublic = isPublic,
            videoUrl = recipeVideoUrl ?: "",
            recipeType = currentRecipeType,
            isDraft = true
        )
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
    }

    // 탭 색상 업데이트
    private fun updateSelectedTab(selected: TextView) {
        textViews.forEachIndexed { index, tab ->
            val color = when {
                tab == selected -> "#35A825"                       // 현재 탭
                committedCompleted.getOrNull(index) == true -> "#2B2B2B" // 이동 시 커밋된 완료만 검정
                else -> "#A1A9AD"
            }
            tab.setTextColor(Color.parseColor(color))
        }
    }

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
                val container = currentLayout.findViewById<LinearLayout?>(R.id.imageContainer)
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

    private fun publishDraft(draftId: Long, isPublic: Boolean, done: () -> Unit) {
        val token = App.prefs.token ?: return done()

        RetrofitInstance.apiService.publishDraft(
            "Bearer $token",
            draftId,
            PublishRequest(isPublic = isPublic)
        ).enqueue(object : retrofit2.Callback<RecipeDraftDto> {
            override fun onResponse(
                call: Call<RecipeDraftDto>,
                res: Response<RecipeDraftDto>
            ) {
                if (res.isSuccessful) {
                    Toast.makeText(this@RecipeWriteVideoActivity, "레시피가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    // ✅ publish 성공 후에는 더 이상 draft 아님
                    currentDraftId = null
                } else {
                    Toast.makeText(this@RecipeWriteVideoActivity, "등록 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                }
                done()
            }

            override fun onFailure(call: Call<RecipeDraftDto>, t: Throwable) {
                Toast.makeText(this@RecipeWriteVideoActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                done()
            }
        })
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

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
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

    // Activity 내부 어디서든 접근 가능하게 간단 DTO
    private data class Ingredient(val name: String?, val amount: String?)

    // 필수: Gson 사용
    private val gson by lazy { com.google.gson.Gson() }

    /** UI 값을 읽어 RecipeRequest를 만든다 */
    private fun buildRecipeRequest(
        isDraft: Boolean,
        recipeType: String = currentRecipeType   // 이 화면은 기본 VIDEO
    ): RecipeRequest {
        val gson = Gson()

        // 1) 기본값들
        val title = findViewById<EditText>(R.id.recipeTitleWrite).text?.toString()?.trim().orEmpty()

        // 카테고리(화면 텍스트 → 서버 enum)
        val uiCategoryText = findViewById<TextView>(R.id.koreanFood).text?.toString()
        val categoryEnum = mapCategoryToEnum(uiCategoryText)

        // 2) 재료 수집 (정적 6칸 + 동적 컨테이너)
        fun readOneMaterial(nameId: Int, qtyId: Int): Ingredient? {
            val n = findViewById<EditText>(nameId).text?.toString()?.trim().orEmpty()
            val q = findViewById<EditText>(qtyId).text?.toString()?.trim().orEmpty()
            if (n.isBlank()) return null
            val amount = listOf(q).filter { it.isNotBlank() }.joinToString(" ")
            return Ingredient(n, amount.ifBlank { null })
        }

        val ingredients = mutableListOf<Ingredient>()

        listOf(
            R.id.material to R.id.measuring
        ).forEach { (n, q) ->
            readOneMaterial(n, q)?.let(ingredients::add)
        }

        findViewById<LinearLayout>(R.id.materialContainer)?.let { container ->
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                if (row is ViewGroup) {
                    var name: String? = null
                    var qty: String? = null
                    for (j in 0 until row.childCount) {
                        when (val v = row.getChildAt(j)) {
                            is EditText -> {
                                if (name == null) name = v.text?.toString()?.trim()
                                else if (qty == null) qty = v.text?.toString()?.trim()
                            }
                        }
                    }
                    if (!name.isNullOrBlank()) {
                        val amount = listOf(qty).filter { !it.isNullOrBlank() }.joinToString(" ")
                        ingredients += Ingredient(name, amount.ifBlank { null })
                    }
                }
            }
        }
        val ingredientsJson = gson.toJson(ingredients)

        // 3) 대체 재료
        val alternatives = mutableListOf<Ingredient>()
        fun readReplacePair(nameId: Int, replaceId: Int) {
            val n = findViewById<EditText>(nameId).text?.toString()?.trim().orEmpty()
            val r = findViewById<EditText>(replaceId).text?.toString()?.trim().orEmpty()
            if (n.isNotBlank() && r.isNotBlank()) alternatives += Ingredient(n, r)
        }
        readReplacePair(R.id.replaceMaterialName, R.id.replaceMaterial)
        readReplacePair(R.id.replaceMaterialMaterialTwo, R.id.replaceMaterialTwo)

        findViewById<LinearLayout>(R.id.replaceMaterialContainer)?.let { container ->
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                if (row is ViewGroup) {
                    var leftName: String? = null
                    var rightName: String? = null
                    for (j in 0 until row.childCount) {
                        val v = row.getChildAt(j)
                        if (v is EditText) {
                            if (leftName == null) leftName = v.text?.toString()?.trim()
                            else if (rightName == null) rightName = v.text?.toString()?.trim()
                        }
                    }
                    if (!leftName.isNullOrBlank() && !rightName.isNullOrBlank()) {
                        alternatives += Ingredient(leftName, rightName)
                    }
                }
            }
        }
        val alternativesJson = gson.toJson(alternatives)

        // 4) 처리 방법
        val handling = mutableListOf<String>()
        fun addHandling(nameId: Int, methodId: Int) {
            val n = findViewById<EditText>(nameId).text?.toString()?.trim().orEmpty()
            val m = findViewById<EditText>(methodId).text?.toString()?.trim().orEmpty()
            if (n.isNotBlank() && m.isNotBlank()) handling += "$n : $m"
        }
        addHandling(R.id.handlingMethodName, R.id.handlingMethod)
        addHandling(R.id.handlingMethodMaterialTwo, R.id.handlingMethodTwo)

        findViewById<LinearLayout>(R.id.handlingMethodContainer)?.let { container ->
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                if (row is ViewGroup) {
                    var ing: String? = null
                    var method: String? = null
                    for (j in 0 until row.childCount) {
                        val v = row.getChildAt(j)
                        if (v is EditText) {
                            if (ing == null) ing = v.text?.toString()?.trim()
                            else if (method == null) method = v.text?.toString()?.trim()
                        }
                    }
                    if (!ing.isNullOrBlank() && !method.isNullOrBlank()) {
                        handling += "$ing : $method"
                    }
                }
            }
        }
        val handlingJson = gson.toJson(handling)

        // 5) 난이도/태그/시간
        val difficulty = findViewById<TextView>(R.id.elementaryLevel).text?.toString()?.trim().orEmpty()
        val tags = findViewById<EditText>(R.id.detailSettleRecipeTitleWrite).text?.toString()?.trim().orEmpty()

        val hours = findViewById<EditText>(R.id.zero).text?.toString()?.trim()?.toIntOrNull() ?: 0
        val minutes = findViewById<EditText>(R.id.halfHour).text?.toString()?.trim()?.toIntOrNull() ?: 0
        val cookingTime = (hours * 60) + minutes

        // 6) 대표사진/영상/타입
        val mainUrl = mainImageUrl.orEmpty()
        val videoUrl = recipeVideoUrl.orEmpty()
        val type = recipeType.uppercase()   // 항상 "VIDEO"

        val servings = 1

        return RecipeRequest(
            title = title,
            category = categoryEnum,
            ingredients = ingredientsJson,
            alternativeIngredients = alternativesJson,
            handlingMethods = handlingJson,
            cookingSteps = "[]",
            mainImageUrl = mainUrl,
            difficulty = difficulty,
            tags = tags,
            cookingTime = cookingTime,
            servings = servings,
            isPublic = isPublic,
            videoUrl = videoUrl,
            isDraft = isDraft,
            recipeType = type
        )
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

    private fun loadDraftIfNeeded() {
        val id = currentDraftId ?: return
        val token = App.prefs.token ?: return

        RetrofitInstance.apiService.getMyDraftById("Bearer $token", id)
            .enqueue(object : retrofit2.Callback<RecipeDraftDto> {
                override fun onResponse(
                    call: Call<RecipeDraftDto>,
                    res: Response<RecipeDraftDto>
                ) {
                    val dto = res.body()
                    if (res.isSuccessful && dto != null) {
                        restoreDraftToUI(dto)

                        // 서버 식별자 재세팅(없으면 요청에 쓴 id라도 유지)
                        currentDraftId = dto.recipeId ?: id

                        // 타입 보존(없으면 VIDEO 유지)
                        currentRecipeType = dto.recipeType ?: "VIDEO"
                    } else {
                        Toast.makeText(
                            this@RecipeWriteVideoActivity,
                            "임시저장 조회 실패: ${res.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RecipeDraftDto>, t: Throwable) {
                    Toast.makeText(
                        this@RecipeWriteVideoActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun restoreDraftToUI(d: RecipeDraftDto) {
        val gson = Gson()
        // 기본
        findViewById<EditText>(R.id.recipeTitleWrite).setText(d.title.orEmpty())
        // 카테고리 역매핑(서버 enum → UI 텍스트)
        fun enumToKor(e:String?) = when(e) {
            "koreaFood"->"한식"; "westernFood"->"양식"; "japaneseFood"->"일식"
            "chineseFood"->"중식"; "vegetarianDiet"->"채식"; "snack"->"간식"
            "alcoholSnack"->"안주"; "sideDish"->"반찬"; "etc"->"기타"
            else -> "카테고리 선택"
        }
        findViewById<TextView>(R.id.koreanFood).text = enumToKor(d.category)

        mainImageUrl = d.mainImageUrl.orEmpty()
        if (mainImageUrl.isNotBlank()) {
            val iv = ImageView(this)
            val lp = LinearLayout.LayoutParams(dpToPx(336), dpToPx(261))
            iv.layoutParams = lp
            Glide.with(this).load(RetrofitInstance.BASE_URL + mainImageUrl.trim()).into(iv)
            representImageContainer.addView(iv)
        }

        // 난이도/태그/시간
        findViewById<TextView>(R.id.elementaryLevel).text = d.difficulty.orEmpty()
        findViewById<EditText>(R.id.detailSettleRecipeTitleWrite).setText(d.tags.orEmpty())
        val time = d.cookingTime ?: 0
        findViewById<EditText>(R.id.zero).setText((time/60).toString())
        findViewById<EditText>(R.id.halfHour).setText((time%60).toString())

        isPublic = d.isPublic ?: true
        recipeVideoUrl = d.videoUrl
        // 비디오 썸네일/플레이 버튼은 기존 contentCheck 쪽 로직 재사용 가능

        // 재료
        runCatching {
            val ing: List<Ingredient> = gson.fromJson(d.ingredients ?: "[]", object: com.google.gson.reflect.TypeToken<List<Ingredient>>(){}.type)
            // 정적 6칸 채우고 초과는 동적 add
            val fixed = listOf(
                R.id.material to R.id.measuring
            )
            fun splitQtyUnit(v:String): Pair<String,String> {
                val parts = v.trim().split(" ")
                if (parts.size<=1) return v to ""
                return parts.dropLast(1).joinToString(" ") to parts.last()
            }
            ing.forEachIndexed { i, it ->
                val (qty, unit) = splitQtyUnit(it.amount ?: "")
                if (i < fixed.size) {
                    val triple = fixed[i]
                    val nameId = (triple.first as Pair<Int,Int>).first
                    val measId = (triple.first as Pair<Int,Int>).second
                    findViewById<EditText>(nameId).setText(it.name.orEmpty())
                    findViewById<EditText>(measId).setText(qty)
                } else {
                    addNewItem()
                    val row = materialContainer.getChildAt(materialContainer.childCount-1) as ViewGroup
                    val nameEt = row.getChildAt(0) as EditText
                    val qtyEt  = row.getChildAt(1) as EditText
                    nameEt.setText(it.name.orEmpty())
                    qtyEt.setText(qty)
                }
            }
        }.onFailure { Log.e("DraftRestore","ingredients parse fail ${it.message}") }

        // 대체재료
        runCatching {
            val alts: List<Ingredient> = gson.fromJson(d.alternativeIngredients ?: "[]", object: com.google.gson.reflect.TypeToken<List<Ingredient>>(){}.type)
            val first = alts.getOrNull(0)
            val second = alts.getOrNull(1)
            findViewById<EditText>(R.id.replaceMaterialName).setText(first?.name.orEmpty())
            findViewById<EditText>(R.id.replaceMaterial).setText(first?.amount.orEmpty())
            if (second!=null) {
                findViewById<EditText>(R.id.replaceMaterialMaterialTwo).setText(second.name.orEmpty())
                findViewById<EditText>(R.id.replaceMaterialTwo).setText(second.amount.orEmpty())
            }
            // 초과분이 생기면 replaceMaterialAddNewItem() 만들어 채우는 로직도 추가 가능
        }

        // 처리방법(JSON 문자열이 ["재료 : 방법", ...] 형태)
        runCatching {
            val list: List<String> = gson.fromJson(d.handlingMethods ?: "[]", object: com.google.gson.reflect.TypeToken<List<String>>(){}.type)
            val first = list.getOrNull(0)?.split(" : ", limit=2)?.let { it.getOrNull(0) to it.getOrNull(1) }
            val second = list.getOrNull(1)?.split(" : ", limit=2)?.let { it.getOrNull(0) to it.getOrNull(1) }
            findViewById<EditText>(R.id.handlingMethodName).setText(first?.first.orEmpty())
            findViewById<EditText>(R.id.handlingMethod).setText(first?.second.orEmpty())
            if (second!=null) {
                findViewById<EditText>(R.id.handlingMethodMaterialTwo).setText(second.first.orEmpty())
                findViewById<EditText>(R.id.handlingMethodTwo).setText(second.second.orEmpty())
            }
        }

        // === 대표사진 복원 ===
        val representImageContainer = findViewById<LinearLayout>(R.id.representImageContainer)
        loadMainImageInto(representImageContainer, d.mainImageUrl)
        mainImageUrl = d.mainImageUrl.orEmpty()

        // === 동영상 복원 ===
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)

        d.videoUrl?.takeIf { it.isNotBlank() }?.let { vurl ->
            val uri = Uri.parse(vurl)
            showVideoInfo(uri)   // ← 여기서 재사용
            recipeVideoUrl = vurl
        }

        // 상태 갱신
        checkTabs()
        checkAndUpdateContinueButton()

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
            scaleType = ImageView.ScaleType.CENTER_CROP  // 중앙 정렬onActivityResult
        }

        container.addView(imageView)  // 동적으로 이미지 추가
        moveLayoutsDown(265)
    }

    // 레시피 세부설정 카메라 클릭시 난이도, 소요시간, 태그 아래로 내려감
    private fun moveLayoutsDown(offsetDp: Int) {
        val parent = findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)

        // 형제끼리 연결 ( 같은 부모)
        constraintSet.connect(
            R.id.levelChoice,
            ConstraintSet.TOP,
            R.id.representImageChoice,   // container(자식) 말고 choice(형제)
            ConstraintSet.BOTTOM,
            dpToPx(offsetDp)
        )
        constraintSet.connect(
            R.id.requiredTimeAndTag,
            ConstraintSet.TOP,
            R.id.levelChoice,
            ConstraintSet.BOTTOM,
            dpToPx(20) // 기존 마진 유지
        )

        constraintSet.applyTo(parent)
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
    private fun loadMainImageInto(container: LinearLayout, urlOrPath: String?) {
        if (urlOrPath.isNullOrBlank()) return

        val raw = urlOrPath.trim()
        val model: Any = when {
            raw.startsWith("content://") || raw.startsWith("file://") -> Uri.parse(raw)
            raw.startsWith("http://") || raw.startsWith("https://")   -> raw
            else -> {
                // 서버가 상대경로 주면 BASE_URL과 합치기
                val base = RetrofitInstance.BASE_URL.trimEnd('/')
                val path = raw.trimStart('/')
                "$base/$path"
            }
        }

        container.removeAllViews()

        val iv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(250), dpToPx(250)).apply {
                setMargins(20, dpToPx(10), 0, dpToPx(10))
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        container.addView(iv)

        Glide.with(this).load(model).into(iv)   // Any로 명시 → 오버로드 모호성 회피
    }
}
