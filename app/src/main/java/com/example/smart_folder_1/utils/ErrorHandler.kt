package com.example.smart_folder_1.utils

import android.content.Context
import java.io.FileNotFoundException
import java.io.IOException
import java.net.UnknownHostException

/**
 * 에러 처리 유틸리티
 */
object ErrorHandler {

    /**
     * 사용자 친화적인 에러 메시지 변환
     */
    fun getUserFriendlyMessage(throwable: Throwable): String {
        return when (throwable) {
            // 파일 관련 에러
            is FileNotFoundException -> "파일을 찾을 수 없습니다. 파일이 이동되거나 삭제되었을 수 있습니다."
            is SecurityException -> "파일에 접근할 권한이 없습니다. 설정에서 저장소 권한을 허용해주세요."
            is IOException -> "파일 작업 중 오류가 발생했습니다. 저장 공간을 확인해주세요."

            // 네트워크 관련 에러
            is UnknownHostException -> "인터넷 연결을 확인해주세요."
            is java.net.SocketTimeoutException -> "서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."

            // 메모리 관련 에러
            is OutOfMemoryError -> "메모리가 부족합니다. 일부 파일을 삭제한 후 다시 시도해주세요."

            // 기타 에러
            else -> {
                val message = throwable.message
                when {
                    message?.contains("ENOSPC", ignoreCase = true) == true ->
                        "저장 공간이 부족합니다. 불필요한 파일을 삭제해주세요."
                    message?.contains("permission", ignoreCase = true) == true ->
                        "권한이 필요합니다. 설정에서 필요한 권한을 허용해주세요."
                    message?.contains("network", ignoreCase = true) == true ->
                        "네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요."
                    !message.isNullOrBlank() ->
                        "오류가 발생했습니다: ${message.take(100)}"
                    else ->
                        "알 수 없는 오류가 발생했습니다. 앱을 재시작해주세요."
                }
            }
        }
    }

    /**
     * 권한 관련 에러 메시지
     */
    fun getPermissionErrorMessage(permission: String): String {
        return when {
            permission.contains("STORAGE") || permission.contains("EXTERNAL_STORAGE") ->
                "파일을 스캔하고 정리하려면 저장소 권한이 필요합니다."
            permission.contains("NOTIFICATION") ->
                "파일 정리 알림을 받으려면 알림 권한이 필요합니다."
            permission.contains("READ_MEDIA") ->
                "미디어 파일에 접근하려면 미디어 권한이 필요합니다."
            else ->
                "이 기능을 사용하려면 권한이 필요합니다."
        }
    }

    /**
     * 에러 로깅 (개발용)
     */
    fun logError(tag: String, message: String, throwable: Throwable?) {
        android.util.Log.e(tag, message, throwable)

        // TODO: 프로덕션에서는 Firebase Crashlytics 등으로 전송
        // FirebaseCrashlytics.getInstance().recordException(throwable ?: Exception(message))
    }

    /**
     * 파일 작업 에러 카테고리 분류
     */
    enum class FileErrorType {
        PERMISSION_DENIED,      // 권한 거부
        FILE_NOT_FOUND,        // 파일 없음
        STORAGE_FULL,          // 저장 공간 부족
        FILE_IN_USE,           // 파일 사용 중
        UNKNOWN                // 알 수 없음
    }

    /**
     * 파일 에러 타입 판별
     */
    fun getFileErrorType(throwable: Throwable): FileErrorType {
        return when (throwable) {
            is SecurityException -> FileErrorType.PERMISSION_DENIED
            is FileNotFoundException -> FileErrorType.FILE_NOT_FOUND
            is IOException -> {
                if (throwable.message?.contains("ENOSPC", ignoreCase = true) == true) {
                    FileErrorType.STORAGE_FULL
                } else if (throwable.message?.contains("EBUSY", ignoreCase = true) == true) {
                    FileErrorType.FILE_IN_USE
                } else {
                    FileErrorType.UNKNOWN
                }
            }
            else -> FileErrorType.UNKNOWN
        }
    }
}
