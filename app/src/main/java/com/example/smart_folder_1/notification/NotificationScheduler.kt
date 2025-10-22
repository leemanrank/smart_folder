package com.example.smart_folder_1.notification

import android.content.Context
import androidx.work.*
import com.example.smart_folder_1.worker.FileCheckWorker
import java.util.concurrent.TimeUnit

/**
 * 알림 스케줄링 관리
 */
object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    private const val FILE_CHECK_WORK_NAME = "file_check_periodic"

    /**
     * 주기적 파일 체크 스케줄 시작
     */
    fun scheduleFileCheckNotification(
        context: Context,
        repeatIntervalHours: Long = 24 // 기본 24시간마다
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true) // 배터리가 충분할 때만
            .build()

        val fileCheckRequest = PeriodicWorkRequestBuilder<FileCheckWorker>(
            repeatIntervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // 첫 실행은 1시간 후
            .addTag("file_check")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FILE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // 이미 있으면 유지
            fileCheckRequest
        )

        android.util.Log.d(TAG, "File check notification scheduled (every $repeatIntervalHours hours)")
    }

    /**
     * 스케줄 취소
     */
    fun cancelFileCheckNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(FILE_CHECK_WORK_NAME)
        android.util.Log.d(TAG, "File check notification cancelled")
    }

    /**
     * 즉시 파일 체크 실행 (테스트용)
     */
    fun runFileCheckNow(context: Context) {
        val fileCheckRequest = OneTimeWorkRequestBuilder<FileCheckWorker>()
            .addTag("file_check_now")
            .build()

        WorkManager.getInstance(context).enqueue(fileCheckRequest)
        android.util.Log.d(TAG, "File check triggered immediately")
    }

    /**
     * 스케줄 상태 확인
     */
    fun isScheduled(context: Context, callback: (Boolean) -> Unit) {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(FILE_CHECK_WORK_NAME)

        workInfos.addListener({
            try {
                val infos = workInfos.get()
                val isScheduled = infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
                callback(isScheduled)
            } catch (e: Exception) {
                callback(false)
            }
        }, { it.run() })
    }
}
