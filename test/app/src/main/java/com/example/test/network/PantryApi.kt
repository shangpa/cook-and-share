package com.example.test.network.api

import com.example.test.model.pantry.PantryCreateRequest
import com.example.test.model.pantry.PantryResponse
import com.example.test.model.pantry.PantryUpdateRequest
import retrofit2.Call
import retrofit2.http.*

interface PantryApi {

    @POST("/api/pantries")
    fun createPantry(
        @Header("Authorization") token: String,
        @Body body: PantryCreateRequest
    ): Call<PantryResponse>

    @GET("/api/pantries")
    fun listPantries(
        @Header("Authorization") token: String
    ): Call<List<PantryResponse>>

    @PUT("/api/pantries/{id}")
    fun updatePantry(
        @Header("Authorization") token: String,
        @Path("id") pantryId: Long,
        @Body body: PantryUpdateRequest
    ): Call<PantryResponse>

    @DELETE("api/pantries/{id}")
    fun deletePantry(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<Void>
}
