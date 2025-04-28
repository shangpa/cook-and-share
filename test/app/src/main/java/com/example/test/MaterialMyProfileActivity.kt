package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.SaleHistoryAdapter
import com.example.test.model.TradePost.TpReviewResponseDTO
import com.example.test.model.TradePost.TradeItem
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.network.RetrofitInstance
import com.example.test.network.RetrofitInstance.BASE_URL
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Stack

var lastEnteredFrom: String? = null


class MaterialMyProfileActivity : AppCompatActivity() {
    
    private lateinit var saleHistoryAdapter: SaleHistoryAdapter //어댑터
    private var allTradeList = mutableListOf<TradeItem>()  // 전체 판매내역

    private var isMaterialVisible = false
    private var isdistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button> // 거리 버튼 리스트
    private lateinit var selectedFilterLayout: LinearLayout

    private var selectedMaterial: Button? = null
    private var selectedDistance: Button? = null

    private lateinit var profile: LinearLayout
    private lateinit var saleHistory: LinearLayout
    private lateinit var purchaseHistory: LinearLayout
    private lateinit var reviewContainer: ConstraintLayout
    private lateinit var savePost: ConstraintLayout
    private lateinit var review: LinearLayout

    private val viewStack = Stack<View>()


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_profile)
        val token = "Bearer ${App.prefs.token.toString()}"
        selectedFilterLayout = findViewById(R.id.selectedFilterLayout)
        loadReceivedReviews()
        loadWrittenReviews()
        profile = findViewById(R.id.profile)
        saleHistory = findViewById(R.id.saleHistory)
        purchaseHistory = findViewById(R.id.purchaseHistory)
        reviewContainer = findViewById(R.id.reviewContainer)
        savePost = findViewById(R.id.savePost)
        review = findViewById(R.id.review)

        // 프로필 선언
        val profileLayout = findViewById<LinearLayout>(R.id.profile)
        val saleIcon: ImageView = findViewById(R.id.ic_ticket)
        val saleText: TextView = findViewById(R.id.ic_ticket1)
        val saleArrow: ImageView = findViewById(R.id.ic_rigth1)
        val purchaseIcon = findViewById<ImageView>(R.id.ic_basket)
        val purchaseText = findViewById<TextView>(R.id.ic_basket1)
        val purchaseArrow = findViewById<ImageView>(R.id.ic_rigth2)
        val saveIcon = findViewById<ImageView>(R.id.ic_bookmark)
        val saveText = findViewById<TextView>(R.id.ic_bookmark1)
        val saveArrow = findViewById<ImageView>(R.id.ic_rigth3)
        val reviewIcon = findViewById<ImageView>(R.id.ic_chatt)
        val reviewText = findViewById<TextView>(R.id.ic_chatt1)
        val reviewArrow = findViewById<ImageView>(R.id.ic_rigth4)

        // 판매내역 선언
        val saleHistoryLayout = findViewById<LinearLayout>(R.id.saleHistory)
        val indicator = findViewById<View>(R.id.indicator)

        // 구매내역 선언
        val purchaseHistoryLayout = findViewById<LinearLayout>(R.id.purchaseHistory)
        val profileItemSeven = findViewById<LinearLayout>(R.id.profileItemSeven)

        // 후기 작성하기 선언
        val reviewItemMore = findViewById<ImageView>(R.id.reviewItemMore)
        val descriptionText = findViewById<EditText>(R.id.descriptionText)
        val postBtn = findViewById<Button>(R.id.postBtn)

        // 저장한 게시글 선언
        val savePostLayout = findViewById<ConstraintLayout>(R.id.savePost)
        val materialContainer = findViewById<LinearLayout>(R.id.materialContainer)
        val distanceContainer = findViewById<LinearLayout>(R.id.distanceContainer)
        val profileItemEight = findViewById<LinearLayout>(R.id.profileItemEight)
        val itemMore7 = findViewById<ImageView>(R.id.itemMore7)
        val reviewWrite = findViewById<LinearLayout>(R.id.reviewWrite)
        val itemMore8 = findViewById<ImageView>(R.id.itemMore8)
        val reviewWriteTwo = findViewById<LinearLayout>(R.id.reviewWriteTwo)
        val saveItemMore = findViewById<ImageView>(R.id.saveItemMore)
        val saveItemMore2 = findViewById<ImageView>(R.id.saveItemMore2)
        val saveItemMore3 = findViewById<ImageView>(R.id.saveItemMore3)
        val saveItemMore4 = findViewById<ImageView>(R.id.saveItemMore4)
        val saveItemMore5 = findViewById<ImageView>(R.id.saveItemMore5)
        val item1 = findViewById<LinearLayout>(R.id.item1)
        val item2 = findViewById<LinearLayout>(R.id.item2)
        val item3 = findViewById<LinearLayout>(R.id.item3)
        val item4 = findViewById<LinearLayout>(R.id.item4)
        val item5 = findViewById<LinearLayout>(R.id.item5)
        val materialText = findViewById<TextView>(R.id.materialText)
        val materialIcon = findViewById<ImageView>(R.id.materialIcon)
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon)
        val material = findViewById<LinearLayout>(R.id.material)
        val distance = findViewById<LinearLayout>(R.id.distance)
        val w = findViewById<TextView>(R.id.w)
        val sortArrow = findViewById<ImageView>(R.id.sortArrow)
        val mySalesBack = findViewById<ImageView>(R.id.mySalesBack)
        val myPurchaseBack = findViewById<ImageView>(R.id.myPurchaseBack)
        val reviewWriteBack = findViewById<ImageView>(R.id.reviewWriteBack)
        val savedTransactioneBack = findViewById<ImageView>(R.id.savedTransactioneBack)
        val receivedReviewBack = findViewById<ImageView>(R.id.receivedReviewBack)

        // 리뷰 선언
        val reviewLayout = findViewById<LinearLayout>(R.id.review)
        val receiveReview = findViewById<LinearLayout>(R.id.receiveReview)
        val profileItemNine = findViewById<LinearLayout>(R.id.profileItemNine)
        val profileItemTen = findViewById<LinearLayout>(R.id.profileItemTen)
        val profileItemEleven = findViewById<LinearLayout>(R.id.profileItemEleven)
        val profileItemTweleve = findViewById<LinearLayout>(R.id.profileItemTweleve)
        val indicatorTwo = findViewById<View>(R.id.indicatorTwo)
        val itemMore9 = findViewById<ImageView>(R.id.itemMore9)
        val itemMore10 = findViewById<ImageView>(R.id.itemMore10)
        val writtenReview = findViewById<LinearLayout>(R.id.writtenReview)
        val itemMore11 = findViewById<ImageView>(R.id.itemMore11)
        val itemMore12 = findViewById<ImageView>(R.id.itemMore12)

        // 신고하기 선언
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
        val cancelTwoBtn = findViewById<Button>(R.id.cancelTwoBtn)
        val registerBtn = findViewById<Button>(R.id.registerBtn)


        // saleHistoryAdapter를 먼저 빈 리스트로 초기화
        saleHistoryAdapter = SaleHistoryAdapter(emptyList(),token)

        // RecyclerView 연결
        val saleHistoryRecyclerView = findViewById<RecyclerView>(R.id.saleHistoryRecyclerView)
        saleHistoryRecyclerView.adapter = saleHistoryAdapter
        saleHistoryRecyclerView.layoutManager = LinearLayoutManager(this)

        val apiService = RetrofitInstance.apiService
        apiService.getMyTradePosts(token).enqueue(object : Callback<List<TradePostSimpleResponse>> {
            override fun onResponse(
                call: Call<List<TradePostSimpleResponse>>,
                response: Response<List<TradePostSimpleResponse>>
            ) {
                if (response.isSuccessful) {
                    val myPosts = response.body() ?: emptyList()

                    // 변환해서 RecyclerView에 넣기
                    allTradeList = myPosts.map {
                        TradeItem(
                            id = it.tradePostId,
                            title = it.title,
                            imageUrl = BASE_URL + it.firstImageUrl,
                            distance = "거리정보없음", // 거리 필요하면 추후 계산
                            date = it.createdAt,
                            price = "${it.price} P",
                            isCompleted = it.status == 1
                        )
                    }.toMutableList()

                    saleHistoryAdapter.updateList(allTradeList)
                } else {
                    Log.e("TradePost", "실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<TradePostSimpleResponse>>, t: Throwable) {
                Log.e("TradePost", "에러: ${t.message}")
            }
        })



        // 판매내역 클릭시 나의 동네재료 판매내역으로 화면 이동
        saleIcon.setOnClickListener {
            profileLayout.visibility = View.GONE
            saleHistoryLayout.visibility = View.VISIBLE
        }

        saleText.setOnClickListener {
            profileLayout.visibility = View.GONE
            saleHistoryLayout.visibility = View.VISIBLE
        }

        saleArrow.setOnClickListener {
            profileLayout.visibility = View.GONE
            saleHistoryLayout.visibility = View.VISIBLE
        }

        // 구매내역 클릭시 나의 동네재료 구매내역으로 화면 이동
        purchaseIcon.setOnClickListener {
            profileLayout.visibility = View.GONE
            purchaseHistoryLayout.visibility = View.VISIBLE
        }

        purchaseText.setOnClickListener {
            profileLayout.visibility = View.GONE
            purchaseHistoryLayout.visibility = View.VISIBLE
        }

        purchaseArrow.setOnClickListener {
            profileLayout.visibility = View.GONE
            purchaseHistoryLayout.visibility = View.VISIBLE
        }

        // 저장한 게시글 클릭시 나의 저장한 동네주방 거래글로 화면 이동
        saveIcon.setOnClickListener {
            profileLayout.visibility = View.GONE
            savePostLayout.visibility = View.VISIBLE
        }

        saveText.setOnClickListener {
            profileLayout.visibility = View.GONE
            savePostLayout.visibility = View.VISIBLE
        }

        saveArrow.setOnClickListener {
            profileLayout.visibility = View.GONE
            savePostLayout.visibility = View.VISIBLE
        }

        // 후기보기 클릭시 나의 동네재료 리뷰로 화면 이동
        reviewIcon.setOnClickListener {
            profileLayout.visibility = View.GONE
            reviewLayout.visibility = View.VISIBLE
        }

        reviewText.setOnClickListener {
            profileLayout.visibility = View.GONE
            reviewLayout.visibility = View.VISIBLE
        }

        reviewArrow.setOnClickListener {
            profileLayout.visibility = View.GONE
            reviewLayout.visibility = View.VISIBLE
        }

        // 판매내역 TextView 리스트
        val textViews = listOf(
            findViewById<TextView>(R.id.total),
            findViewById<TextView>(R.id.deal),
            findViewById<TextView>(R.id.dealComplete)
        )
        textViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                // 탭 색상 변경
                textViews.forEach { it.setTextColor(Color.parseColor("#B3B3B3")) }
                textView.setTextColor(Color.parseColor("#35A825"))

                // 인디케이터 이동
                val indicator = findViewById<View>(R.id.indicator)
                indicator.post {
                    val location = IntArray(2)
                    textView.getLocationOnScreen(location)
                    val textViewX = location[0]
                    indicator.translationX = textViewX.toFloat()
                }

                // 리스트 필터링
                when (index) {
                    0 -> saleHistoryAdapter.updateList(allTradeList)  // 전체
                    1 -> saleHistoryAdapter.updateList(allTradeList.filter { !it.isCompleted })  // 거래중
                    2 -> saleHistoryAdapter.updateList(allTradeList.filter { it.isCompleted })  // 거래완료
                }
            }
        }

        // 구매내역, 저장한 거래글 더하기 버튼 클릭시 신고하기 나타남
        val reportButtons = listOf(
            itemMore7,
            itemMore8,
            saveItemMore,
            saveItemMore2,
            saveItemMore3,
            saveItemMore4,
            saveItemMore5
        )
        val reportLayouts =
            listOf(profileItemSeven, profileItemEight, item1, item2, item3, item4, item5)

        fun showReportPopup(anchorView: View, targetLayout: LinearLayout) {
            val popup = PopupMenu(this, anchorView)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    // 신고 레이아웃 보이기
                    val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
                    recipeRegister.visibility = View.VISIBLE

                    // 등록 버튼 눌렀을 때 처리
                    val registerBtn = findViewById<Button>(R.id.registerBtn)
                    registerBtn.setOnClickListener {
                        recipeRegister.visibility = View.GONE  // 신고 레이아웃 숨기기
                        targetLayout.visibility = View.GONE   // 댓글 등 타겟 레이아웃 숨기기
                    }

                    true
                } else {
                    false
                }
            }

            popup.show()
        }

        // 신고하기 버튼 연결
        reportButtons.zip(reportLayouts).forEach { (button, layout) ->
            button.setOnClickListener {
                showReportPopup(it, layout)
            }
        }

        reviewWrite.setOnClickListener {
            lastEnteredFrom = "reviewWrite"
            purchaseHistoryLayout.visibility = View.GONE
            reviewContainer.visibility = View.VISIBLE
        }

        reviewWriteTwo.setOnClickListener {
            lastEnteredFrom = "reviewWriteTwo"
            purchaseHistoryLayout.visibility = View.GONE
            reviewContainer.visibility = View.VISIBLE
        }


        // 구매내역 후기 작성하기 더하기 버튼 클릭시 신고하기 나타남
        val reviewButtons = listOf(reviewItemMore)
        val reviewLayouts = listOf(reviewContainer)

        fun showReviewPopup(anchorView: View, targetLayout: ConstraintLayout) {
            val popup = PopupMenu(this, anchorView)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    // 신고 레이아웃 보여주기
                    val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
                    recipeRegister.visibility = View.VISIBLE

                    // 등록 버튼 클릭 처리
                    val registerBtn = findViewById<Button>(R.id.registerBtn)
                    registerBtn.setOnClickListener {
                        recipeRegister.visibility = View.GONE // 신고 레이아웃 숨기기

                        // profileItem 제거
                        when (lastEnteredFrom) {
                            "reviewWrite" -> profileItemSeven.visibility = View.GONE
                            "reviewWriteTwo" -> profileItemEight.visibility = View.GONE
                            "itemMore11" -> profileItemEleven.visibility = View.GONE
                            "itemMore12" -> profileItemTweleve.visibility = View.GONE
                        }

                        finish() // 현재 액티비티 종료
                    }

                    true
                } else {
                    false
                }
            }

            popup.show()
        }


        // 신고하기 버튼 연결
        reviewButtons.zip(reviewLayouts).forEach { (button, layout) ->
            button.setOnClickListener {
                showReviewPopup(it, layout)
            }
        }

        // 후기 작성하기 게시하기 버튼 클릭시
        postBtn.setOnClickListener {
            reviewContainer.visibility = View.GONE
            purchaseHistoryLayout.visibility = View.VISIBLE
        }

        // 저장한 게시글 재료 버튼들
        val all = findViewById<Button>(R.id.all)
        val cookware = findViewById<Button>(R.id.cookware)
        val fansPots = findViewById<Button>(R.id.fans_pots)
        val containers = findViewById<Button>(R.id.containers)
        val tableware = findViewById<Button>(R.id.tableware)
        val storage = findViewById<Button>(R.id.storageSupplies)
        val sanitary = findViewById<Button>(R.id.sanitaryProducts)
        val small = findViewById<Button>(R.id.smallAppliances)
        val disposable = findViewById<Button>(R.id.disposableProducts)
        val etc = findViewById<Button>(R.id.etc)

        val materialButtons = listOf(
            all, cookware, fansPots, containers, tableware,
            storage, sanitary, small, disposable, etc
        )

        materialButtons.forEach { button ->
            button.setOnClickListener {
                setSelectedMaterialButton(button, materialContainer, materialText)
                showSelectedFilterBadge(button.text.toString(), materialContainer, materialText)
                material.visibility = View.GONE
                isMaterialVisible = false
            }
        }

        // 거리 버튼 리스트 초기화
        buttons = listOf(
            findViewById(R.id.alll),
            findViewById(R.id.threeHundred),
            findViewById(R.id.fiveHundred),
            findViewById(R.id.oneThousand),
            findViewById(R.id.onefiveThousand),
            findViewById(R.id.twoThousand)
        )

        for (button in buttons) {
            button.setOnClickListener {
                setSelectedButton(button)
            }
        }

        // 재료 필터 클릭
        materialContainer.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            material.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            if (isMaterialVisible) {
                materialContainer.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                materialText.setTextColor(Color.WHITE)
                materialIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                if (selectedMaterial != null) {
                    materialContainer.setBackgroundColor(Color.BLACK)
                    materialText.setTextColor(Color.WHITE)
                } else {
                    materialContainer.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    materialText.setTextColor(Color.parseColor("#8A8F9C"))
                }
                materialIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        // 거리 필터 클릭
        distanceContainer.setOnClickListener {
            isdistanceVisible = !isdistanceVisible
            distance.visibility = if (isdistanceVisible) View.VISIBLE else View.GONE
            if (isdistanceVisible) {
                distanceContainer.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                distanceText.setTextColor(Color.WHITE)
                distanceIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                distanceContainer.setBackgroundResource(R.drawable.rounded_rectangle_background)
                distanceText.setTextColor(Color.parseColor("#8A8F9C"))
                distanceIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        val commentIcons = listOf(
            findViewById<ImageView>(R.id.commentIcon),
            findViewById<ImageView>(R.id.commentIcon2),
            findViewById<ImageView>(R.id.commentIcon3),
            findViewById<ImageView>(R.id.commentIcon5),
            findViewById<ImageView>(R.id.commentIcon6)
        )

        commentIcons.forEach { icon ->
            icon.setOnClickListener {
                val intent = Intent(this, MaterialChatDetailActivity::class.java)
                startActivity(intent)
            }
        }

        sortArrow.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("최신순")
            popup.menu.add("거리순")

            popup.setOnMenuItemClickListener { item ->
                w.text = item.title  // 선택한 텍스트를 TextView(w)에 표시
                true
            }

            popup.show()
        }

        // 리뷰 TextView 리스트
        val textViewd = listOf(
            findViewById<TextView>(R.id.receiveReviewTap),
            findViewById<TextView>(R.id.writeReviewTap)
        )

        // 리뷰 LinearLayout 리스트 (TextView와 1:1 매칭)
        val layoutd = listOf(
            findViewById<LinearLayout>(R.id.receiveReview),
            findViewById<LinearLayout>(R.id.writtenReview)
        )

        // 리뷰 TextView 클릭 시 해당 화면으로 이동 & 바 위치 변경
        textViewd.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                // 모든 ConstraintLayout 숨김
                layoutd.forEach { it.visibility = View.GONE }

                // 클릭된 TextView에 해당하는 ConstraintLayout만 표시
                layoutd[index].visibility = View.VISIBLE

                // 모든 TextView 색상 초기화
                textViewd.forEach { it.setTextColor(Color.parseColor("#B3B3B3")) }

                // 클릭된 TextView만 색상 변경 (#2B2B2B)
                textView.setTextColor(Color.parseColor("#35A825"))

                // indicatorTwo 클릭된 TextView 아래로 이동
                val params = indicatorTwo.layoutParams as ViewGroup.MarginLayoutParams
                indicatorTwo.post {
                    val location = IntArray(2)
                    textView.getLocationOnScreen(location)
                    val textViewX = location[0]

                    // 바 위치를 TextView의 x 좌표로 이동
                    indicatorTwo.translationX = textViewX.toFloat()
                }
            }
        }

        // 리뷰 더하기 버튼 클릭시 수정, 삭제 나타남
        val reviewButton = listOf(itemMore11, itemMore12)
        val reviewlayoutList = listOf(profileItemEleven, profileItemTweleve) // 삭제 대상 LinearLayout들

        val reviewPopupItems = listOf("수정", "삭제")

        fun reviewShowPopup(anchorView: View, targetLayout: LinearLayout) {
            val popup = PopupMenu(this, anchorView)
            val reviewPopupItems = listOf("수정", "삭제")

            reviewPopupItems.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "수정" -> {
                        reviewLayout.visibility = View.GONE
                        reviewContainer.visibility = View.VISIBLE
                        true
                    }

                    "삭제" -> {
                        // 삭제 클릭시 해당 레이아웃 없어짐
                        targetLayout.visibility = View.GONE
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }

        // 버튼과 레이아웃을 1:1 매칭
        reviewButton.zip(reviewlayoutList).forEach { (button, layout) ->
            button.setOnClickListener {
                reviewShowPopup(it, layout) // View + 대응되는 LinearLayout 넘김
            }
        }

        // 리뷰 더하기 버튼 클릭시 신고하기 나타남
        val reviewWriteButtons = listOf(itemMore9, itemMore10)
        val reviewWriteLayouts = listOf(profileItemNine, profileItemTen)

        fun showReviewWritePopup(anchorView: View, targetLayout: LinearLayout) {
            val popup = PopupMenu(this, anchorView)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    // 신고 레이아웃 보이기
                    val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
                    recipeRegister.visibility = View.VISIBLE

                    // 등록 버튼 눌렀을 때 처리
                    val registerBtn = findViewById<Button>(R.id.registerBtn)
                    registerBtn.setOnClickListener {
                        recipeRegister.visibility = View.GONE  // 신고 레이아웃 숨기기
                        targetLayout.visibility = View.GONE   // 댓글 등 타겟 레이아웃 숨기기
                    }
                    true
                } else {
                    false
                }
            }

            popup.show()
        }

        // 신고하기 버튼 연결
        reviewWriteButtons.zip(reviewWriteLayouts).forEach { (button, layout) ->
            button.setOnClickListener {
                showReviewWritePopup(it, layout)
            }
        }

        // 2. 첫 화면 설정 (예: profile만 보이게)
        showOnly(profile)

        findViewById<ImageView>(R.id.ic_ticket).setOnClickListener {
            showView(saleHistory)
        }
        findViewById<TextView>(R.id.ic_ticket1).setOnClickListener {
            showView(saleHistory)
        }
        findViewById<ImageView>(R.id.ic_rigth1).setOnClickListener {
            showView(saleHistory)
        }

        findViewById<ImageView>(R.id.ic_basket).setOnClickListener {
            showView(purchaseHistory)
        }
        findViewById<TextView>(R.id.ic_basket1).setOnClickListener {
            showView(purchaseHistory)
        }
        findViewById<ImageView>(R.id.ic_rigth2).setOnClickListener {
            showView(purchaseHistory)
        }

        findViewById<ImageView>(R.id.ic_bookmark).setOnClickListener {
            showView(savePost)
        }
        findViewById<TextView>(R.id.ic_bookmark1).setOnClickListener {
            showView(savePost)
        }
        findViewById<ImageView>(R.id.ic_rigth3).setOnClickListener {
            showView(savePost)
        }

        findViewById<ImageView>(R.id.ic_chatt).setOnClickListener {
            showView(review)
        }
        findViewById<TextView>(R.id.ic_chatt1).setOnClickListener {
            showView(review)
        }
        findViewById<ImageView>(R.id.ic_rigth4).setOnClickListener {
            showView(review)
        }

        // ✅ 뒤로가기 버튼 연결
        findViewById<ImageView>(R.id.mySalesBack).setOnClickListener { goBack() }
        findViewById<ImageView>(R.id.myPurchaseBack).setOnClickListener { goBack() }
        findViewById<ImageView>(R.id.reviewWriteBack).setOnClickListener { goBack() }
        findViewById<ImageView>(R.id.savedTransactioneBack).setOnClickListener { goBack() }
        findViewById<ImageView>(R.id.receivedReviewBack).setOnClickListener { goBack() }

        // 신고하기 선언
        findViewById<TextView>(R.id.report1).setOnClickListener { moveArrowTo(it as TextView) }
        findViewById<TextView>(R.id.report2).setOnClickListener { moveArrowTo(it as TextView) }
        findViewById<TextView>(R.id.report3).setOnClickListener { moveArrowTo(it as TextView) }
        findViewById<TextView>(R.id.report4).setOnClickListener { moveArrowTo(it as TextView) }
        findViewById<TextView>(R.id.report5).setOnClickListener { moveArrowTo(it as TextView) }

        // 신고하기 취소하기 선택시 회색 화면 없어짐
        cancelTwoBtn.setOnClickListener {
            recipeRegister.visibility = View.GONE
        }


    }

        private fun setSelectedButton(selectedButton: Button) {
            val distanceText = findViewById<TextView>(R.id.distanceText)
            val distanceContainer = findViewById<LinearLayout>(R.id.distanceContainer)
            val distanceLayout = findViewById<LinearLayout>(R.id.distance)

            // 버튼 스타일 설정 및 selectedDistance 지정
            for (button in buttons) {
                if (button == selectedButton) {
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                    button.setTextColor(Color.WHITE)
                    selectedDistance = button
                } else {
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    button.setTextColor(Color.parseColor("#8A8F9C"))
                }
            }

            // 기존 거리 뱃지 제거
            val childCount = selectedFilterLayout.childCount
            for (i in childCount - 1 downTo 0) {
                val badge = selectedFilterLayout.getChildAt(i)
                val badgeText = badge.findViewById<TextView>(R.id.filterText)
                if (badge.tag == "distance") {
                    selectedFilterLayout.removeView(badge)
                }
            }

            // 뱃지 추가
            val badge = layoutInflater.inflate(R.layout.filter_badge, null)
            badge.tag = "distance"
            val badgeText = badge.findViewById<TextView>(R.id.filterText)
            val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

            badgeText.text = selectedButton.text.toString()
            badgeClose.setOnClickListener {
                selectedFilterLayout.removeView(badge)
                selectedDistance = null

                // 거리 버튼 초기화
                buttons.forEach {
                    it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    it.setTextColor(Color.parseColor("#8A8F9C"))
                }

                // 거리 필터 UI 초기화
                distanceContainer.setBackgroundResource(R.drawable.rounded_rectangle_background)
                distanceText.setTextColor(Color.parseColor("#8A8F9C"))
            }

            selectedFilterLayout.addView(badge)

            // 거리 레이아웃 닫기
            distanceLayout.visibility = View.GONE
            isdistanceVisible = false

            // 선택된 상태로 필터 배경/글자색 설정
            distanceContainer.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            distanceText.setTextColor(Color.WHITE)
        }



        private fun setSelectedMaterialButton(button: Button, filterLayout: LinearLayout, textView: TextView) {
            selectedMaterial = if (selectedMaterial == button) null else button

            val allMaterialButtons = listOf(
                findViewById<Button>(R.id.all),
                findViewById(R.id.cookware),
                findViewById(R.id.fans_pots),
                findViewById(R.id.containers),
                findViewById(R.id.tableware),
                findViewById(R.id.storageSupplies),
                findViewById(R.id.sanitaryProducts),
                findViewById(R.id.smallAppliances),
                findViewById(R.id.disposableProducts),
                findViewById(R.id.etc)
            )

            allMaterialButtons.forEach {
                it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                it.setTextColor(Color.parseColor("#8A8F9C"))
            }

            selectedMaterial?.let {
                it.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                it.setTextColor(Color.WHITE)
            }
        }

        private fun showSelectedFilterBadge(text: String, materialFilter: LinearLayout, materialText: TextView) {
            selectedFilterLayout.visibility = View.VISIBLE

            val badge = layoutInflater.inflate(R.layout.filter_badge, null)
            val badgeText = badge.findViewById<TextView>(R.id.filterText)
            val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

            badgeText.text = text
            badge.tag = "material-$text" // 고유 태그 설정

            badgeClose.setOnClickListener {
                selectedFilterLayout.removeView(badge)

                // 버튼 초기화
                val allMaterialButtons = listOf(
                    findViewById<Button>(R.id.all),
                    findViewById(R.id.cookware),
                    findViewById(R.id.fans_pots),
                    findViewById(R.id.containers),
                    findViewById(R.id.tableware),
                    findViewById(R.id.storageSupplies),
                    findViewById(R.id.sanitaryProducts),
                    findViewById(R.id.smallAppliances),
                    findViewById(R.id.disposableProducts),
                    findViewById(R.id.etc)
                )

                allMaterialButtons.find { it.text == text }?.let { button ->
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    button.setTextColor(Color.parseColor("#8A8F9C"))
                }

                // 재료 필터 색 초기화 (선택된 게 없을 때만)
                if (selectedFilterLayout.children.none { it.tag.toString().startsWith("material-") }) {
                    materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    materialText.setTextColor(Color.parseColor("#8A8F9C"))
                }
            }

            selectedFilterLayout.addView(badge)
        }

    // 현재 보여지는 화면 숨기고, 새 화면 보여주기 + 이전 화면 스택에 저장
    private fun showView(nextView: View) {
        val currentView = getCurrentVisibleView()
        currentView?.let {
            it.visibility = View.GONE
            viewStack.push(it)
        }
        nextView.visibility = View.VISIBLE
    }

    // 현재 보이는 화면 찾기
    private fun getCurrentVisibleView(): View? {
        val allViews = listOf(profile, saleHistory, reviewContainer, savePost, review)
        return allViews.find { it.visibility == View.VISIBLE }
    }

    // 뒤로가기: 현재 화면 숨기고, 스택에서 이전 화면 복원
    private fun goBack() {
        val currentView = getCurrentVisibleView()
        currentView?.visibility = View.GONE

        if (viewStack.isNotEmpty()) {
            val previousView = viewStack.pop()
            previousView.visibility = View.VISIBLE
        } else {
            finish() // 스택이 비어있으면 Activity 종료
        }
    }

    // 초기화: 하나만 VISIBLE, 나머지는 GONE
    private fun showOnly(target: View) {
        val allViews = listOf(profile, saleHistory, purchaseHistory, reviewContainer, savePost, review)
        for (view in allViews) {
            view.visibility = if (view == target) View.VISIBLE else View.GONE
        }
    }

    // 신고하기 클릭시 나타남
    private fun moveArrowTo(selectedTextView: TextView) {
        val arrow = findViewById<ImageView>(R.id.registerArrow)

        // 텍스트 색 초기화 후 선택된 항목만 초록색
        resetAllTextColors()
        selectedTextView.setTextColor(Color.parseColor("#35A825"))

        // ConstraintLayout Params 변경
        val params = arrow.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = selectedTextView.id
        params.bottomToBottom = selectedTextView.id
        arrow.layoutParams = params
        arrow.visibility = View.VISIBLE
    }

    private fun resetAllTextColors() {
        val textIds = listOf(
            R.id.report1, R.id.report2, R.id.report3, R.id.report4, R.id.report5
        )
        textIds.forEach {
            findViewById<TextView>(it).setTextColor(Color.parseColor("#000000"))
        }
    }
    private fun loadReceivedReviews() {
        val token = App.prefs.token.toString()

        RetrofitInstance.apiService.getReviewsOnMyTradePosts("Bearer $token")
            .enqueue(object : retrofit2.Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: retrofit2.Call<List<TpReviewResponseDTO>>,
                    response: retrofit2.Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reviews ->
                            addReviewsToLayout(reviews, findViewById(R.id.receiveReview))
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MaterialMyProfile", "받은 거래 후기 불러오기 실패: ${t.message}")
                }
            })
    }

    private fun loadWrittenReviews() {
        val token = App.prefs.token.toString()

        RetrofitInstance.apiService.getMyTpReviews("Bearer $token")
            .enqueue(object : retrofit2.Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: retrofit2.Call<List<TpReviewResponseDTO>>,
                    response: retrofit2.Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reviews ->
                            addReviewsToLayout(reviews, findViewById(R.id.writtenReview))
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MaterialMyProfile", "작성한 거래 후기 불러오기 실패: ${t.message}")
                }
            })
    }
    private fun addReviewsToLayout(reviews: List<TpReviewResponseDTO>, parentLayout: LinearLayout) {
        parentLayout.removeAllViews() // 기존 뷰 초기화

        for (review in reviews) {
            val reviewView = LayoutInflater.from(this).inflate(R.layout.item_trade_review, parentLayout, false)

            // 뷰 아이디 찾아서 데이터 넣기
            val itemImage = reviewView.findViewById<ImageView>(R.id.itemImage) // 기본 썸네일
            val itemTitle = reviewView.findViewById<TextView>(R.id.itemTitle)
            val itemSubTitle = reviewView.findViewById<TextView>(R.id.itemSubTitle)
            val reviewRating = reviewView.findViewById<TextView>(R.id.reviewRating)
            val reviewDate = reviewView.findViewById<TextView>(R.id.reviewDate)
            val buyerName = reviewView.findViewById<TextView>(R.id.buyerName)
            val buyerRole = reviewView.findViewById<TextView>(R.id.buyerRole)
            val reviewContent = reviewView.findViewById<TextView>(R.id.reviewContent)

            // 데이터 바인딩
            itemTitle.text = "거래한 재료" // 서버에서 제목 안오니까 고정값
            itemSubTitle.text = "거래 아이템 설명" // 이것도 나중에 추가 가능
            reviewRating.text = "${review.rating}.0 |"
            reviewDate.text = review.createdAt.substring(0, 10) // yyyy-MM-dd
            buyerName.text = review.username
            buyerRole.text = " | 구매자" // 고정
            reviewContent.text = review.content

            parentLayout.addView(reviewView)
        }
    }
}



