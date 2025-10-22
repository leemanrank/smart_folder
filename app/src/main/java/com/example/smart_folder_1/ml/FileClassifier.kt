package com.example.smart_folder_1.ml

import com.example.smart_folder_1.data.database.UserPreference
import com.example.smart_folder_1.data.model.FileCategory
import com.example.smart_folder_1.data.model.FileItem
import java.io.File

/**
 * 파일 분류기 - 사용자 학습 데이터 기반 파일 분류
 */
class FileClassifier {

    /**
     * 파일을 분석하여 카테고리를 추천
     * 1. 파일 확장자 기반 기본 분류
     * 2. 사용자 학습 데이터 기반 스마트 분류
     * 3. 파일명 패턴 분석
     */
    fun classifyFile(file: FileItem, userPreferences: List<UserPreference>): Pair<FileCategory, Float> {
        // 1단계: 확장자 기반 기본 카테고리
        val baseCategory = FileCategory.fromExtension(file.extension)

        // 2단계: 사용자 학습 데이터 분석
        val learningScore = analyzeUserPreferences(file, userPreferences)

        // 3단계: 파일명 패턴 분석
        val patternScore = analyzeFileNamePattern(file)

        // 최종 카테고리 결정 (학습 데이터 우선)
        val finalCategory = learningScore.first ?: patternScore.first ?: baseCategory
        val confidence = maxOf(learningScore.second, patternScore.second)

        return Pair(finalCategory, confidence)
    }

    /**
     * 사용자 학습 데이터 분석
     */
    private fun analyzeUserPreferences(
        file: FileItem,
        preferences: List<UserPreference>
    ): Pair<FileCategory?, Float> {
        if (preferences.isEmpty()) return Pair(null, 0f)

        // 같은 확장자를 가진 파일들의 사용자 선택 패턴 분석
        val sameExtensionPrefs = preferences.filter {
            it.fileExtension.equals(file.extension, ignoreCase = true)
        }

        if (sameExtensionPrefs.isEmpty()) {
            // 파일명 유사도 기반 분석
            return analyzeByFileName(file, preferences)
        }

        // 가장 많이 선택된 카테고리 찾기
        val categoryFrequency = sameExtensionPrefs
            .groupBy { it.selectedCategory }
            .mapValues { it.value.size }

        val mostFrequentCategory = categoryFrequency.maxByOrNull { it.value }?.key
        val confidence = (categoryFrequency[mostFrequentCategory] ?: 0).toFloat() /
                        sameExtensionPrefs.size

        return if (mostFrequentCategory != null && confidence > 0.5f) {
            try {
                Pair(FileCategory.valueOf(mostFrequentCategory), confidence)
            } catch (e: IllegalArgumentException) {
                Pair(null, 0f)
            }
        } else {
            Pair(null, 0f)
        }
    }

    /**
     * 파일명 유사도 기반 분석
     */
    private fun analyzeByFileName(
        file: FileItem,
        preferences: List<UserPreference>
    ): Pair<FileCategory?, Float> {
        val fileName = file.name.lowercase()

        // 파일명에 포함된 키워드로 유사한 파일 찾기
        val keywords = extractKeywords(fileName)
        if (keywords.isEmpty()) return Pair(null, 0f)

        val similarFiles = preferences.filter { pref ->
            val prefKeywords = extractKeywords(pref.fileName.lowercase())
            keywords.any { it in prefKeywords }
        }

        if (similarFiles.isEmpty()) return Pair(null, 0f)

        val categoryFrequency = similarFiles
            .groupBy { it.selectedCategory }
            .mapValues { it.value.size }

        val mostFrequentCategory = categoryFrequency.maxByOrNull { it.value }?.key
        val confidence = (categoryFrequency[mostFrequentCategory] ?: 0).toFloat() /
                        similarFiles.size * 0.7f // 유사도 기반이므로 신뢰도를 낮춤

        return if (mostFrequentCategory != null && confidence > 0.4f) {
            try {
                Pair(FileCategory.valueOf(mostFrequentCategory), confidence)
            } catch (e: IllegalArgumentException) {
                Pair(null, 0f)
            }
        } else {
            Pair(null, 0f)
        }
    }

    /**
     * 파일명 패턴 분석 (스크린샷, 다운로드 등)
     */
    private fun analyzeFileNamePattern(file: FileItem): Pair<FileCategory?, Float> {
        val fileName = file.name.lowercase()

        return when {
            // 스크린샷 패턴
            fileName.contains("screenshot") ||
            fileName.contains("스크린샷") ||
            fileName.startsWith("screenshot_") ||
            fileName.matches(Regex(".*\\d{8}_\\d{6}.*")) -> // 날짜시간 패턴
                Pair(FileCategory.SCREENSHOTS, 0.9f)

            // 다운로드 패턴
            fileName.contains("download") ||
            fileName.contains("다운로드") ->
                Pair(FileCategory.DOWNLOADS, 0.8f)

            // 밈/짤 패턴
            fileName.contains("meme") ||
            fileName.contains("짤") ||
            fileName.contains("funny") ->
                Pair(FileCategory.MEMES, 0.85f)

            // 업무 관련 키워드
            fileName.contains("report") ||
            fileName.contains("meeting") ||
            fileName.contains("보고서") ||
            fileName.contains("회의") ||
            fileName.contains("업무") ->
                Pair(FileCategory.WORK, 0.85f)

            // 중요 파일 패턴
            fileName.contains("important") ||
            fileName.contains("urgent") ||
            fileName.contains("중요") ->
                Pair(FileCategory.IMPORTANT, 0.9f)

            else -> Pair(null, 0f)
        }
    }

