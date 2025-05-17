package com.example.test.Utils

import android.widget.ImageButton
import android.widget.Toast
import com.example.test.App
import com.example.test.R
import com.example.test.network.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RecommendUtils {

    fun setupRecommendButton(button: ImageButton, recipeId: Long) {
        val token = "Bearer ${App.prefs.token ?: ""}"

        // 초기 추천 상태 조회
        RetrofitInstance.apiService.isRecipeRecommended(recipeId, token)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    val isRecommended = response.body() ?: false
                    button.setImageResource(
                        if (isRecommended) R.drawable.ic_good_fill else R.drawable.ic_good
                    )
                    button.setTag(R.id.goodButton, isRecommended)
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(button.context, "추천 상태 조회 실패", Toast.LENGTH_SHORT).show()
                }
            })

        // 버튼 클릭 시 추천 토글
        button.setOnClickListener {
            RetrofitInstance.apiService.toggleRecommend(recipeId, token)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string() ?: ""
                            val isNowRecommended = result.contains("추가")

                            button.setImageResource(
                                if (isNowRecommended) R.drawable.ic_good_fill else R.drawable.ic_good
                            )
                            button.setTag(R.id.goodButton, isNowRecommended)

                            if (isNowRecommended) {
                                Toast.makeText(button.context, "해당 레시피를 추천하였습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(button.context, "추천을 취소하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(button.context, "서버 요청 실패", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
