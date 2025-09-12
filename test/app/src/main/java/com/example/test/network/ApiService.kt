package com.example.test.network

import com.example.test.FollowUserResponse
import com.example.test.model.*
import com.example.test.model.Fridge.FridgeCreateRequest
import com.example.test.model.Fridge.FridgeHistoryResponse
import com.example.test.model.Fridge.FridgeRecommendRequest
import com.example.test.model.Fridge.FridgeRecommendResponse
import com.example.test.model.Fridge.FridgeRequest
import com.example.test.model.Fridge.FridgeResponse
import com.example.test.model.Fridge.FridgeStatsResponse
import com.example.test.model.Fridge.UsedIngredientRequest
import com.example.test.model.TradePost.TradePostRequest
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.model.TradePost.TradePostUpResult
import com.example.test.model.TradePost.TradeUserResponse
import com.example.test.model.community.CommunityDetailResponse
import com.example.test.model.profile.ProfileSummaryResponse
import com.example.test.model.recipeDetail.ExpectedIngredient
import com.example.test.model.recipeDetail.MyWriteRecipeResponse
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.model.review.ReviewRequestDTO
import com.example.test.model.review.ReviewResponseDTO
import com.example.test.model.recipeDetail.RecipeMainSearchResponseDTO
import com.example.test.model.recipeDetail.ThumbnailResponse
import com.example.test.model.review.TpReviewResponseDTO
import com.example.test.model.shorts.CommentRequestDTO
import com.example.test.model.shorts.ShortCommentResponse
import com.example.test.model.shorts.ShortVideoListResponse
import com.example.test.model.shorts.ShortsCardDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

    // 로그인 요청
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // 회원가입 요청
    @POST("join")
    fun signUp(@Body signUpRequest: SignUpRequest): Call<ApiResponse>

    // 사용자 정보 조회
    @GET("api/user/profile")
    fun getUserInfo(@Header("Authorization") token: String): Call<LoginInfoResponse>

    //메인 - 냉장고 재료
    @GET("/api/main")
    fun getMainMessage(
        @Header("Authorization") token: String
    ): Call<Map<String, String>>

    //메인 - 냉장고 재료 추천 레시피
    @GET("/api/recipes/recommend-by-title")
    fun recommendByTitle(
        @Query("ingredients") ingredients: List<String>,
        @Header("Authorization") token: String
    ): Call<List<RecipeMainSearchResponseDTO>>

    @POST("/api/recipes/recommend-grouped")
    fun recommendGrouped(
        @Body ingredients: List<String>,
        @Header("Authorization") token: String
    ): Call<List<IngredientRecipeGroup>>

    //메인 - 동네주방 HOT 거래글
    @GET("api/trade-posts/popular")
    fun getPopularTradePosts(
        @Header("Authorization") token: String
    ): Call<List<TradePostSimpleResponse>>

    //메인 - 레시피 조회
    @GET("/api/recipes/like/list")
    fun getMainLikedRecipes(
        @Header("Authorization") token: String
    ): Call<List<RecipeMainSearchResponseDTO>>

    // 메인 - 조회수 TOP 레시피
    @GET("/api/recipes/top/view")
    fun getTopViewedRecipes(): Call<List<RecipeMainSearchResponseDTO>>

    @POST("/api/recipes/{recipeId}/like-toggle")
    fun toggleLikeMainRecipe(
        @Path("recipeId") recipeId: Long,
        @Header("Authorization") token: String
    ): Call<String>

    // 레시피 탭
    @GET("api/recipes/public")
    fun getAllPublicRecipes(
        @Header("Authorization") token: String,
        @Query("sort") sort: String? = null
    ): Call<List<Recipe>>

    // 레시피 탭 - 이거 어때요?
    @GET("/api/recipes/suggest")
    fun suggestRecipes(
        @Header("Authorization") token: String,
        @Query("type") type: String
    ): Call<List<Recipe>>

    // 레시피 탭 - 1분 레시피(랜덤 3개)
    @GET("/api/shorts/random3")
    fun getShortsRandom3(
        @Header("Authorization") bearer: String? = null
    ): Call<List<ShortsCardDto>>

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


    //레시피 - 예상 사용 재료
    @GET("api/recipes/{id}/expected-ingredients")
    fun getExpectedIngredients(
        @Path("id") recipeId: Long,
        @Header("Authorization") token: String
    ): Call<List<ExpectedIngredient>>

    @POST("api/fridges/use-ingredients")
    fun useIngredients(
        @Header("Authorization") token: String,
        @Body ingredients: List<UsedIngredientRequest>
    ): Call<Void>


    // 레시피 검색
    @GET("api/recipes/search")
    fun searchRecipes(
        @Header("Authorization") token: String,
        @Query("title") title: String,
        @Query("sort") sort: String? = null
    ): Call<List<Recipe>>

    // 제철 음식 추천
    @GET("/api/recipes/seasonal")
    fun getSeasonalRecipes(): Call<List<Recipe>>

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
    @DELETE("api/fridges/delete-by-name")
    fun deleteFridgeByName(
        @Query("ingredientName") ingredientName: String,
        @Header("Authorization") token: String
    ): Call<Void>

    // 냉장고 재료 이력 조회
    @GET("/api/fridges/history")
    fun getFridgeHistory(
        @Header("Authorization") token: String,
        @Query("ingredientName") ingredientName: String
    ): Call<List<FridgeHistoryResponse>>

    // 영수증으로 재료추가
    @POST("/api/fridges/ocr")
    fun createFridgeByOCR(
        @Body body: FridgeCreateRequest,
        @Header("Authorization") token: String
    ): Call<Void>

    // 냉장고 통계
    @GET("/api/fridges/stats/my")
    suspend fun getFridgeStats(
        @Header("Authorization") token: String
    ): Response<FridgeStatsResponse>

    @POST("/api/fridges/ocr/batch")
    fun createFridgesByOCRBatch(
        @Body body: List<FridgeCreateRequest>,
        @Header("Authorization") token: String
    ): Call<Void>

    //동네재료 게시글
    @POST("/api/trade-posts")
    fun createTradePost(
        @Header("Authorization") token: String,
        @Body request: TradePostRequest
    ): Call<TradePostResponse>

    //동네재료 끌어올리기
    @POST("api/trade-posts/{id}/up")
    fun upTradePost(
        @Header("Authorization") token: String,
        @Path("id") postId: Long
    ): Call<TradePostUpResult>

    //동네재료 메인 거래글 조회
    @GET("/api/trade-posts")
    fun getAllTradePosts(@Header("Authorization") token: String? = null
    ): Call<List<TradePostResponse>>

    //사용자 - 거래글 거리 계산
    @GET("/api/user/location")
    fun getUserLocation(
        @Header("Authorization") token: String
    ): Call<TradeUserResponse>

    //동네재료 메인 카테고리 필터링
    @GET("/api/trade-posts/category")
    fun getTradePostsByCategory(
        @Query("category") category: String
    ): Call<List<TradePostResponse>>

    // 동네재료 카테고리 필터링 통합: 거리/카테고리/정렬 모두 선택적
    @GET("/api/trade-posts/nearby")
    fun getNearbyFlexible(
        @Header("Authorization") token: String,
        @Query("distanceKm") distanceKm: Double? = null,          // null이면 거리 필터 미적용
        @Query("categories") categories: List<String>? = null,    // null/빈리스트면 카테고리 미적용
        @Query("sort") sort: String? = "LATEST"                   // LATEST | UPDATED | DISTANCE | PRICE | PURCHASE_DATE
    ): Call<List<TradePostResponse>>

    //동네재료 사용자 위치 저장
    @POST("/api/user/location")
    fun saveUserLocation(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<Void>

    //동네재료 카테고리 + 거리 다중 필터링
    @GET("/api/trade-posts/nearby-by-multiple-categories")
    fun getNearbyTradePostsByMultipleCategories(
        @Header("Authorization") token: String,
        @Query("distanceKm") distanceKm: Double,
        @Query("categories") categories: List<String>
    ): Call<List<TradePostResponse>>

    @POST("trade-posts/filter/categories")
    fun getTradePostsByMultipleCategories(
        @Header("Authorization") token: String,
        @Body categories: List<String>
    ): Call<List<TradePostResponse>>

    //카테고리 + 거리순
    @GET("/api/trade-posts/nearby/filter")
    fun getNearbyTradePostsByCategory(
        @Header("Authorization") token: String,
        @Query("distanceKm") distanceKm: Double,
        @Query("category") category: String
    ): Call<List<TradePostResponse>>

    // 동네재료 거래글 검색 (키워드로 검색)
    @GET("/api/trade-posts/search")
    fun searchTradePosts(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String
    ): Call<List<TradePostResponse>>

    //거래글 조회수
    @PATCH("api/trade-posts/{id}/view")
    fun increaseViewCount(
        @Path("id") postId: Long,
        @Header("Authorization") token: String
    ): Call<Void>

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

    // 레시피 좋아요 눌렀는지 확인
    @GET("api/recipes/{recipeId}/liked")
    fun isRecipeLiked(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Long
    ): Call<Boolean>

    //추천 토글
    @POST("api/recipes/{recipeId}/recommend-toggle")
    fun toggleRecommend(
        @Path("recipeId") recipeId: Long,
        @Header("Authorization") token: String
    ): Call<ResponseBody>

    //추천 여부 확인
    @GET("api/recipes/{recipeId}/recommended")
    fun isRecipeRecommended(
        @Path("recipeId") recipeId: Long,
        @Header("Authorization") token: String
    ): Call<Boolean>


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

    //개인정보수정
    @PUT("api/user/update")
    fun updateUserInfo(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Call<Void>

    @POST("/api/user/check-password")
    fun checkPassword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Boolean>

    //마이페이지 - 찜한 레시피
    @GET("api/recipes/likes")
    fun getLikedRecipes(
        @Header("Authorization") token: String
    ): Call<List<RecipeDetailResponse>>

    @GET("user/recipes")
    fun getMyRecipes(
        @Header("Authorization") token: String,
        @Query("sort") sort: String,
        @Query("categories") categories: List<String>,
        @Query("userId") userId: Int? = null   //  없으면 내 레시피, 있으면 해당 유저 레시피
    ): Call<MyWriteRecipeResponse>

    //마이페이지 - 작성한 레시피 삭제
    @DELETE("api/recipes/{id}")
    fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): Call<Void>

    //마이페이지 - 작성한 게시글
    @GET("api/boards/mine")
    fun getMyPosts(@Header("Authorization") token: String
    ): Call<List<CommunityDetailResponse>>

    @DELETE("community/{id}")
    fun deletePost(
        @Path("id") postId: Long,
        @Header("Authorization") token: String
    ): Call<Void>

    //마이페이지 - 리뷰 내역
    @GET("api/reviews/mypage")
    fun getMyReviews(
        @Header("Authorization") token: String
    ): Call<List<ReviewResponseDTO>>

    //마이페이지 - 리뷰 삭제
    @DELETE("api/reviews/{reviewId}")
    fun deleteReview(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: Long
    ): Call<Void>

    //마이페이지 - 리뷰 카테고리
    @GET("api/reviews/mypage/filter")
    fun getMyReviewsByCategory(
        @Header("Authorization") token: String,
        @Query("category") category: String
    ): Call<List<ReviewResponseDTO>>

    //마이페이지 - 냉장고 재료 관리
    @GET("/api/fridge-history/all")
    fun getAllFridgeHistories(
        @Header("Authorization") token: String
    ): Call<List<FridgeHistoryResponse>>

    @GET("/api/user/id")
    fun getUserIdByUsername(
        @Header("Authorization") token: String,
        @Query("username") username: String
    ): Call<Long>

    // 마이페이지 - 포인트 사용내역
    @GET("/api/point/my-history")
    fun getMyPointHistory(
        @Header("Authorization") token: String
    ): Call<List<PointHistoryResponse>>

    @Multipart
    @POST("/api/vision/analyze")
    fun detectAndSaveIngredients(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<List<String>>

    @POST("/api/auth/google-login")
    fun googleLogin(@Body request: GoogleLoginRequest): Call<LoginResponse>

    /*//쇼츠만 영상 업로드 안쓰는중
    @Multipart
    @POST("/api/shorts/upload-file")
    fun uploadShortsFileOnly(
        @Part video: MultipartBody.Part,
        @Header("Authorization") bearer: String
    ): Call<ResponseBody>*/

    //쇼츠 등록
    @POST("/api/shorts/register")
    fun registerShorts(
        @Body body: ShortsCreateRequest,
        @Header("Authorization") bearer: String
    ): Call<Long>

    //쇼츠 재생
    @GET("api/shorts/random")
    fun getShorts(
        @Query("seed") seed: String,
        @Query("page") page: Int,
        @Query("size") size: Int = 10,
        @Header("Authorization") bearer: String
    ): Call<List<ShortObject>>

    //프로필요약
    @GET("api/profile/{userId}/summary")
    fun getProfileSummary(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Call<ProfileSummaryResponse>

    //팔로우, 팔로우 해제 토글
    @POST("api/profile/{userId}/follow-toggle")
    fun toggleFollow(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Call<ResponseBody>

    @GET("api/shorts/{userId}")
    fun getUserShorts(
        @Header("Authorization") bearer: String,
        @Path("userId") userId: Int,
        @Query("sort") sort: String,        // latest | views | date
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): Call<ShortVideoListResponse>

    @POST("api/shorts/{id}/comment")
    fun addShortsComment(
        @Path("id") shortsId: Long,
        @Body dto: CommentRequestDTO,
        @Header("Authorization") bearer: String
    ): Call<Void>

    @GET("api/shorts/{id}/comments")
    fun getShortsComments(
        @Path("id") id: Long,
        @Header("Authorization") bearer: String
    ): Call<List<ShortCommentResponse>>

    // 팔로워 목록(나를 팔로우하는 사람들)
    @GET("api/profile/{userId}/followers")
    fun getFollowers(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Call<List<FollowUserResponse>>

    // 팔로잉 목록(내가 팔로우하는 사람들)
    @GET("api/profile/{userId}/followings")
    fun getFollowings(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Call<List<FollowUserResponse>>
}
