package com.example.test.network.api

import com.example.test.model.ingredients.IngredientResponse
import com.example.test.model.pantry.IngredientHistoryResponse
import com.example.test.model.pantry.IngredientMasterResponse
import com.example.test.model.pantry.PantryCreateRequest
import com.example.test.model.pantry.PantryResponse
import com.example.test.model.pantry.PantryStockCreateRequest
import com.example.test.model.pantry.PantryStockDetailResponse
import com.example.test.model.pantry.PantryStockDto
import com.example.test.model.pantry.PantryStockResponse
import com.example.test.model.pantry.PantryStockUpdateRequest
import com.example.test.model.pantry.PantryUpdateRequest
import com.example.test.model.pantry.UnitResponse
import com.example.test.model.pantry.PantryStatsResponse
import com.example.test.model.recipt.ReceiptOcrConfirmRequest
import retrofit2.Call
import retrofit2.http.*
import com.example.test.model.recipt.OcrConfirmResult

interface PantryApi {

    @POST("/api/pantries")
    suspend fun createPantry(
        @Header("Authorization") token: String,
        @Body body: PantryCreateRequest
    ): PantryResponse

    @GET("/api/pantries")
    suspend fun listPantries(
        @Header("Authorization") token: String
    ): List<PantryResponse>

    @GET("/api/pantries/{id}")
    suspend fun getPantry(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): PantryResponse

    @PUT("/api/pantries/{id}")
    suspend fun updatePantry(
        @Header("Authorization") token: String,
        @Path("id") pantryId: Long,
        @Body body: PantryUpdateRequest
    ): PantryResponse

    @DELETE("/api/pantries/{id}")
    suspend fun deletePantry(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): retrofit2.Response<Unit>

    // --- Stocks (목록/조회/추가/수정/삭제) ---
    @GET("/api/pantries/{id}/stocks")
    suspend fun listPantryStocks(
        @Header("Authorization") token: String,
        @Path("id") pantryId: Long
    ): List<PantryStockDto>

    @GET("/api/pantries/{pantryId}/stocks/{stockId}")
    suspend fun getPantryStock(
        @Header("Authorization") token: String,
        @Path("pantryId") pantryId: Long,
        @Path("stockId") stockId: Long
    ): PantryStockDetailResponse

    @POST("/api/pantries/{pantryId}/stocks")
    suspend fun addPantryStock(
        @Header("Authorization") token: String,
        @Path("pantryId") pantryId: Long,
        @Body req: PantryStockCreateRequest
    ): PantryStockResponse

    @PUT("/api/pantries/{pantryId}/stocks/{stockId}")
    suspend fun updatePantryStock(
        @Header("Authorization") token: String,
        @Path("pantryId") pantryId: Long,
        @Path("stockId") stockId: Long,
        @Body body: PantryStockUpdateRequest
    ): retrofit2.Response<Unit>

    @DELETE("/api/pantries/{pantryId}/stocks/{stockId}")
    suspend fun deletePantryStock(
        @Header("Authorization") token: String,
        @Path("pantryId") pantryId: Long,
        @Path("stockId") stockId: Long
    ): retrofit2.Response<Unit>

    // 벌크 삭제 엔드포인트를 만들었다면
    @POST("/api/pantries/{pantryId}/stocks/delete")
    suspend fun deletePantryStocks(
        @Header("Authorization") token: String,
        @Path("pantryId") pantryId: Long,
        @Body body: IdsRequest
    ): retrofit2.Response<Unit>
    data class IdsRequest(val ids: List<Long>)

    // --- 마스터 데이터 ---
    @GET("/api/ingredients")
    suspend fun getIngredients(
        @Header("Authorization") token: String,
        @Query("category") category: String,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): List<IngredientMasterResponse>

    @GET("/api/units")
    suspend fun getUnits(
        @Header("Authorization") token: String
    ): List<UnitResponse>

    // 사용,추가내역
    @GET("/api/pantries/{pantryId}/history")
    suspend fun listPantryHistory(
        @Header("Authorization") token: String,
        @Path("pantryId") pantryId: Long,
        @Query("ingredientName") ingredientName: String? = null
    ): List<IngredientHistoryResponse>

    //재료 가져오기
    @GET("/api/ingredients")
    fun listIngredients(
        @Header("Authorization") token: String,
        @Query("category") category: String,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Call<List<IngredientResponse>>

    //모든 재료 가져오기
    @GET("/api/ingredients/all")
    fun listAll(@Header("Authorization") token: String,): Call<List<IngredientResponse>>

    //냉장고 통계
    @GET("/api/pantry/stats/overview")
    suspend fun getPantryOverview(
        @Header("Authorization") token: String,
        @Query("from") from: String? = null,
        @Query("to")   to: String? = null
    ): PantryStatsResponse

    @POST("/api/pantries/{pantryId}/ocr/confirm")
    fun confirmReceiptOcr(
        @Header("Authorization") bearer: String,
        @Path("pantryId") pantryId: Long,
        @Body body: ReceiptOcrConfirmRequest
    ): Call<List<OcrConfirmResult>>
}
