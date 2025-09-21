package com.example.test.repository

import com.example.test.model.refrigerator.Refrigerator
import com.example.test.network.ApiService
import com.example.test.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File

object RefrigeratorRepository {

    private val api: ApiService = RetrofitInstance.apiService

    /** 리스트 불러오기 */
    fun list(token: String): Call<List<Refrigerator>> =
        api.getMyRefrigerators("Bearer $token")

    /** 생성  */
    fun create(
        token: String,
        name: String,
        memo: String?,
        imageFile: File?
    ): Call<Refrigerator> {
        val nameBody: RequestBody =
            name.toRequestBody("text/plain".toMediaTypeOrNull())
        val memoBody: RequestBody? =
            memo?.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val req = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, req)
        }

        return api.createRefrigerator("Bearer $token", nameBody, memoBody, imagePart)
    }

    /** 수정  */
    fun update(
        token: String,
        id: Long,
        name: String,
        memo: String?,
        imageFile: File?
    ): Call<Refrigerator> {
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val memoBody = memo?.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val req = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, req)
        }

        return api.updateRefrigerator("Bearer $token", id, nameBody, memoBody, imagePart)
    }

    /** 삭제  */
    fun delete(token: String, id: Long): Call<Void> =
        api.deleteRefrigerator("Bearer $token", id)
}
