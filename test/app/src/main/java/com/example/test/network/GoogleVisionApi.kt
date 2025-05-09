package com.example.test.network

import com.example.test.model.recipt.VisionRequest
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleVisionApi {
    @POST("v1/images:annotate")
    fun annotateImage(
        @Query("key") apiKey: String,
        @Body body: VisionRequest
    ): Call<JsonObject>
}
