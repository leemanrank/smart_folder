package com.example.smart_folder_1.data.model

/**
 * 중복 파일 그룹
 */
data class DuplicateFileGroup(
    val hash: String,
    val files: List<FileItem>,
    val totalSize: Long = files.sumOf { it.size },
    val wastedSpace: Long = totalSize - (files.firstOrNull()?.size ?: 0L)
) {
    val fileCount: Int get() = files.size
    val canSave: Long get() = wastedSpace
}
