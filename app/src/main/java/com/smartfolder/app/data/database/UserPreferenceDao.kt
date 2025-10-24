package com.smartfolder.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 선호도 데이터 접근 객체
 */
@Dao
interface UserPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: UserPreference)

    @Query("SELECT * FROM user_preferences ORDER BY timestamp DESC")
    fun getAllPreferences(): Flow<List<UserPreference>>

    @Query("SELECT * FROM user_preferences ORDER BY timestamp DESC")
    suspend fun getAllPreferencesList(): List<UserPreference>

    @Query("SELECT * FROM user_preferences WHERE fileExtension = :extension ORDER BY timestamp DESC LIMIT 10")
    suspend fun getPreferencesByExtension(extension: String): List<UserPreference>

    @Query("SELECT * FROM user_preferences WHERE fileName LIKE '%' || :keyword || '%' ORDER BY timestamp DESC LIMIT 10")
    suspend fun getPreferencesByKeyword(keyword: String): List<UserPreference>

    @Query("DELETE FROM user_preferences WHERE timestamp < :timestamp")
    suspend fun deleteOldPreferences(timestamp: Long)

    @Query("SELECT COUNT(*) FROM user_preferences")
    suspend fun getPreferenceCount(): Int
}
