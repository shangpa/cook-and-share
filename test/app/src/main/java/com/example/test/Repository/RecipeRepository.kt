package com.example.test.Repository


import com.example.test.model.RecipeRequest
import com.example.test.model.RecipeResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RecipeRepository {

    fun uploadRecipe(token: String, recipe: RecipeRequest, callback: (RecipeResponse?) -> Unit) {
        val call = RetrofitInstance.apiService.createRecipe("Bearer $token", recipe)
        call.enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()) // 성공 시 콜백 호출
                } else {
                    println("레시피 업로드 실패: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                println("네트워크 오류: ${t.message}")
                callback(null)
            }
        })
    }
}