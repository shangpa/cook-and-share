package com.example.test.alarm

import Prefs
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test.R
import com.example.test.model.notification.FcmTokenRequestDTO
import com.example.test.network.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 푸시 알림 수신
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.data["title"] ?: "알림"
        val body = remoteMessage.data["body"] ?: ""

        Log.d("FCM", "onMessageReceived 호출됨")
        Log.d("FCM", "알림 제목: $title")
        Log.d("FCM", "알림 내용: $body")

        showNotification(applicationContext, title, body)
    }
    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, "default") // ← 채널 ID 반드시 일치해야 함
            .setSmallIcon(R.drawable.ic_bell_light) // 🔔 너 프로젝트에 있는 알림 아이콘으로 바꿔
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    // 새 토큰 수신 시 자동 호출됨
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val context = App.context // ✅ 항상 초기화된 컨텍스트 사용
        val prefs = Prefs(context)
        val authToken = prefs.token ?: return

        val request = FcmTokenRequestDTO(token, "ANDROID")

        RetrofitInstance.notificationApi.sendFcmToken(
            "Bearer $authToken",
            request
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("FCM", "FCM 토큰 서버 전송 성공")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("FCM", "FCM 토큰 서버 전송 실패", t)
            }
        })
    }}
