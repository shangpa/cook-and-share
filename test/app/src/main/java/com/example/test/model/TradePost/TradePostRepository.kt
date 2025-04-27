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
                    callback(response.body())
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

    //거래글 불러오기
    fun getAllTradePosts(token: String?, callback: (List<TradePostResponse>?) -> Unit) {
        val call = if (token != null) {
            RetrofitInstance.apiService.getAllTradePosts("Bearer $token")
        } else {
            RetrofitInstance.apiService.getAllTradePosts()
        }
        call.enqueue(object : Callback<List<TradePostResponse>> {
            override fun onResponse(
                call: Call<List<TradePostResponse>>,
                response: Response<List<TradePostResponse>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    println("거래글 불러오기 실패: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                println("네트워크 오류: ${t.message}")
                callback(null)
            }
        })
    }

    //거래글 카테고리 필터링
    fun getTradePostsByCategory(category: String, callback: (List<TradePostResponse>?) -> Unit) {
        val call = RetrofitInstance.apiService.getTradePostsByCategory(category)
        call.enqueue(object : Callback<List<TradePostResponse>> {
            override fun onResponse(call: Call<List<TradePostResponse>>, response: Response<List<TradePostResponse>>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    println("카테고리 거래글 불러오기 실패: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                println("네트워크 오류: ${t.message}")
                callback(null)
            }
        })
    }

}
