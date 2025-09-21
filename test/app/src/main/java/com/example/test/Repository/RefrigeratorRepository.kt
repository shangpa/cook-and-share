package com.example.test.repository

import com.example.test.model.refrigerator.Refrigerator
import com.example.test.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File

object RefrigeratorRepository {

    private val TEXT = "text/plain".toMediaType()
    private val IMAGE = "image/*".toMediaType()

    /** 목록 가져오기  */
    fun list(token: String): Call<List<Refrigerator>> =
        RetrofitInstance.apiService.getMyRefrigerators(bearer(token))

    /** 생성 */
    fun create(
        token: String,
        name: String,
        memo: String?,
        imageFile: File?
    ): Call<Refrigerator> {
        val nameBody: RequestBody = name.toRequestBody(TEXT)
        val memoBody: RequestBody? = memo?.toRequestBody(TEXT)
        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val req = file.asRequestBody(IMAGE)
            MultipartBody.Part.createFormData("image", file.name, req)
        }

        return RetrofitInstance.apiService.createRefrigerator(
            token = bearer(token),
            name = nameBody,
            memo = memoBody,
            image = imagePart
        )
    }

    /** 수정 */
    fun update(
        token: String,
        id: Long,
        name: String,
        memo: String?,
        imageFile: File?
    ): Call<Refrigerator> {
        val nameBody: RequestBody = name.toRequestBody(TEXT)
        val memoBody: RequestBody? = memo?.toRequestBody(TEXT)
        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val req = file.asRequestBody(IMAGE)
            MultipartBody.Part.createFormData("image", file.name, req)
        }

        return RetrofitInstance.apiService.updateRefrigerator(
            token = bearer(token),
            id = id,
            name = nameBody,
            memo = memoBody,
            image = imagePart
        )
    }

    /** 삭제 */
    fun delete(token: String, id: Long) =
        RetrofitInstance.apiService.deleteRefrigerator(bearer(token), id)

    private fun bearer(token: String) =
        if (token.startsWith("Bearer ")) token else "Bearer $token"
}
