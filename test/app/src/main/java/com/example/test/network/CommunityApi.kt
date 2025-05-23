package com.example.test.network

import com.example.test.model.community.CommentListResponse
import com.example.test.model.community.CommentRequest
import com.example.test.model.community.CommunityDetailResponse
import com.example.test.model.community.CommunityMainResponse
import com.example.test.model.community.CommunityPostRequest
import com.example.test.model.community.CommunityPostResponse
import com.example.test.model.community.ReportRequestDTO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CommunityApi {

    //커뮤니티 작성
    @POST("/api/boards")
    fun uploadPost(
        @Header("Authorization") token: String,
        @Body request: CommunityPostRequest
    ): Call<CommunityPostResponse>

    //인기 게시글 TOP 10
    @GET("/api/boards/popular")
    fun getPopularPosts(
        @Header("Authorization") token: String
    ): Call<List<CommunityDetailResponse>>

    // 게시글 상세 조회
    @GET("/api/boards/{id}/detail")
    fun getPostDetail(
        @Header("Authorization") token: String,
        @Path("id") postId: Long
    ): Call<CommunityDetailResponse>

    // 댓글 목록 조회
    @GET("/api/boards/{id}/commentsWithC")
    fun getCommentsWithCount(
        @Header("Authorization") token: String,
        @Path("id") postId: Long
    ): Call<CommentListResponse>

    // 댓글 작성
    @POST("/api/boards/{id}/comment")
    fun postComment(
        @Header("Authorization") token: String,
        @Path("id") postId: Long,
        @Body comment: CommentRequest
    ): Call<Void>

    //좋아요기능
    @POST("/api/boards/{id}/like")
    fun likePost(
        @Header("Authorization") token: String,
        @Path("id") postId: Long
    ): Call<ResponseBody>

    //메인기능
    @GET("/api/boards/main")
    fun getCommunityMain(
        @Header("Authorization") token: String
    ): Call<CommunityMainResponse>


    //타입별
    @GET("api/boards/{type}")
    fun getBoardsByType(
        @Path("type") type: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "latest"
    ): Call<List<CommunityDetailResponse>>

    @POST("/api/reports")
    fun report(
        @Header("Authorization") token: String,
        @Body request: ReportRequestDTO
    ): Call<Void>
}
