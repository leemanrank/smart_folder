package com.smartfolder.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartfolder.app.MainActivity
import com.smartfolder.app.R
import com.smartfolder.app.data.repository.DirectoryType
import com.smartfolder.app.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 주기적으로 파일을 체크하고 알림을 보내는 Worker
 */
class FileCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "FileCheckWorker"
        private const val CHANNEL_ID = "file_cleanup_reminder"
        private const val CHANNEL_NAME = "파일 정리 알림"
        private const val NOTIFICATION_ID = 1001

        // 알림을 보낼 최소 파일 개수
        const val MIN_FILE_COUNT_FOR_NOTIFICATION = 50
    }

    private val repository = FileRepository(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(TAG, "Starting file check...")

            // 전체 저장소 스캔
            val files = repository.scanFiles(DirectoryType.ALL_STORAGE)
            val fileCount = files.size

            android.util.Log.d(TAG, "Found $fileCount files")

            // 파일이 일정 개수 이상이면 알림 전송
            if (fileCount >= MIN_FILE_COUNT_FOR_NOTIFICATION) {
                sendCleanupNotification(fileCount)
            }

            // 통계 계산
            val totalSize = files.sumOf { it.size }
            val totalSizeMB = totalSize / (1024 * 1024)

            android.util.Log.d(TAG, "Total size: ${totalSizeMB}MB")

            // 마지막 체크 시간 저장
            saveLastCheckTime()

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error checking files", e)
            Result.retry()
        }
    }

    /**
     * 파일 정리 알림 전송
     */
    private fun sendCleanupNotification(fileCount: Int) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "cleanup_files")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("파일이 쌓였어요! 📁")
            .setContentText("정리되지 않은 파일이 ${fileCount}개 있습니다.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("정리되지 않은 파일이 ${fileCount}개 있습니다.\n지금 정리하고 저장 공간을 확보하세요!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "지금 정리",
                pendingIntent
            )
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        android.util.Log.d(TAG, "Cleanup notification sent for $fileCount files")
    }

    /**
     * 알림 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "파일이 많이 쌓였을 때 정리를 권장하는 알림"
                enableVibration(true)
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 마지막 체크 시간 저장
     */
    private fun saveLastCheckTime() {
        val sharedPreferences = applicationContext.getSharedPreferences(
            "smart_folder_prefs",
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit()
            .putLong("last_file_check_time", System.currentTimeMillis())
            .apply()
    }
}
