package com.example.test.network

import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.model.UserSimpleResponse
import com.example.test.model.review.TpReviewRequestDTO
import com.example.test.model.review.TpReviewResponseDTO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("/api/trade-posts/{id}/complete-request")
    fun requestComplete(
        @Header("Authorization") token: String,
        @Path("id") postId: Long
    ): Call<ResponseBody>

    @GET("/api/trade-posts/{id}/complete-requests")
    fun getCompleteRequestUsers(
        @Header("Authorization") token: String,
        @Path("id") postId: Long
    ): Call<List<UserSimpleResponse>>

    @PATCH("/api/trade-posts/{id}/complete")
    fun completeTradePost(
        @Header("Authorization") token: String,
        @Path("id") postId: Long,
        @Query("buyerId") buyerId: Long
    ): Call<TradePostSimpleResponse>

    @GET("/api/trade-posts/mypurchases")
    fun getMyPurchasedPosts(
        @Header("Authorization") token: String
    ): Call<List<TradePostSimpleResponse>>

    @POST("/api/tp-reviews")
    fun createTpReview(
        @Header("Authorization") token: String,
        @Body dto: TpReviewRequestDTO
    ): Call<TpReviewResponseDTO>

    @GET("/api/tp-reviews/reviews-on-my-posts")
    fun getReviewsOnMyTradePosts(
        @Header("Authorization") token: String
    ): Call<List<TpReviewResponseDTO>>

    @GET("/api/tp-reviews/my-reviews")
    fun getMyReviews(
        @Header("Authorization") token: String
    ): Call<List<TpReviewResponseDTO>>

    @GET("/api/tp-reviews/reviews-on-user/username/{username}")
    fun getReviewsByUsername(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Call<List<TpReviewResponseDTO>>

    @GET("/api/trade-posts/user/{username}")
    fun getUserTradePosts(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Call<List<TradePostSimpleResponse>>

}
