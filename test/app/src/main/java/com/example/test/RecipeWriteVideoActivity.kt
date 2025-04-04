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
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import com.example.test.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private lateinit var materialContainer: LinearLayout
private lateinit var replaceMaterialContainer: LinearLayout
private lateinit var handlingMethodContainer: LinearLayout
private lateinit var cookOrderRecipeContainer: LinearLayout
private lateinit var addFixButton: Button
private lateinit var replaceMaterialAddFixButton: Button
private lateinit var handlingMethodAddFixButton: Button
private var itemCount = 0 // 추가된 개수 추적
private val maxItems = 10 // 최대 10개 제한
private val buttonMarginIncrease = 130 // 버튼을 아래로 내릴 거리 (px)
private lateinit var cookVideoCamera: ImageButton
private lateinit var detailSettleCamera: ImageButton
private lateinit var imageContainer: LinearLayout
private lateinit var representImageContainer: LinearLayout
private lateinit var levelChoice: ConstraintLayout
private lateinit var requiredTimeAndTag: ConstraintLayout
private lateinit var root: ConstraintLayout  // 전체 레이아웃

class RecipeWriteVideoActivity : AppCompatActivity() {

    private var targetContainer: LinearLayout? = null  // 선택한 이미지가 추가될 컨테이너 저장
    private var selectedVideoUri: Uri? = null
    private var recipeVideoUrl: String? = null  // 서버에 업로드된 영상 URL 저장용
    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            showVideoInfo(uri)         // 파일명 표시
            uploadVideoToServer(uri)   // 서버 업로드
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_video)

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
        val recipeWriteCategory = findViewById<ConstraintLayout>(R.id.recipeWriteCategory)
        val recipeWrite = findViewById<ConstraintLayout>(R.id.recipeWrite)
        val indicatorBar = findViewById<View>(R.id.divideRectangleBarThirtythree)

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

        // 레시피 조리영상 선언
        val root = findViewById<ConstraintLayout>(R.id.root)
        val recipeWriteCookVideoLayout = findViewById<ConstraintLayout>(R.id.recipeWriteCookVideoLayout)
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)
        val cookVideoCamera = findViewById<ImageButton>(R.id.cookVideoCamera)

        // 레시피 세부설정 선언
        val recipeWriteDetailSettleLayout = findViewById<ConstraintLayout>(R.id.recipeWriteDetailSettleLayout)
        val representImageContainer = findViewById<LinearLayout>(R.id.representImageContainer)
        val levelBoxChoice = findViewById<ConstraintLayout>(R.id.levelBoxChoice)
        val requiredTimeAndTag = findViewById<ConstraintLayout>(R.id.requiredTimeAndTag)
        val detailSettleCamera = findViewById<ImageButton>(R.id.detailSettleCamera)
        val elementaryLevel = findViewById<TextView>(R.id.elementaryLevel)
        val detailSettleDownArrow = findViewById<ImageButton>(R.id.detailSettleDownArrow)
        val zero = findViewById<EditText>(R.id.zero)
        val halfHour = findViewById<EditText>(R.id.halfHour)
        val detailSettleRecipeTitleWrite = findViewById<View>(R.id.detailSettleRecipeTitleWrite)

        // 작성한 내용 확인 선언
        val contentCheckLayout = findViewById<ConstraintLayout>(R.id.contentCheckLayout)
        val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
        val contentCheckTapBar = findViewById<ConstraintLayout>(R.id.contentCheckTapBar)
        val shareFixButton = findViewById<Button>(R.id.shareFixButton)
        val registerFixButton = findViewById<Button>(R.id.registerFixButton)
        val register = findViewById<Button>(R.id.register)

        // 완료된 내용 확인 선언
        val registerRecipeUpLayout = findViewById<ConstraintLayout>(R.id.registerRecipeUpLayout)
        val registerRecipeSeeLayout = findViewById<ConstraintLayout>(R.id.registerRecipeSeeLayout)

        // 카테고리 TextView 리스트
        val textViews = listOf(
            findViewById<TextView>(R.id.one),
            findViewById<TextView>(R.id.two),
            findViewById<TextView>(R.id.three),
            findViewById<TextView>(R.id.four),
            findViewById<TextView>(R.id.five),
            findViewById<TextView>(R.id.six)
        )

        // ConstraintLayout 리스트 (TextView와 1:1 매칭)
        val layouts = listOf(
            findViewById<ConstraintLayout>(R.id.recipeWriteTitleLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteReplaceMaterialLayout),
            findViewById<ConstraintLayout>(R.id.recipeWriteHandlingMethodLayout),
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
                // 마지막 화면이면 contentCheckLayout으로 이동
                layouts[currentIndex].visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.contentCheckLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.contentCheckTapFix).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.recipeWriteCategory).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarTwo).visibility = View.GONE
                findViewById<View>(R.id.divideRectangleBarThirtythree).visibility = View.GONE
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
        }// 레시피 재료 materialDropDown을 findViewById로 제대로 연결
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

        // 레시피 조리영상 갤러리 불러오기
        cookVideoCamera.setOnClickListener {
            targetContainer = imageContainer  // 이 버튼을 누르면 imageContainer에 추가
            videoPickerLauncher.launch("video/*")
        }

        detailSettleCamera.setOnClickListener {
            targetContainer = representImageContainer  // 이 버튼을 누르면 representImageContainer에 추가
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
            val shareSettleLayout = findViewById<ConstraintLayout>(R.id.shareSettle)
            shareSettleLayout.visibility = View.VISIBLE
        }

        // 레시피 등록한 레시피 확인 (작은 등록하기 클릭시 화면 이동)
        register.setOnClickListener {
            registerRecipeUpLayout.visibility = View.VISIBLE
            registerRecipeSeeLayout.visibility = View.VISIBLE
            contentCheckTapBar.visibility = View.GONE
            recipeRegister.visibility = View.GONE
            contentCheckLayout.visibility = View.GONE
            recipeWrite.visibility = View.GONE
        }

        // 레시피 등록한 레시피 확인 (큰 등록하기 클릭시 화면 이동)
        registerFixButton.setOnClickListener {
            registerRecipeUpLayout.visibility = View.VISIBLE
            registerRecipeSeeLayout.visibility = View.VISIBLE
            contentCheckTapBar.visibility = View.GONE
            recipeRegister.visibility = View.GONE
            contentCheckLayout.visibility = View.GONE
            recipeWrite.visibility = View.GONE
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
                        val videoUrl = responseBody.string()  // 문자열 직접 파싱
                        Log.d("Upload", "영상 업로드 성공: $videoUrl")
                        recipeVideoUrl = videoUrl
                    } else {
                        Log.e("Upload", "업로드 실패 - 응답 없음 또는 실패 응답: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "업로드 실패: ${t.message}")
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

    // 레시피 재료 레이아웃 위치 업데이트 함수
    private fun updateDropdownPosition(button: ImageButton) {
        val layout = findViewById<ConstraintLayout>(R.id.materialDropDown)
        val layoutParams = layout.layoutParams as? ConstraintLayout.LayoutParams

        if (layoutParams != null) {
            layoutParams.topToBottom = button.id  // 버튼의 아래로 레이아웃을 배치
            layoutParams.topMargin = (7 * resources.displayMetrics.density).toInt()  // 7dp 간격을 px로 변환
            layout.layoutParams = layoutParams
        }
    }

    // 레시피 재료 텍스트뷰 위치 업데이트 함수
    private fun updateDropdownTextPosition(textView: TextView) {
        val layout = findViewById<ConstraintLayout>(R.id.materialDropDown)
        val layoutParams = layout.layoutParams as? ConstraintLayout.LayoutParams

        if (layoutParams != null) {
            layoutParams.topToBottom = textView.id  // 텍스트뷰의 아래로 레이아웃을 배치
            layoutParams.topMargin = (7 * resources.displayMetrics.density).toInt()  // 7dp 간격을 px로 변환
            layout.layoutParams = layoutParams
        }
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

    // 레시피 조리영상, 세부설정 카메라 클릭시 갤러리 열리기
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
}
