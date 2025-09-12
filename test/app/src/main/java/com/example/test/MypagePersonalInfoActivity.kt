package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.test.model.UserProfileResponse
import com.example.test.model.LoginInfoResponse
import com.example.test.model.UpdateUserRequest
import com.example.test.network.RetrofitInstance
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.example.test.model.ProfileImageResponse
import androidx.activity.result.PickVisualMediaRequest

class MypagePersonalInfoActivity : AppCompatActivity() {
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var passwordUnderline: View
    private lateinit var confirmUnderline: View
    private lateinit var passwordErrorText: TextView
    private lateinit var passwordMismatchText: TextView
    private lateinit var passwordNowInput: EditText
    private lateinit var passwordNowUnderline: View
    private lateinit var passwordNowErrorText: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var btnProfileCamera: ImageButton
    private var cameraImageUri: Uri? = null
    private var chosenImageUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_personal_info)

        passwordInput = findViewById(R.id.passwordInput) // 비밀번호 EditText id 직접 설정해줘야 함
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        passwordUnderline = findViewById(R.id.passwordUnderline)
        confirmUnderline = findViewById(R.id.confirmUnderline)
        passwordErrorText = findViewById(R.id.passwordErrorText)
        passwordMismatchText = findViewById(R.id.passwordMismatchText)
        passwordNowInput = findViewById(R.id.passwordNowInput)
        passwordNowUnderline = findViewById(R.id.passwordNowUnderline)
        passwordNowErrorText = findViewById(R.id.passwordNowErrorText)

        passwordNowInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val inputPassword = s.toString()
                checkCurrentPassword(inputPassword)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val pattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,15}\$")

                if (!pattern.matches(password)) {
                    passwordErrorText.visibility = View.VISIBLE
                    passwordUnderline.setBackgroundColor(Color.RED)
                } else {
                    passwordErrorText.visibility = View.GONE
                    passwordUnderline.setBackgroundColor(Color.BLUE)
                }

                checkPasswordMatch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val usernameEditText = findViewById<EditText>(R.id.personalInfoID)
        usernameEditText.isEnabled = false

        RetrofitInstance.apiService.getUserInfo("Bearer ${App.prefs.token}")
            .enqueue(object : Callback<LoginInfoResponse> {
                override fun onResponse(
                    call: Call<LoginInfoResponse>,
                    response: Response<LoginInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        findViewById<TextView>(R.id.personalInfoName).text = data?.name
                        usernameEditText.setText(data?.username)

                        val url = data?.profileImageUrl
                        if (!url.isNullOrBlank()) {
                            App.prefs.profileImageUrl = url
                            Glide.with(this@MypagePersonalInfoActivity)
                                .load(url)      // String 명확히 전달
                                .circleCrop()
                                .into(imageProfile)
                        }
                    }
                }
                override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) { /* ... */ }
            })



        val btnInfoFix = findViewById<LinearLayout>(R.id.btnInfoFix)

        btnInfoFix.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordInput.text.toString()

            //현재 비밀번호와 일치하는지 여부 판단
            if (passwordNowErrorText.visibility == View.VISIBLE) {
                Toast.makeText(this, "현재 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 비밀번호 유효성 검사 실패 시 중단
            if (passwordErrorText.visibility == View.VISIBLE || passwordMismatchText.visibility == View.VISIBLE) {
                Toast.makeText(this, "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val request = UpdateUserRequest(password = password)

            RetrofitInstance.apiService.updateUserInfo("Bearer ${App.prefs.token}", request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MypagePersonalInfoActivity, "수정 완료! 다시 로그인해 주세요", Toast.LENGTH_SHORT).show()

                            // 토큰 제거 (로그아웃)
                            App.prefs.token = ""

                            // 로그인 화면으로 이동
                            val intent = Intent(this@MypagePersonalInfoActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            // 현재 액티비티 종료
                            finishAffinity()
                        } else {
                            Toast.makeText(this@MypagePersonalInfoActivity, "수정 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MypagePersonalInfoActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                })

        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        imageProfile = findViewById(R.id.imageProfile)
        btnProfileCamera = findViewById(R.id.btnProfileCamera)

        val openChooser = { showImageSourceChooser() }
        imageProfile.setOnClickListener { openChooser() }
        btnProfileCamera.setOnClickListener { openChooser() }

        loadExistingProfileImage()   // 앱 시작 시 저장된 프로필 로드

    }


    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) openCamera() else Toast.makeText(this,"카메라 권한이 필요해요.",Toast.LENGTH_SHORT).show()
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) { chosenImageUri = cameraImageUri; showPreview(chosenImageUri); uploadProfileImage(chosenImageUri) }
        }

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri!=null){ chosenImageUri = uri; showPreview(uri); uploadProfileImage(uri) }
        }

    private fun showImageSourceChooser() {
        AlertDialog.Builder(this)
            .setTitle("프로필 이미지")
            .setItems(arrayOf("사진 촬영","앨범에서 선택")) { _, which ->
                if (which==0) ensureCameraAndOpen() else openGalleryPicker()
            }.show()
    }

    private fun ensureCameraAndOpen() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        else openCamera()
    }

    private fun openCamera() {
        val file = File.createTempFile("profile_${System.currentTimeMillis()}_", ".jpg", externalCacheDir)
        cameraImageUri = FileProvider.getUriForFile(this,"${packageName}.fileprovider", file)
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun openGalleryPicker() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showPreview(uri: Uri?) {
        if (uri==null) return
        Glide.with(this).load(uri).circleCrop().into(imageProfile)
    }

    private fun checkCurrentPassword(input: String) {
        val body = mapOf("password" to input)
        RetrofitInstance.apiService.checkPassword("Bearer ${App.prefs.token}", body)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    val match = response.body() ?: false
                    if (!match) {
                        passwordNowErrorText.visibility = View.VISIBLE
                        passwordNowUnderline.setBackgroundColor(Color.RED)
                    } else {
                        passwordNowErrorText.visibility = View.GONE
                        passwordNowUnderline.setBackgroundColor(Color.BLUE)
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.e("비밀번호 확인 실패", t.message ?: "알 수 없는 오류")
                }
            })
    }

    private fun checkPasswordMatch() {
        val pw = passwordInput.text.toString()
        val confirm = confirmPasswordInput.text.toString()

        if (confirm.isNotEmpty() && pw != confirm) {
            passwordMismatchText.visibility = View.VISIBLE
            confirmUnderline.setBackgroundColor(Color.RED)
        } else if (confirm.isNotEmpty() && pw == confirm) {
            passwordMismatchText.visibility = View.GONE
            confirmUnderline.setBackgroundColor(Color.BLUE)
        }
    }

    private fun uploadProfileImage(uri: Uri?) {
        if (uri == null) return
        val token = App.prefs.token ?: return

        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            "image",
            "profile_${System.currentTimeMillis()}.jpg",
            body
        )

        RetrofitInstance.apiService.uploadProfileImage("Bearer $token", part)
            .enqueue(object : Callback<ProfileImageResponse> {
                override fun onResponse(
                    call: Call<ProfileImageResponse>,
                    response: Response<ProfileImageResponse>
                ) {
                    val url = response.body()?.profileImageUrl
                    if (response.isSuccessful && !url.isNullOrBlank()) {
                        // 성공: 로컬에만 저장, 토스트는 띄우지 않음
                        App.prefs.profileImageUrl = url
                    } else {
                        Toast.makeText(this@MypagePersonalInfoActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileImageResponse>, t: Throwable) {
                    Toast.makeText(this@MypagePersonalInfoActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun loadExistingProfileImage() {
        val url: String? = App.prefs.profileImageUrl
        if (!url.isNullOrBlank()) {
            Glide.with(this).load(url).circleCrop().into(imageProfile)
        } else {
            imageProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

}
