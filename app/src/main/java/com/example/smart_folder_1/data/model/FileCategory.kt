package com.example.smart_folder_1.data.model

/**
 * 파일 카테고리 열거형
 */
enum class FileCategory(val displayName: String, val color: Long) {
    IMAGES("사진", 0xFF2196F3),
    VIDEOS("동영상", 0xFFE91E63),
    DOCUMENTS("문서", 0xFF4CAF50),
    AUDIO("오디오", 0xFFFF9800),
    ARCHIVES("압축파일", 0xFF9C27B0),
    DOWNLOADS("다운로드", 0xFF00BCD4),
    WORK("업무", 0xFFFF5722),
    PERSONAL("개인", 0xFF8BC34A),
    SCREENSHOTS("스크린샷", 0xFFFFEB3B),
    MEMES("밈/짤", 0xFFFF4081),
    IMPORTANT("중요", 0xFFF44336),
    OTHERS("기타", 0xFF607D8B);

    companion object {
        fun fromExtension(extension: String): FileCategory {
            return when (extension.lowercase()) {
                in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic") -> IMAGES
                in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm") -> VIDEOS
                in listOf("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx") -> DOCUMENTS
                in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a") -> AUDIO
                in listOf("zip", "rar", "7z", "tar", "gz") -> ARCHIVES
                else -> OTHERS
            }
        }
    }
}
