package com.smartfolder.app.data.model

import java.io.File

/**
 * 파일 아이템 데이터 클래스
 */
data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val size: Long = file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension,
    val mimeType: String = "",
    var suggestedCategory: FileCategory? = null,
    var confidence: Float = 0f
) {
    val isImage: Boolean
        get() = extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")

    val isVideo: Boolean
        get() = extension.lowercase() in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")

    val isDocument: Boolean
        get() = extension.lowercase() in listOf("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx")

    val isAudio: Boolean
        get() = extension.lowercase() in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a")

    val isArchive: Boolean
        get() = extension.lowercase() in listOf("zip", "rar", "7z", "tar", "gz")
}
