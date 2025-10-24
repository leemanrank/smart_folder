package com.smartfolder.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.smartfolder.app.data.database.AppDatabase
import com.smartfolder.app.data.database.UserPreference
import com.smartfolder.app.data.model.CategoryStats
import com.smartfolder.app.data.model.DuplicateFileGroup
import com.smartfolder.app.data.model.FileCategory
import com.smartfolder.app.data.model.FileItem
import com.smartfolder.app.data.model.FileStatistics
import com.smartfolder.app.ml.FileClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * 파일 관리 Repository
 */
class FileRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userPreferenceDao = database.userPreferenceDao()
    private val fileClassifier = FileClassifier()

    /**
     * 지정된 디렉토리의 파일들을 스캔
     */
    suspend fun scanFiles(directoryType: DirectoryType): List<FileItem> = withContext(Dispatchers.IO) {
        android.util.Log.d("FileRepository", "Starting scan for directory type: $directoryType")

        when (directoryType) {
            DirectoryType.ALL_STORAGE -> {
                val result = scanAllStorage()
                android.util.Log.d("FileRepository", "Scan completed. Found ${result.size} files")
                result
            }
            else -> {
                val directory = when (directoryType) {
                    DirectoryType.DOWNLOADS -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    DirectoryType.PICTURES -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    DirectoryType.DOCUMENTS -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    DirectoryType.DCIM -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    else -> return@withContext emptyList()
                }

                android.util.Log.d("FileRepository", "Scanning directory: ${directory.absolutePath}")
                android.util.Log.d("FileRepository", "Directory exists: ${directory.exists()}")

                if (!directory.exists()) {
                    android.util.Log.w("FileRepository", "Directory does not exist: ${directory.absolutePath}")
                    return@withContext emptyList()
                }

                val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()
                android.util.Log.d("FileRepository", "Found ${files.size} files in ${directory.name}")

                files.map { file ->
                    FileItem(file = file)
                }
            }
        }
    }

    /**
     * 전체 저장소에서 주요 폴더 자동 스캔
     */
    private suspend fun scanAllStorage(): List<FileItem> = withContext(Dispatchers.IO) {
        val allFiles = mutableListOf<FileItem>()

        android.util.Log.d("FileRepository", "Starting ALL_STORAGE scan")

        // 주요 시스템 디렉토리 목록
        val mainDirectories = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            File(Environment.getExternalStorageDirectory(), "Screenshots"),
            File(Environment.getExternalStorageDirectory(), "Download")
        )

        // 각 디렉토리에서 파일 수집 (재귀 탐색)
        mainDirectories.forEach { directory ->
            android.util.Log.d("FileRepository", "Checking directory: ${directory.absolutePath}")
            android.util.Log.d("FileRepository", "  Exists: ${directory.exists()}, IsDirectory: ${directory.isDirectory}")

            if (directory.exists() && directory.isDirectory) {
                val filesInDir = scanDirectoryRecursive(directory, maxDepth = 3)
                android.util.Log.d("FileRepository", "  Found ${filesInDir.size} files in ${directory.name}")
                allFiles.addAll(filesInDir)
            }
        }

        android.util.Log.d("FileRepository", "Total files found: ${allFiles.size}")
        allFiles
    }

    /**
     * 디렉토리를 재귀적으로 스캔 (성능을 위해 깊이 제한)
     */
    private fun scanDirectoryRecursive(
        directory: File,
        currentDepth: Int = 0,
        maxDepth: Int = 3
    ): List<FileItem> {
        if (currentDepth >= maxDepth) return emptyList()

        val files = mutableListOf<FileItem>()

        try {
            directory.listFiles()?.forEach { file ->
                when {
                    file.isFile -> {
                        files.add(FileItem(file = file))
                    }
                    file.isDirectory && !file.name.startsWith(".") -> {
                        // 숨김 폴더 제외, 재귀 탐색
                        files.addAll(scanDirectoryRecursive(file, currentDepth + 1, maxDepth))
                    }
                }
            }
        } catch (e: Exception) {
            // 권한 오류 등 무시
        }

        return files
    }

    /**
     * 파일들을 자동으로 분류 (최적화됨)
     */
    suspend fun classifyFiles(files: List<FileItem>): List<FileItem> = withContext(Dispatchers.IO) {
        // Flow 대신 직접 데이터베이스에서 가져오기 (속도 향상)
        val userPreferences = userPreferenceDao.getAllPreferencesList()
        fileClassifier.classifyFiles(files, userPreferences)
    }

    /**
     * 사용자의 선택을 학습 데이터로 저장
     */
    suspend fun saveUserPreference(fileItem: FileItem, selectedCategory: FileCategory, isManualOverride: Boolean = false) {
        withContext(Dispatchers.IO) {
            val preference = UserPreference(
                fileName = fileItem.name,
                fileExtension = fileItem.extension,
                fileSize = fileItem.size,
                filePath = fileItem.path,
                selectedCategory = selectedCategory.name,
                isManualOverride = isManualOverride
            )
            userPreferenceDao.insert(preference)
        }
    }

    /**
     * 파일을 지정된 카테고리 폴더로 이동
     */
    suspend fun moveFileToCategory(fileItem: FileItem, category: FileCategory): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = fileItem.file

            // 카테고리별 대상 디렉토리 결정
            val targetDirectory = getCategoryDirectory(category)
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs()
            }

            // 대상 파일 경로
            val targetFile = File(targetDirectory, fileItem.name)

            // 파일명 중복 처리
            val finalTargetFile = if (targetFile.exists()) {
                getUniqueFile(targetDirectory, fileItem.name)
            } else {
                targetFile
            }

            // 파일 이동
            val success = sourceFile.renameTo(finalTargetFile)

            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("파일 이동 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 카테고리에 해당하는 디렉토리 반환
     */
    private fun getCategoryDirectory(category: FileCategory): File {
        val baseDir = Environment.getExternalStorageDirectory()
        return when (category) {
            FileCategory.IMAGES, FileCategory.SCREENSHOTS, FileCategory.MEMES ->
                File(baseDir, "Pictures/${category.displayName}")
            FileCategory.VIDEOS ->
                File(baseDir, "Movies/${category.displayName}")
            FileCategory.DOCUMENTS, FileCategory.WORK ->
                File(baseDir, "Documents/${category.displayName}")
            FileCategory.DOWNLOADS ->
                File(baseDir, "Download/${category.displayName}")
            else ->
                File(baseDir, "SmartFolder/${category.displayName}")
        }
    }

    /**
     * 중복 파일명 처리
     */
    private fun getUniqueFile(directory: File, fileName: String): File {
        val nameWithoutExt = fileName.substringBeforeLast(".")
        val extension = fileName.substringAfterLast(".", "")
        var counter = 1
        var newFile: File

        do {
            val newName = if (extension.isNotEmpty()) {
                "${nameWithoutExt}_$counter.$extension"
            } else {
                "${nameWithoutExt}_$counter"
            }
            newFile = File(directory, newName)
            counter++
        } while (newFile.exists())

        return newFile
    }

    /**
     * 모든 사용자 선호도 가져오기
     */
    fun getAllUserPreferences(): Flow<List<UserPreference>> {
        return userPreferenceDao.getAllPreferences()
    }

    /**
     * 학습 데이터 개수 가져오기
     */
    suspend fun getUserPreferenceCount(): Int = withContext(Dispatchers.IO) {
        userPreferenceDao.getPreferenceCount()
    }

    /**
     * 파일 삭제 (Scoped Storage 지원)
     */
    suspend fun deleteFile(filePath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)

            if (!file.exists()) {
                return@withContext Result.failure(Exception("파일이 존재하지 않습니다"))
            }

            val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 이상: MediaStore API 사용
                deleteFileUsingMediaStore(filePath)
            } else {
                // Android 9 이하: 직접 삭제
                deleteFileDirect(filePath)
            }

            if (deleted) {
                Result.success(true)
            } else {
                Result.failure(Exception("파일 삭제 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 여러 파일 일괄 삭제 (Scoped Storage 지원)
     */
    suspend fun deleteFiles(filePaths: List<String>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var deletedCount = 0

            filePaths.forEach { path ->
                android.util.Log.d("FileRepository", "Attempting to delete: $path")

                val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10 이상: MediaStore API 사용
                    deleteFileUsingMediaStore(path)
                } else {
                    // Android 9 이하: 직접 삭제
                    deleteFileDirect(path)
                }

                android.util.Log.d("FileRepository", "Delete result for $path: $deleted")
                if (deleted) {
                    deletedCount++
                }
            }

            android.util.Log.d("FileRepository", "Total deleted: $deletedCount out of ${filePaths.size}")
            Result.success(deletedCount)
        } catch (e: Exception) {
            android.util.Log.e("FileRepository", "Error deleting files", e)
            Result.failure(e)
        }
    }

    /**
     * MediaStore를 사용한 파일 삭제 (Android 10+)
     */
    private fun deleteFileUsingMediaStore(filePath: String): Boolean {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                android.util.Log.w("FileRepository", "File does not exist: $filePath")
                return false
            }

            // MediaStore에서 파일 URI 찾기
            val uri = getMediaStoreUri(filePath)

            return if (uri != null) {
                android.util.Log.d("FileRepository", "Found MediaStore URI: $uri")
                try {
                    val deleted = context.contentResolver.delete(uri, null, null)
                    android.util.Log.d("FileRepository", "MediaStore delete count: $deleted")
                    deleted > 0
                } catch (securityException: SecurityException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Android 10 이상: RecoverableSecurityException 처리
                        // 사용자 앱이 파일을 만들지 않은 경우 권한 요청 필요
                        android.util.Log.w("FileRepository", "SecurityException - File not owned by app, trying direct delete")
                    }
                    // 직접 삭제 시도
                    deleteFileDirect(filePath)
                }
            } else {
                // MediaStore에 없는 파일은 직접 삭제 시도
                android.util.Log.w("FileRepository", "File not found in MediaStore, trying direct delete")
                deleteFileDirect(filePath)
            }
        } catch (e: Exception) {
            android.util.Log.e("FileRepository", "Error deleting file using MediaStore: $filePath", e)
            // 실패 시 직접 삭제 시도
            return deleteFileDirect(filePath)
        }
    }

    /**
     * 파일 경로로 MediaStore URI 찾기
     */
    private fun getMediaStoreUri(filePath: String): Uri? {
        try {
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DATA}=?"
            val selectionArgs = arrayOf(filePath)

            // 파일 타입에 따라 적절한 MediaStore 컬렉션 선택
            val collection = when {
                filePath.contains("/Pictures/", ignoreCase = true) ||
                filePath.contains("/DCIM/", ignoreCase = true) ||
                filePath.contains("/Screenshots/", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                }
                filePath.contains("/Movies/", ignoreCase = true) ||
                filePath.contains("/Video/", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                }
                filePath.contains("/Music/", ignoreCase = true) ||
                filePath.contains("/Audio/", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    } else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                filePath.contains("/Download/", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    } else {
                        return null
                    }
                }
                else -> {
                    // 일반 파일: Files 컬렉션
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    } else {
                        MediaStore.Files.getContentUri("external")
                    }
                }
            }

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val id = cursor.getLong(idColumn)
                    return ContentUris.withAppendedId(collection, id)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FileRepository", "Error getting MediaStore URI for: $filePath", e)
        }
        return null
    }

    /**
     * 직접 파일 삭제 (Android 9 이하 또는 fallback)
     */
    private fun deleteFileDirect(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                android.util.Log.w("FileRepository", "File does not exist: $filePath")
                return false
            }

            android.util.Log.d("FileRepository", "Attempting direct delete: ${file.absolutePath}")
            android.util.Log.d("FileRepository", "File exists: ${file.exists()}, canWrite: ${file.canWrite()}, canRead: ${file.canRead()}")
            android.util.Log.d("FileRepository", "Parent directory writable: ${file.parentFile?.canWrite()}")

            // 파일 삭제 시도
            val deleted = file.delete()

            if (deleted) {
                android.util.Log.d("FileRepository", "Direct delete SUCCESS: $filePath")

                // MediaStore 동기화 (파일 시스템과 MediaStore 일치시키기)
                try {
                    context.contentResolver.delete(
                        MediaStore.Files.getContentUri("external"),
                        "${MediaStore.MediaColumns.DATA}=?",
                        arrayOf(filePath)
                    )
                } catch (e: Exception) {
                    android.util.Log.w("FileRepository", "Failed to sync with MediaStore after delete", e)
                }
            } else {
                android.util.Log.e("FileRepository", "Direct delete FAILED: $filePath")
            }

            deleted
        } catch (e: Exception) {
            android.util.Log.e("FileRepository", "Error in direct delete: $filePath", e)
            false
        }
    }

    /**
     * 파일 통계 계산
     */
    suspend fun calculateStatistics(files: List<FileItem>): FileStatistics = withContext(Dispatchers.IO) {
        val totalFiles = files.size
        val totalSize = files.sumOf { it.size }

        // 카테고리별 분류 (suggestedCategory가 있는 파일만)
        val categorizedFiles = files.filter { it.suggestedCategory != null }
        val categoryMap = categorizedFiles.groupBy { it.suggestedCategory!! }

        val categoryBreakdown = categoryMap.mapValues { (category, categoryFiles) ->
            val categorySize = categoryFiles.sumOf { it.size }
            CategoryStats(
                category = category,
                fileCount = categoryFiles.size,
                totalSize = categorySize,
                percentage = if (totalFiles > 0) categoryFiles.size.toFloat() / totalFiles * 100 else 0f
            )
        }

        android.util.Log.d("FileRepository", "Statistics calculated: total=${totalFiles}, categorized=${categorizedFiles.size}, categories=${categoryBreakdown.size}")

        // 최대 파일 (상위 10개)
        val largestFiles = files
            .sortedByDescending { it.size }
            .take(10)

        // 가장 오래된 파일 (상위 10개)
        val oldestFiles = files
            .sortedBy { it.file.lastModified() }
            .take(10)

        // 최근 파일 (상위 10개)
        val recentFiles = files
            .sortedByDescending { it.file.lastModified() }
            .take(10)

        FileStatistics(
            totalFiles = totalFiles,
            totalSize = totalSize,
            categoryBreakdown = categoryBreakdown,
            largestFiles = largestFiles,
            oldestFiles = oldestFiles,
            recentFiles = recentFiles
        )
    }

    /**
     * 중복 파일 탐지
     */
    suspend fun findDuplicateFiles(files: List<FileItem>): List<DuplicateFileGroup> = withContext(Dispatchers.IO) {
        android.util.Log.d("FileRepository", "Starting duplicate file detection for ${files.size} files")

        // 파일 해시 계산
        val fileHashes = mutableMapOf<String, MutableList<FileItem>>()

        files.forEach { fileItem ->
            try {
                val hash = calculateMD5(fileItem.file)
                if (hash != null) {
                    fileHashes.getOrPut(hash) { mutableListOf() }.add(fileItem)
                }
            } catch (e: Exception) {
                android.util.Log.w("FileRepository", "Failed to calculate hash for: ${fileItem.path}", e)
            }
        }

        // 중복 파일 그룹 생성 (2개 이상인 것만)
        val duplicateGroups = fileHashes
            .filter { it.value.size > 1 }
            .map { (hash, duplicateFiles) ->
                DuplicateFileGroup(
                    hash = hash,
                    files = duplicateFiles
                )
            }
            .sortedByDescending { it.wastedSpace }

        android.util.Log.d("FileRepository", "Found ${duplicateGroups.size} duplicate groups")
        duplicateGroups
    }

    /**
     * 파일의 MD5 해시 계산
     */
    private fun calculateMD5(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            android.util.Log.e("FileRepository", "Error calculating MD5 for: ${file.path}", e)
            null
        }
    }
}

/**
 * 스캔할 디렉토리 타입
 */
enum class DirectoryType {
    ALL_STORAGE,  // 전체 저장소
    DOWNLOADS,
    PICTURES,
    DOCUMENTS,
    DCIM
}
