package com.example.test.alarm

import Prefs
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test.MaterialChatDetailActivity
import com.example.test.R
import com.example.test.Utils.ChatSessionManager
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
        val category = remoteMessage.data["category"] ?: "GENERAL"
        val roomKey = remoteMessage.data["roomKey"]

        Log.d("FCM", "알림 수신됨 - category: $category")

        // 채팅 알림일 경우 → 별도 처리
        if (category == "CHAT") {
            if (roomKey != null && roomKey == ChatSessionManager.currentChatRoomKey) {
                Log.d("FCM", "현재 채팅방 열려있음 → 알림 무시")
                return
            }
            // 채팅 알림은 별도로 처리 (채팅방으로 이동할 수 있게)
            showChatNotification(applicationContext, title, body, roomKey)
            return
        }
        // 일반 알림 처리
        showSimpleNotification(applicationContext, title, body)
    }
    fun showSimpleNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_bell_light)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    fun showChatNotification(context: Context, title: String, message: String, roomKey: String?) {
        val intent = Intent(context, MaterialChatDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("roomKey", roomKey)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_chatt)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // 새 토큰 수신 시 자동 호출됨
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val prefs = Prefs(applicationContext)
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
