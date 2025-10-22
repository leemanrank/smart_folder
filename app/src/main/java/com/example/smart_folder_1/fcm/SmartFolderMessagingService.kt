package com.example.smart_folder_1.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smart_folder_1.MainActivity
import com.example.smart_folder_1.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM 메시징 서비스
 */
class SmartFolderMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "smart_folder_notifications"
        private const val CHANNEL_NAME = "스마트 폴더 알림"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d(TAG, "New FCM token: $token")

        // TODO: 서버에 토큰 전송 (추후 백엔드 구현 시)
        // sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        android.util.Log.d(TAG, "FCM message received from: ${message.from}")

        // 알림 데이터 처리
        message.notification?.let { notification ->
            val title = notification.title ?: "스마트 폴더"
            val body = notification.body ?: ""

            showNotification(title, body, message.data)
        }

        // 데이터 페이로드 처리
        if (message.data.isNotEmpty()) {
            android.util.Log.d(TAG, "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    /**
     * 알림 표시
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 데이터 전달
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "파일 정리 알림을 받습니다"
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 데이터 페이로드 처리
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]

        when (type) {
            "file_cleanup_reminder" -> {
                // 파일 정리 리마인더
                val fileCount = data["file_count"]?.toIntOrNull() ?: 0
                showNotification(
                    "파일이 쌓였어요! 📁",
                    "정리되지 않은 파일이 ${fileCount}개 있습니다. 지금 정리하시겠어요?",
                    data
                )
            }
            "weekly_report" -> {
                // 주간 리포트
                val savedSpace = data["saved_space"] ?: "0 MB"
                showNotification(
                    "이번 주 정리 완료! 🎉",
                    "총 ${savedSpace}의 공간을 확보했습니다!",
                    data
                )
            }
            "achievement_unlocked" -> {
                // 업적 달성
                val achievement = data["achievement"] ?: ""
                showNotification(
                    "업적 달성! 🏆",
                    achievement,
                    data
                )
            }
        }
    }
}
