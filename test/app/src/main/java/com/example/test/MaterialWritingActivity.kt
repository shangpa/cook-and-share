package com.example.test

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.model.TradePost.TradePostRepository
import com.example.test.model.TradePost.TradePostRequest
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.skt.tmap.TMapView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Stack

class MaterialWritingActivity : AppCompatActivity() {

    private lateinit var photoContainer: LinearLayout
    private lateinit var cameraCountText: TextView
    private val imageUrlList = mutableListOf<String>() // 여러 이미지 URL 저장용

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                photoContainer.visibility = View.VISIBLE
                findViewById<HorizontalScrollView>(R.id.photoScrollView).visibility = View.VISIBLE
                val imageView = ImageView(this).apply {
                    val sizeInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        150f,
                        resources.displayMetrics
                    ).toInt()

                    layoutParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                        // 왼쪽 20dp, 위쪽 25dp margin
                        val leftMarginPx = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            20f,
                            resources.displayMetrics
                        ).toInt()
                        val topMarginPx = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            25f,
                            resources.displayMetrics
                        ).toInt()
                        setMargins(leftMarginPx, topMarginPx, 0, 0)
                    }

                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageURI(uri)
                }

                photoContainer.addView(imageView)
                //이미지 업로드
                uploadImageToServer(uri) { imageUrl ->
                    if (imageUrl != null) {
                        imageUrlList.add(imageUrl)
                        Log.d("Upload", "업로드된 URL: $imageUrl")
                    } else {
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                // 사진 개수 텍스트 업데이트
                val count = photoContainer.childCount
                cameraCountText.text = "$count/10"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_writing) // 다른 프로필 화면의 레이아웃 파일 연결

        // detailViewIcon 클릭했을 때 MaterialOtherProfileActivity 이동
        val detailViewIcon: ImageView = findViewById(R.id.detailViewIcon)
        detailViewIcon.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            startActivity(intent)
        }

        // chatButton 클릭했을 때 MaterialChatDetailActivity 이동
        val chatButton: Button = findViewById(R.id.chatButton)
        chatButton.setOnClickListener {
            val intent = Intent(this, MaterialChatDetailActivity::class.java)
            startActivity(intent)
        }

        // imageSearch 클릭했을 때 MaterialSearchActivity 이동
        val imageSearch: ImageView = findViewById(R.id.imageSearch)
        imageSearch.setOnClickListener {
            val intent = Intent(this, MaterialSearchActivity::class.java)
            startActivity(intent)
        }

        // 거래규칙 선언
        val continueButton = findViewById<Button>(R.id.continueButton)
        val exchangeRule = findViewById<ConstraintLayout>(R.id.exchangeRule)

        // 새로운 거래글 선언
        val newExchangeWrite = findViewById<ConstraintLayout>(R.id.newExchangeWrite)
        val exchangeWishPlaceChoice = findViewById<ConstraintLayout>(R.id.exchangeWishPlaceChoice)
        val registerChoice = findViewById<ConstraintLayout>(R.id.registerChoice)
        val categoryBox = findViewById<FrameLayout>(R.id.categoryBox)
        val cameraIcon = findViewById<ImageView>(R.id.ic_camera)
        val arrowDown = findViewById<ImageView>(R.id.ic_arrow_down)
        val rigthArrow = findViewById<ImageView>(R.id.ic_rigth_arrow)
        val btn_close = findViewById<ImageView>(R.id.btn_close)
        val post = findViewById<Button>(R.id.post)
        val categoryText = findViewById<TextView>(R.id.categoryText)
        val titleText = findViewById<EditText>(R.id.titleText)
        val quantityText = findViewById<EditText>(R.id.quantityText)
        val transactionPriceText = findViewById<EditText>(R.id.transactionPriceText)
        val purchaseDateText = findViewById<EditText>(R.id.purchaseDateText)
        val descriptionText = findViewById<EditText>(R.id.descriptionText)
        val wishPlaceText = findViewById<TextView>(R.id.wishPlaceText)
        val cancel = findViewById<Button>(R.id.cancel)
        val register = findViewById<Button>(R.id.register)
        // 이미지 컨테이너 뷰 초기화
        photoContainer = findViewById<LinearLayout>(R.id.photoContainer)
        cameraCountText = findViewById<TextView>(R.id.camera)

        // 등록한 거래글 보기 선언
        val postSee = findViewById<ConstraintLayout>(R.id.postSee)
        val itemMore = findViewById<ImageView>(R.id.itemMore)

        // 거래 희망장소 화살표 클릭시 장소 선택 나타남
        continueButton.setOnClickListener {
            exchangeRule.visibility = View.GONE
            newExchangeWrite.visibility = View.VISIBLE
        }

        // 새로운 거래 카메라 버튼 클릭 시 갤러리 열기
        cameraIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        arrowDown.setOnClickListener {
            if (categoryBox.visibility == View.GONE) {
                categoryBox.visibility = View.VISIBLE
            } else {
                categoryBox.visibility = View.GONE
            }
        }

        // 카테고리 박스 선언
        val categoryViews = listOf(
            Pair(findViewById<LinearLayout>(R.id.categoryOne), "조리기구"),
            Pair(findViewById<LinearLayout>(R.id.categoryTwo), "팬/냄비류"),
            Pair(findViewById<LinearLayout>(R.id.categoryThree), "용기류"),
            Pair(findViewById<LinearLayout>(R.id.categoryFour), "식기류"),
            Pair(findViewById<LinearLayout>(R.id.categoryFive), "수납용품"),
            Pair(findViewById<LinearLayout>(R.id.categorySix), "위생용품"),
            Pair(findViewById<LinearLayout>(R.id.categorySeven), "소형가전"),
            Pair(findViewById<LinearLayout>(R.id.categoryEight), "일회용품"),
            Pair(findViewById<LinearLayout>(R.id.categoryNine), "기타")
        )

        // 여러가지 카테고리중 선택시 박스 없어지고 선택한 text 나타남
        for ((layout, text) in categoryViews) {
            layout.setOnClickListener {
                categoryText.text = text
                categoryText.setTextColor(Color.parseColor("#2B2B2B"))
                categoryBox.visibility = View.GONE
            }
        }

        // 거래 희망장소 화살표 클릭시 장소 선택 나타남
        rigthArrow.setOnClickListener {
            newExchangeWrite.visibility = View.GONE
            exchangeWishPlaceChoice.visibility = View.VISIBLE
        }

        val fields = listOf(
            findViewById<EditText>(R.id.titleText),
            findViewById<EditText>(R.id.quantityText),
            findViewById<EditText>(R.id.transactionPriceText),
            findViewById<EditText>(R.id.purchaseDateText),
            findViewById<EditText>(R.id.descriptionText)
        )


        // 입력값 확인 함수
        fun checkFieldsAndUpdateButton() {
            val allFilled = fields.all { it.text.toString().isNotBlank() } &&
                    categoryText.text.toString() != "카테고리를 선택해주세요." &&
                    wishPlaceText.text.toString() != "거래 희망 장소" // 기본값 체크

            if (allFilled) {
                post.setBackgroundColor(Color.parseColor("#35A825"))
                post.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                post.setBackgroundColor(Color.parseColor("#E7E7E7"))
                post.setTextColor(Color.parseColor("#A1A9AD"))
            }
        }

        // 실시간 감지용 TextWatcher
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = checkFieldsAndUpdateButton()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // 모든 EditText에 TextWatcher 연결
        fields.forEach { it.addTextChangedListener(textWatcher) }

        // 버튼 클릭 리스너
        post.setOnClickListener {
            when {
                titleText.text.toString().isBlank() -> {
                    Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }

                quantityText.text.toString().isBlank() -> {
                    Toast.makeText(this, "수량을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }

                transactionPriceText.text.toString().isBlank() -> {
                    Toast.makeText(this, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }

                purchaseDateText.text.toString().isBlank() -> {
                    Toast.makeText(this, "구매 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }

                descriptionText.text.toString().isBlank() -> {
                    Toast.makeText(this, "내용을 작성해주세요.", Toast.LENGTH_SHORT).show()
                }

                categoryText.text.toString() == "카테고리를 선택해주세요." -> {
                    Toast.makeText(this, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                }

                wishPlaceText.text.toString() == "거래 희망 장소" -> {
                    Toast.makeText(this, "거래 희망 장소를 선택해주세요.", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    // ✅ 여기에서 바로 처리
                    registerChoice.visibility = View.VISIBLE
                }
            }
        }

        // 거래 희망 장소 선택 닫기 누르면 거래 희망 장소 선택 화면 사라짐
        btn_close.setOnClickListener {
            exchangeWishPlaceChoice.visibility = View.GONE
        }

        // 등록 여부에서 취소 클릭시 등록 여부 화면 사라짐
        cancel.setOnClickListener {
            registerChoice.visibility = View.GONE
        }

        lateinit var tmapView: TMapView
        var selectedLat: Double? = null
        var selectedLng: Double? = null

        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)

        tmapView = TMapView(this)
        tmapView.setSKTMapApiKey("5p5MIj0ajg149wOnUX3Ui5WfNBUnZngt5kCE8TMc")
        mapContainer.addView(tmapView, 0)

        Handler(Looper.getMainLooper()).postDelayed({
            selectedLat = 37.49054682674008
            selectedLng = 126.77845157600154

            try {
                tmapView.setZoomLevel(15)
                tmapView.setCenterPoint(selectedLat!!, selectedLng!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            findViewById<Button>(R.id.btn_zoom_in).setOnClickListener {
                val currentZoom = tmapView.zoomLevel
                tmapView.zoomLevel = (currentZoom + 1).coerceAtMost(20)
            }

            findViewById<Button>(R.id.btn_zoom_out).setOnClickListener {
                val currentZoom = tmapView.zoomLevel
                tmapView.zoomLevel = (currentZoom - 1).coerceAtLeast(1)
            }

        }, 1500)

        tmapView.setOnDisableScrollWithZoomLevelListener { _, centerPoint ->
            selectedLat = centerPoint.latitude
            selectedLng = centerPoint.longitude
        }

        findViewById<Button>(R.id.btn_select).setOnClickListener {
            if (selectedLat != null && selectedLng != null) {
                wishPlaceText.text = "(${selectedLat}, ${selectedLng})"
                exchangeWishPlaceChoice.visibility = View.GONE
                newExchangeWrite.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "지도를 움직여 위치를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 등록 여부에서 등록 클릭시 등록한 거래글 보기로 화면 넘어감
        register.setOnClickListener {
            val category = categoryText.text.toString().trim()
            val title = titleText.text.toString().trim()
            val quantity = quantityText.text.toString().toIntOrNull() ?: 0
            val price = transactionPriceText.text.toString().toIntOrNull() ?: 0
            val purchaseDate = purchaseDateText.text.toString().trim() // yyyy-MM-dd
            val description = descriptionText.text.toString().trim()
            // todo 지도 위치 받아와야함
            // val location = wishPlaceText.text.toString().trim()
            val gson = Gson() // 이거 꼭 선언해야 함

            val locationString = if (selectedLat != null && selectedLng != null) {
                "${selectedLat},${selectedLng}"
            } else {
                ""
            }

            val request = TradePostRequest(
                category = category,
                title = title,
                quantity = quantity,
                price = price,
                purchaseDate = purchaseDate,
                description = description,
                location = locationString, // ← 여기!!
                imageUrls = gson.toJson(imageUrlList)
            )


            val token = App.prefs.token.toString()
            Log.d("TradePostRequest", "category=$category, title=$title, quantity=$quantity, price=$price, date=$purchaseDate, description=$description,imageUrls=$imageUrlList")
            TradePostRepository.uploadTradePost(token, request) { response ->
                if (response != null) {
                    val intent = Intent(this, MaterialDetailActivity::class.java)
                    intent.putExtra("tradePostId",response.tradePostId)
                    startActivity(intent)
                    Toast.makeText(this, "거래글 업로드 성공!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "거래글 업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // 등록한 거래글 보기 더하기 버튼 클릭시 수정, 삭제 나타남
        val moreButtons = listOf(itemMore)
        val layoutList = listOf(postSee) // 삭제 대상 LinearLayout들

        val popupItems = listOf("수정", "삭제")

        fun showPopup(anchorView: View, targetLayout: ConstraintLayout) {
            val popup = PopupMenu(this, anchorView)
            val popupItems = listOf("수정", "삭제")

            popupItems.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "수정" -> {
                        postSee.visibility = View.GONE
                        newExchangeWrite.visibility = View.VISIBLE
                        true
                    }

                    "삭제" -> {
                        // ✅ 삭제 클릭 시 MaterialActivity로 이동
                        val intent = Intent(this, MaterialActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }

        // 버튼과 레이아웃을 1:1 매칭
        moreButtons.zip(layoutList).forEach { (button, layout) ->
            button.setOnClickListener {
                showPopup(it, layout) // View + 대응되는 LinearLayout 넘김
            }
        }

        // 등록한 거래글 보기 하트버튼 선언
        val heartIcon = listOf(
            findViewById<ImageView>(R.id.heartIcon)
        )

        // 등록한 거래글 보기 하트버튼 클릭시 채워진 하트로 바뀜
        heartIcon.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.heartIcon, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.heartIcon) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_heart)
                } else {
                    button.setImageResource(R.drawable.ic_heart_fill)
                    Toast.makeText(this, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.heartIcon, !isLiked)
            }
        }
    }
    fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        val file = uriToFile(this, uri) ?: return
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = App.prefs.token ?: ""
        if (token.isEmpty()) {
            Log.e("Upload", "토큰 없음")
            callback(null)
            return
        }

        RetrofitInstance.apiService.uploadImage("Bearer $token", body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.string()
                        callback(imageUrl)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(null)
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
}




