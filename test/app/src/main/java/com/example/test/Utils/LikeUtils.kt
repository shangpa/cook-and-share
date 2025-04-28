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

object LikeUtils {

    fun setupLikeButton(button: ImageButton, recipeId: Long) {
        button.setOnClickListener {
            val token = App.prefs.token.toString()
            RetrofitInstance.apiService.toggleLike(recipeId, "Bearer $token")
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string() ?: ""

                            // 좋아요 상태 확인
                            val isLiked = result.contains("추가")

                            // 하트 이미지 업데이트
                            button.setImageResource(
                                if (isLiked) R.drawable.ic_heart_fill else R.drawable.ic_recipe_heart
                            )

                            // 태그로 상태 저장
                            button.setTag(R.id.heartButton, isLiked)

                            // 토스트 출력
                            if (isLiked) {
                                Toast.makeText(button.context, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(button.context, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}