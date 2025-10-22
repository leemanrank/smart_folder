package com.example.smart_folder_1.data.model

/**
 * 파일 통계 데이터 모델
 */
data class FileStatistics(
    val totalFiles: Int = 0,
    val totalSize: Long = 0,
    val categoryBreakdown: Map<FileCategory, CategoryStats> = emptyMap(),
    val largestFiles: List<FileItem> = emptyList(),
    val oldestFiles: List<FileItem> = emptyList(),
    val recentFiles: List<FileItem> = emptyList(),
    val duplicateFileCount: Int = 0,
    val duplicateFileSize: Long = 0
)

/**
 * 카테고리별 통계
 */
data class CategoryStats(
    val category: FileCategory,
    val fileCount: Int = 0,
    val totalSize: Long = 0,
    val percentage: Float = 0f
)

/**
 * 정리 이력
 */
data class CleanupHistory(
    val timestamp: Long,
    val filesOrganized: Int,
    val filesDeleted: Int,
    val spaceFreed: Long
)
