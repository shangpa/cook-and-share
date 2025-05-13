package com.example.test.network

import com.example.test.model.*
import com.example.test.model.Fridge.FridgeRecommendRequest
import com.example.test.model.Fridge.FridgeRecommendResponse
import com.example.test.model.TradePost.TpReviewResponseDTO
import com.example.test.model.TradePost.TradePostRequest
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.TradePost.TradePostSimpleResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.model.review.ReviewRequestDTO
import com.example.test.model.review.ReviewResponseDTO
import com.google.gson.JsonObject
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

    // 레시피 탭
    @GET("api/recipes/public")
    fun getAllPublicRecipes(
        @Query("sort") sort: String? = null
    ): Call<List<Recipe>>

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
        @Query("title") title: String,
        @Query("sort") sort: String? = null
    ): Call<List<Recipe>>

    // 인기 검색어
    @GET("api/search/popular-keywords")
    fun getPopularKeywords(): Call<List<String>>

    // 검색어 저장
    @POST("/api/search/save")
    fun saveSearchKeyword(@Query("keyword") keyword: String): Call<Void>

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

    //동네재료 게시글
    @POST("/api/trade-posts")
    fun createTradePost(
        @Header("Authorization") token: String,
        @Body request: TradePostRequest
    ): Call<TradePostResponse>

    //동네재료 메인 거래글 조회
    @GET("/api/trade-posts")
    fun getAllTradePosts(@Header("Authorization") token: String? = null
    ): Call<List<TradePostResponse>>

    //동네재료 메인 카테고리 필터링
    @GET("/api/trade-posts/category")
    fun getTradePostsByCategory(
        @Query("category") category: String
    ): Call<List<TradePostResponse>>

    // 동네재료 거래글 검색 (키워드로 검색)
    @GET("/api/trade-posts/search")
    fun searchTradePosts(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String
    ): Call<List<TradePostResponse>>

    // 내가 쓴 거래 후기 리스트
    @GET("/api/tp-reviews/my-reviews")
    fun getMyTpReviews(
        @Header("Authorization") token: String
    ): Call<List<TpReviewResponseDTO>>

    // 내 거래글에 달린 거래 후기 리스트
    @GET("/api/tp-reviews/reviews-on-my-posts")
    fun getReviewsOnMyTradePosts(
        @Header("Authorization") token: String
    ): Call<List<TpReviewResponseDTO>>

    //거래글조회
    @GET("api/trade-posts/{tradePostId}")
    fun getTradePostById(
        @Header("Authorization") token: String,
        @Path("tradePostId") tradePostId: Long
    ): Call<TradePostResponse>

    //내가쓴 거래글 조회
    @GET("/api/trade-posts/my-posts")
    fun getMyTradePosts(
        @Header("Authorization") token: String
    ): Call<List<TradePostSimpleResponse>>

    //거래완료
    @PATCH("/api/trade-posts/{id}/complete")
    fun completeTradePost(
        @Header("Authorization") token: String,
        @Path("id") tradePostId: Long
    ): Call<TradePostSimpleResponse>
    
    //리뷰 작성
    @POST("/api/reviews")
    fun submitReview(
        @Header("Authorization") token: String,
        @Body request: ReviewRequestDTO
    ): Call<ReviewResponseDTO>

    //리뷰조회
    @GET("api/reviews/{recipeId}")
    fun getReviews(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Long
    ): Call<List<ReviewResponseDTO>>

    // 레시피 좋아요 토글 (좋아요 또는 취소)
    @POST("api/recipes/{recipeId}/like-toggle")
    fun toggleLike(
        @Path("recipeId") recipeId: Long,
        @Header("Authorization") token: String
    ): Call<ResponseBody>

    // 냉장고 재료 기반 레시피 추천
    @POST("/api/fridge/recommend")
    suspend fun recommendRecipes(
        @Header("Authorization") token: String,
        @Body request: FridgeRecommendRequest
    ): Response<List<FridgeRecommendResponse>>

    //포인트조회
    @GET("/api/point/my-point")
    fun getMyPoint(
        @Header("Authorization") token: String
    ): Call<Int>
}