    /**
     * 파일명에서 키워드 추출
     */
    private fun extractKeywords(fileName: String): List<String> {
        // 특수문자와 숫자 제거, 공백으로 분리
        return fileName
            .replace(Regex("[^a-zA-Z가-힣\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 2 } // 2글자 이상만
            .distinct()
    }

    /**
     * 여러 파일을 일괄 분류 (최적화됨)
     */
    fun classifyFiles(
        files: List<FileItem>,
        userPreferences: List<UserPreference>
    ): List<FileItem> {
        // 성능 최적화: 확장자별로 미리 그룹화
        val extensionCategoryMap = buildExtensionCategoryMap(userPreferences)
        val keywordCategoryMap = buildKeywordCategoryMap(userPreferences)

        return files.map { file ->
            val (category, confidence) = classifyFileOptimized(
                file,
                extensionCategoryMap,
                keywordCategoryMap
            )
            file.copy(
                suggestedCategory = category,
                confidence = confidence
            )
        }
    }

    /**
     * 확장자별 카테고리 맵 생성 (한 번만 계산)
     */
    private fun buildExtensionCategoryMap(
        preferences: List<UserPreference>
    ): Map<String, Pair<FileCategory, Float>> {
        return preferences
            .groupBy { it.fileExtension.lowercase() }
            .mapValues { (_, prefs) ->
                val categoryFrequency = prefs
                    .groupBy { it.selectedCategory }
                    .mapValues { it.value.size }

                val mostFrequent = categoryFrequency.maxByOrNull { it.value }
                val category = mostFrequent?.key
                val confidence = (mostFrequent?.value ?: 0).toFloat() / prefs.size

                if (category != null && confidence > 0.5f) {
                    try {
                        Pair(FileCategory.valueOf(category), confidence)
                    } catch (e: IllegalArgumentException) {
                        Pair(FileCategory.OTHERS, 0f)
                    }
                } else {
                    Pair(FileCategory.OTHERS, 0f)
                }
            }
            .filterValues { it.second > 0f }
    }

    /**
     * 키워드별 카테고리 맵 생성 (한 번만 계산)
     */
    private fun buildKeywordCategoryMap(
        preferences: List<UserPreference>
    ): Map<String, MutableMap<String, Int>> {
        val keywordMap = mutableMapOf<String, MutableMap<String, Int>>()

        preferences.forEach { pref ->
            val keywords = extractKeywords(pref.fileName.lowercase())
            keywords.forEach { keyword ->
                keywordMap.getOrPut(keyword) { mutableMapOf() }
                    .merge(pref.selectedCategory, 1, Int::plus)
            }
        }

        return keywordMap
    }

    /**
     * 최적화된 파일 분류 (사전 계산된 맵 사용)
     */
    private fun classifyFileOptimized(
        file: FileItem,
        extensionMap: Map<String, Pair<FileCategory, Float>>,
        keywordMap: Map<String, MutableMap<String, Int>>
    ): Pair<FileCategory, Float> {
        // 1단계: 확장자 기반 기본 카테고리
        val baseCategory = FileCategory.fromExtension(file.extension)

        // 2단계: 사전 계산된 확장자 맵에서 조회
        val extensionResult = extensionMap[file.extension.lowercase()]
        if (extensionResult != null && extensionResult.second > 0.5f) {
            return extensionResult
        }

        // 3단계: 파일명 패턴 분석 (빠른 우선 체크)
        val patternResult = analyzeFileNamePattern(file)
        if (patternResult.first != null && patternResult.second >= 0.8f) {
            return Pair(patternResult.first!!, patternResult.second)
        }

        // 4단계: 키워드 기반 분석 (사전 계산된 맵 사용)
        val keywordResult = analyzeByKeywordMap(file, keywordMap)
        if (keywordResult.first != null && keywordResult.second > patternResult.second) {
            return Pair(keywordResult.first!!, keywordResult.second)
        }

        // 5단계: 패턴 결과나 기본 카테고리 반환
        return if (patternResult.first != null) {
            Pair(patternResult.first!!, patternResult.second)
        } else {
            Pair(baseCategory, 0.6f)
        }
    }

    /**
     * 사전 계산된 키워드 맵으로 빠른 분석
     */
    private fun analyzeByKeywordMap(
        file: FileItem,
        keywordMap: Map<String, MutableMap<String, Int>>
    ): Pair<FileCategory?, Float> {
        val keywords = extractKeywords(file.name.lowercase())
        if (keywords.isEmpty()) return Pair(null, 0f)

        val categoryScores = mutableMapOf<String, Int>()

        keywords.forEach { keyword ->
            keywordMap[keyword]?.forEach { (category, count) ->
                categoryScores.merge(category, count, Int::plus)
            }
        }

        if (categoryScores.isEmpty()) return Pair(null, 0f)

        val bestMatch = categoryScores.maxByOrNull { it.value }
        val totalScore = categoryScores.values.sum()
        val confidence = (bestMatch?.value ?: 0).toFloat() / totalScore * 0.7f

        return if (bestMatch != null && confidence > 0.4f) {
            try {
                Pair(FileCategory.valueOf(bestMatch.key), confidence)
            } catch (e: IllegalArgumentException) {
                Pair(null, 0f)
            }
        } else {
            Pair(null, 0f)
        }
    }
}
