package com.example.test

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.PurchaseHistoryAdapter
import com.example.test.model.TradePost.TradeItem
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialPurchaseActivity : AppCompatActivity() {
    private lateinit var adapter: PurchaseHistoryAdapter
    private var allTradeList = mutableListOf<TradeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maerial_purchase)

        val token = "Bearer ${App.prefs.token}"

        adapter = PurchaseHistoryAdapter(emptyList())
        findViewById<RecyclerView>(R.id.purchaseHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MaterialPurchaseActivity)
            this.adapter = this@MaterialPurchaseActivity.adapter
        }

        RetrofitInstance.materialApi.getMyPurchasedPosts(token)
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
                                imageUrl = RetrofitInstance.BASE_URL + it.firstImageUrl,
                                distance = "거리정보없음",
                                date = it.createdAt,
                                price = "${it.price} P",
                                isCompleted = it.status == 1
                            )
                        }?.toMutableList() ?: mutableListOf()

                        adapter.updateList(allTradeList)
                    }
                }

                override fun onFailure(call: Call<List<TradePostSimpleResponse>>, t: Throwable) {
                    Log.e("Purchase", "구매내역 불러오기 실패: ${t.message}")
                }
            })

        findViewById<ImageView>(R.id.myPurchaseBack).setOnClickListener {
            finish()
        }
    }
}
