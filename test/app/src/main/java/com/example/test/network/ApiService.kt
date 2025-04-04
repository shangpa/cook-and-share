package com.example.test.network

import com.example.test.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import com.example.test.model.recipeDetail.RecipeDetailResponse
import retrofit2.http.Path

interface ApiService {

    // 로그인 요청
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // 회원가입 요청
    @POST("join")
    fun signUp(@Body signUpRequest: SignUpRequest): Call<ApiResponse>

    // 사용자 정보 조회
    @GET("user/info")
    fun getUserInfo(@Header("Authorization") token: String): Call<LoginInfoResponse>

    // 레시피 작성 요청 (토큰 필요)
    @POST("api/recipes")
    fun createRecipe(
        @Header("Authorization") token: String,
        @Body recipeRequest: RecipeRequest
    ): Call<RecipeResponse>

    // 레시피 조회
    @GET("api/recipes/{id}")
    fun getRecipeById(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Long
    ): Call<RecipeDetailResponse>

    // 레시피 검색
    @GET("api/recipes/search")
    fun searchRecipes(
        @Query("title") title: String?
    ): Call<List<Recipe>>

    // 이미지 업로드
    @Multipart
    @POST("api/upload-image")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>
    
    //동영상 업로드
    @Multipart
    @POST("/api/upload-video")
    fun uploadVideo(
        @Part video: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Call<ResponseBody>
    
    // 냉장고 재료 추가
    @POST("api/fridges")
    suspend fun createFridge(
        @Header("Authorization") token: String,
        @Body fridgeRequest: FridgeRequest
    ): Response<FridgeResponse>

    // 냉장고 재료 조회
    @GET("api/fridges/my")
    suspend fun getMyFridges(
        @Header("Authorization") token: String
    ): Response<List<FridgeResponse>>

    // 냉장고 재료 수정
    @PUT("api/fridges/{id}")
    suspend fun updateFridge(
        @Path("id") fridgeId: Long,
        @Header("Authorization") token: String,
        @Body fridgeRequest: FridgeRequest
    ): Response<Void>

    // 냉장고 재료 삭제
    @DELETE("api/fridges/{id}")
    suspend fun deleteFridge(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Void>
}
