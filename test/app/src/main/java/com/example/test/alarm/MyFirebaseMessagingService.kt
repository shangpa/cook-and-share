package com.example.test.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test.R
import com.example.test.network.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 푸시 알림 수신
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "[알림]"
        val content = remoteMessage.notification?.body ?: ""

        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_bell_light)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }

    // 새 토큰 수신 시 자동 호출됨
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val authToken = App.prefs.token ?: return
        val request = FcmTokenRequestDTO(token, "ANDROID")

        RetrofitInstance.api.sendFcmToken("Bearer $authToken", request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("FCM", "FCM 토큰 서버 전송 성공")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("FCM", "FCM 토큰 서버 전송 실패", t)
                }
            })
    }
}
