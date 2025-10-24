package com.smartfolder.app.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences 관리 유틸리티
 */
object PreferenceManager {

    private const val PREF_NAME = "smart_folder_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 첫 실행 여부 확인
     */
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    /**
     * 첫 실행 플래그 설정
     */
    fun setFirstLaunchCompleted(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_FIRST_LAUNCH, false)
            apply()
        }
    }

    /**
     * 온보딩 완료 여부 확인
     */
    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * 온보딩 완료 설정
     */
    fun setOnboardingCompleted(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_ONBOARDING_COMPLETED, true)
            apply()
        }
    }

    /**
     * 온보딩 초기화 (테스트용)
     */
    fun resetOnboarding(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_ONBOARDING_COMPLETED, false)
            putBoolean(KEY_FIRST_LAUNCH, true)
            apply()
        }
    }
}
