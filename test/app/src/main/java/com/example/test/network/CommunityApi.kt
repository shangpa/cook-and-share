package com.example.test.network

import com.example.test.model.board.CommunityDetailResponse
import com.example.test.model.board.CommunityPostRequest
import com.example.test.model.board.CommunityPostResponse
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
}
