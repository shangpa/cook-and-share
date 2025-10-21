package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.SaleHistoryAdapter
import com.example.test.model.TradePost.TradeItem
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.network.RetrofitInstance
import com.example.test.network.RetrofitInstance.BASE_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialMyProfileSaleActivity : AppCompatActivity() {

    private lateinit var saleHistoryAdapter: SaleHistoryAdapter
    private var allTradeList = mutableListOf<TradeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_sale)

        val token = "Bearer ${App.prefs.token}"

        // 어댑터 초기화
        saleHistoryAdapter = SaleHistoryAdapter(emptyList(), token)
        val saleHistoryRecyclerView = findViewById<RecyclerView>(R.id.saleHistoryRecyclerView)
        saleHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        saleHistoryRecyclerView.adapter = saleHistoryAdapter

        // API 호출
        RetrofitInstance.apiService.getMyTradePosts(token)
            .enqueue(object : Callback<List<TradePostSimpleResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostSimpleResponse>>,
                    response: Response<List<TradePostSimpleResponse>>
                ) {
                    if (response.isSuccessful) {
                        allTradeList = response.body()?.map {
                            TradeItem(
                                id = it.tradePostId,
                                title = it.title,
                                imageUrl = BASE_URL + it.firstImageUrl,
                                distance = "800m",
                                date = it.createdAt,
                                price = "${it.price} P",
                                isCompleted = it.status == 1
                            )
                        }?.toMutableList() ?: mutableListOf()

                        saleHistoryAdapter.updateList(allTradeList)
                    }
                }

                override fun onFailure(call: Call<List<TradePostSimpleResponse>>, t: Throwable) {
                    Log.e("SaleActivity", "거래글 불러오기 실패: ${t.message}")
                }
            })

        // 탭 필터 처리
        val tabTexts = listOf(
            findViewById<TextView>(R.id.total),
            findViewById<TextView>(R.id.deal),
            findViewById<TextView>(R.id.dealComplete)
        )
        val indicator = findViewById<View>(R.id.indicator)

        tabTexts.forEachIndexed { index, tab ->
            tab.setOnClickListener {
                tabTexts.forEach { it.setTextColor(Color.parseColor("#B3B3B3")) }
                tab.setTextColor(Color.parseColor("#35A825"))

                // 리스트 필터
                val filtered = when (index) {
                    0 -> allTradeList
                    1 -> allTradeList.filter { !it.isCompleted }
                    2 -> allTradeList.filter { it.isCompleted }
                    else -> allTradeList
                }
                saleHistoryAdapter.updateList(filtered)

                // indicator 이동
                indicator.post {
                    val location = IntArray(2)
                    tab.getLocationOnScreen(location)
                    indicator.translationX = location[0].toFloat()
                }
            }
        }

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.mySalesBack).setOnClickListener {
            finish()
        }
    }
}
