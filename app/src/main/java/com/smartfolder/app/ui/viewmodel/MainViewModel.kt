package com.smartfolder.app.ui.viewmodel

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartfolder.app.ads.AdManager
import com.smartfolder.app.data.model.DuplicateFileGroup
import com.smartfolder.app.data.model.FileCategory
import com.smartfolder.app.data.model.FileItem
import com.smartfolder.app.data.model.FileStatistics
import com.smartfolder.app.data.repository.DirectoryType
import com.smartfolder.app.data.repository.FileRepository
import com.smartfolder.app.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 메인 화면 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FileRepository(application)

    // UI 상태
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 스캔된 파일 목록
    private val _scannedFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val scannedFiles: StateFlow<List<FileItem>> = _scannedFiles.asStateFlow()

    // 분류된 파일 목록 (카테고리별)
    private val _categorizedFiles = MutableStateFlow<Map<FileCategory, List<FileItem>>>(emptyMap())
    val categorizedFiles: StateFlow<Map<FileCategory, List<FileItem>>> = _categorizedFiles.asStateFlow()

    // 학습 데이터 개수
    private val _learningDataCount = MutableStateFlow(0)
    val learningDataCount: StateFlow<Int> = _learningDataCount.asStateFlow()

    // 선택된 디렉토리
    private val _selectedDirectory = MutableStateFlow(DirectoryType.ALL_STORAGE)
    val selectedDirectory: StateFlow<DirectoryType> = _selectedDirectory.asStateFlow()

    // 선택된 파일들 (다중 선택)
    private val _selectedFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedFiles: StateFlow<Set<String>> = _selectedFiles.asStateFlow()

    // 선택 모드 활성화 여부
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // 파일 통계
    private val _statistics = MutableStateFlow<FileStatistics?>(null)
    val statistics: StateFlow<FileStatistics?> = _statistics.asStateFlow()

    // 중복 파일 그룹
    private val _duplicateFileGroups = MutableStateFlow<List<DuplicateFileGroup>>(emptyList())
    val duplicateFileGroups: StateFlow<List<DuplicateFileGroup>> = _duplicateFileGroups.asStateFlow()

    init {
        loadLearningDataCount()
    }

    /**
     * 파일 스캔 시작
     */
    fun scanFiles(directoryType: DirectoryType = DirectoryType.ALL_STORAGE) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading("파일을 스캔하는 중...")
                _selectedDirectory.value = directoryType

                val files = repository.scanFiles(directoryType)
                _scannedFiles.value = files

                if (files.isEmpty()) {
                    _uiState.value = UiState.Empty("스캔된 파일이 없습니다.")
                    _statistics.value = null
                } else {
                    // 파일 분류 (내부에서 통계도 계산됨)
                    classifyScannedFiles(files)
                }
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "파일 스캔 실패", e)
                _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }

    /**
     * 스캔된 파일들을 자동 분류
     */
    private suspend fun classifyScannedFiles(files: List<FileItem>) {
        try {
            _uiState.value = UiState.Loading("AI가 파일을 분류하는 중...")

            val classifiedFiles = repository.classifyFiles(files)
            _scannedFiles.value = classifiedFiles

            // 카테고리별로 그룹화
            val grouped = classifiedFiles
                .filter { it.suggestedCategory != null }
                .groupBy { it.suggestedCategory!! }

            _categorizedFiles.value = grouped

            // 분류 완료 후 통계 다시 계산
            calculateStatistics(classifiedFiles)

            _uiState.value = UiState.Success("${classifiedFiles.size}개 파일 분류 완료")
        } catch (e: Exception) {
            ErrorHandler.logError("MainViewModel", "파일 분류 실패", e)
            _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
        }
    }

    /**
     * 사용자가 파일의 카테고리를 선택
     */
    fun onCategorySelected(fileItem: FileItem, category: FileCategory) {
        viewModelScope.launch {
            try {
                // AI 추천과 다른 선택인지 확인
                val isManualOverride = fileItem.suggestedCategory != category

                // 학습 데이터 저장
                repository.saveUserPreference(fileItem, category, isManualOverride)

                // 학습 데이터 개수 업데이트
                loadLearningDataCount()

                // 파일 이동
                moveFile(fileItem, category)
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "카테고리 선택 실패", e)
                _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }

    /**
     * 파일을 카테고리 폴더로 이동
     */
    private suspend fun moveFile(fileItem: FileItem, category: FileCategory) {
        try {
            _uiState.value = UiState.Loading("파일을 이동하는 중...")

            val result = repository.moveFileToCategory(fileItem, category)

            if (result.isSuccess) {
                // 파일 목록에서 제거
                _scannedFiles.value = _scannedFiles.value.filter { it.path != fileItem.path }

                // 카테고리별 목록도 업데이트
                updateCategorizedFiles()

                // 통계 다시 계산
                calculateStatistics(_scannedFiles.value)

                _uiState.value = UiState.Success("${fileItem.name}을(를) ${category.displayName} 폴더로 이동했습니다.")
            } else {
                _uiState.value = UiState.Error("파일 이동 실패: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ErrorHandler.logError("MainViewModel", "파일 이동 실패", e)
            _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
        }
    }

    /**
     * 모든 파일을 자동으로 분류 및 이동
     */
    fun autoOrganizeAll(activity: ComponentActivity? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading("모든 파일을 자동으로 정리하는 중...")

                var movedCount = 0
                val filesToMove = _scannedFiles.value.filter { it.suggestedCategory != null }

                for (file in filesToMove) {
                    val category = file.suggestedCategory ?: continue

                    // 학습 데이터 저장
                    repository.saveUserPreference(file, category, isManualOverride = false)

                    // 파일 이동
                    val result = repository.moveFileToCategory(file, category)
                    if (result.isSuccess) {
                        movedCount++
                    }
                }

                // 목록 갱신
                scanFiles(_selectedDirectory.value)

                _uiState.value = UiState.Success("${movedCount}개 파일을 자동으로 정리했습니다.")

                // 전면 광고 표시 (10개 이상 정리했을 때)
                if (movedCount >= 10 && activity != null) {
                    AdManager.showInterstitialAd(activity)
                }
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "자동 정리 실패", e)
                _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }

    /**
     * 카테고리별 파일 목록 업데이트
     */
    private fun updateCategorizedFiles() {
        val grouped = _scannedFiles.value
            .filter { it.suggestedCategory != null }
            .groupBy { it.suggestedCategory!! }

        _categorizedFiles.value = grouped
    }

    /**
     * 학습 데이터 개수 로드
     */
    private fun loadLearningDataCount() {
        viewModelScope.launch {
            try {
                val count = repository.getUserPreferenceCount()
                _learningDataCount.value = count
            } catch (e: Exception) {
                // 조용히 실패
            }
        }
    }

    /**
     * UI 상태 초기화
     */
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    /**
     * 파일 선택/해제 토글
     */
    fun toggleFileSelection(filePath: String) {
        val currentSelection = _selectedFiles.value.toMutableSet()
        if (currentSelection.contains(filePath)) {
            currentSelection.remove(filePath)
        } else {
            currentSelection.add(filePath)
        }
        _selectedFiles.value = currentSelection

        // 선택된 파일이 없으면 선택 모드 종료
        if (currentSelection.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    /**
     * 선택 모드 시작 (파일 롱클릭 시)
     */
    fun startSelectionMode(filePath: String) {
        _isSelectionMode.value = true
        _selectedFiles.value = setOf(filePath)
    }

    /**
     * 선택 모드 종료
     */
    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedFiles.value = emptySet()
    }

    /**
     * 전체 선택/해제
     */
    fun toggleSelectAll() {
        if (_selectedFiles.value.size == _scannedFiles.value.size) {
            // 전체 선택 상태 → 전체 해제
            _selectedFiles.value = emptySet()
            _isSelectionMode.value = false
        } else {
            // 전체 선택
            _selectedFiles.value = _scannedFiles.value.map { it.path }.toSet()
            _isSelectionMode.value = true
        }
    }

    /**
     * 선택된 파일들 삭제
     */
    fun deleteSelectedFiles() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading("파일을 삭제하는 중...")

                val selectedPaths = _selectedFiles.value.toList()

                // 일괄 삭제 사용
                val result = repository.deleteFiles(selectedPaths)

                if (result.isSuccess) {
                    val deletedCount = result.getOrNull() ?: 0

                    // 삭제된 파일들을 목록에서 제거
                    _scannedFiles.value = _scannedFiles.value.filter {
                        it.path !in selectedPaths
                    }

                    // 카테고리별 목록 업데이트
                    updateCategorizedFiles()

                    // 통계 다시 계산
                    calculateStatistics(_scannedFiles.value)

                    // 선택 모드 종료
                    exitSelectionMode()

                    _uiState.value = UiState.Success("${deletedCount}개 파일을 삭제했습니다.")
                } else {
                    _uiState.value = UiState.Error("파일 삭제 실패: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "파일 삭제 실패", e)
                _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }

    /**
     * 단일 파일 삭제 (통계 화면에서 사용 - 조용히 처리)
     */
    fun deleteSingleFile(filePath: String) {
        viewModelScope.launch {
            try {
                // 단일 파일 삭제 (UiState 변경 없이 조용히 처리)
                val result = repository.deleteFiles(listOf(filePath))

                if (result.isSuccess) {
                    // 삭제된 파일을 목록에서 제거
                    _scannedFiles.value = _scannedFiles.value.filter {
                        it.path != filePath
                    }

                    // 카테고리별 목록 업데이트
                    updateCategorizedFiles()

                    // 통계 다시 계산
                    calculateStatistics(_scannedFiles.value)

                    android.util.Log.d("MainViewModel", "파일 삭제 성공: $filePath")
                } else {
                    android.util.Log.e("MainViewModel", "파일 삭제 실패: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "파일 삭제 실패", e)
            }
        }
    }

    /**
     * 파일 통계 계산
     */
    private suspend fun calculateStatistics(files: List<FileItem>) {
        try {
            android.util.Log.d("MainViewModel", "통계 계산 시작: ${files.size}개 파일")
            val stats = repository.calculateStatistics(files)
            _statistics.value = stats
            android.util.Log.d("MainViewModel", "통계 계산 완료: ${stats.totalFiles}개 파일, ${stats.categoryBreakdown.size}개 카테고리")
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "통계 계산 실패", e)
        }
    }

    /**
     * 중복 파일 탐지
     */
    fun findDuplicates() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading("중복 파일을 검색하는 중...")

                val duplicates = repository.findDuplicateFiles(_scannedFiles.value)
                _duplicateFileGroups.value = duplicates

                if (duplicates.isEmpty()) {
                    _uiState.value = UiState.Success("중복 파일이 없습니다!")
                } else {
                    val totalWasted = duplicates.sumOf { it.wastedSpace }
                    val totalWastedMB = totalWasted / (1024 * 1024)
                    _uiState.value = UiState.Success("${duplicates.size}개 중복 그룹 발견! ${totalWastedMB}MB 절약 가능")
                }
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "중복 파일 검색 실패", e)
                _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }

    /**
     * 중복 파일 그룹에서 선택된 파일 삭제
     */
    fun deleteDuplicatesFromGroup(group: DuplicateFileGroup, filesToKeep: Set<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading("중복 파일을 삭제하는 중...")

                val filesToDelete = group.files.filter { it.path !in filesToKeep }.map { it.path }
                val result = repository.deleteFiles(filesToDelete)

                if (result.isSuccess) {
                    val deletedCount = result.getOrNull() ?: 0

                    // 삭제된 파일들을 목록에서 제거
                    _scannedFiles.value = _scannedFiles.value.filter { it.path !in filesToDelete }

                    // 중복 파일 그룹 다시 검색
                    findDuplicates()

                    _uiState.value = UiState.Success("${deletedCount}개 중복 파일을 삭제했습니다.")
                } else {
                    _uiState.value = UiState.Error("중복 파일 삭제 실패: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                ErrorHandler.logError("MainViewModel", "중복 파일 삭제 실패", e)
                _uiState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }
}

/**
 * UI 상태 sealed class
 */
sealed class UiState {
    object Idle : UiState()
    data class Loading(val message: String) : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    data class Empty(val message: String) : UiState()
}
