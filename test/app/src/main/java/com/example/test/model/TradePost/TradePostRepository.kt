package com.example.test.model.TradePost

import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TradePostRepository {

    fun uploadTradePost(token: String, request: TradePostRequest, callback: (TradePostResponse?) -> Unit) {
        val call = RetrofitInstance.apiService.createTradePost("Bearer $token", request)
        call.enqueue(object : Callback<TradePostResponse> {
            override fun onResponse(call: Call<TradePostResponse>, response: Response<TradePostResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()) // 성공 시 콜백 호출
                } else {
                    println("거래글 업로드 실패: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<TradePostResponse>, t: Throwable) {
                println("네트워크 오류: ${t.message}")
                callback(null)
            }
        })
    }
}