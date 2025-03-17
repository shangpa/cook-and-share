package com.example.test.network

import com.example.test.model.ApiResponse
import com.example.test.model.FridgeRequest
import com.example.test.model.FridgeResponse
import com.example.test.model.LoginInfoResponse
import com.example.test.model.LoginRequest
import com.example.test.model.LoginResponse
import com.example.test.model.RecipeRequest
import com.example.test.model.RecipeResponse
import com.example.test.model.SignUpRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.Response

interface ApiService {

    // 로그인 요청
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // 회원가입 요청
    @POST("join")
    fun signUp(@Body signUpRequest: SignUpRequest): Call<ApiResponse>

    @GET("user/info")
    fun getUserInfo(@Header("Authorization") token: String): Call<LoginInfoResponse>

    // 레시피 작성 요청 (토큰 필요)
    @POST("api/recipes")
    fun createRecipe(
        @Header("Authorization") token: String,
        @Body recipeRequest: RecipeRequest
    ): Call<RecipeResponse>

    @Multipart
    @POST("api/upload-image")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    // 냉장고 재료 저장
    @POST("api/fridges")
    suspend fun createFridge(
        @Header("Authorization") token: String,
        @Body fridgeRequest: FridgeRequest
    ): Response<FridgeResponse>

    // 냉장고 재료 조회
    @GET("api/fridges/my")
    suspend fun getMyFridges(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long
    ): Response<List<FridgeResponse>>
}
