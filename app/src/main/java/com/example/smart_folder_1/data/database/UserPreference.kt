package com.example.smart_folder_1.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smart_folder_1.data.model.FileCategory

/**
 * 사용자 학습 데이터 저장용 엔티티
 * 사용자가 특정 파일을 어떤 카테고리로 분류했는지 기록
 */
@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val fileExtension: String,
    val fileSize: Long,
    val filePath: String,
    val selectedCategory: String, // FileCategory enum을 String으로 저장
    val timestamp: Long = System.currentTimeMillis(),
    val isManualOverride: Boolean = false // 사용자가 AI 추천을 거부하고 직접 선택한 경우
)
