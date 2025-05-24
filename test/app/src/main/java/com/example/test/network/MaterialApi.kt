package com.example.test.network

import com.example.test.model.TradePost.TradePostResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MaterialApi {

    @POST("/api/tradeposts/{postId}/save-toggle")
    fun toggleSavedTradePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<ResponseBody>

    @GET("/api/tradeposts/{postId}/saved")
    fun isTradePostSaved(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<Boolean>

    @GET("/api/tradeposts/saved")
    fun getSavedTradePostIds(
        @Header("Authorization") token: String
    ): Call<List<Long>>
}