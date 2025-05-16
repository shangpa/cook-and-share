package com.example.test.network

import com.example.test.model.board.CommentListResponse
import com.example.test.model.board.CommentRequest
import com.example.test.model.board.CommentResponse
import com.example.test.model.board.CommunityDetailResponse
import com.example.test.model.board.CommunityMainResponse
import com.example.test.model.board.CommunityPostRequest
import com.example.test.model.board.CommunityPostResponse
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

    // 요리게시판
    @GET("api/boards/cooking")
    fun getCookingBoards(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "latest" // like, comment, latest
    ): Call<List<CommunityDetailResponse>>
}
