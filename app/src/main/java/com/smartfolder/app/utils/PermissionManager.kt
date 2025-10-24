package com.smartfolder.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 파일 접근 권한 관리 유틸리티
 */
object PermissionManager {

    private const val REQUEST_CODE_PERMISSIONS = 100
    private const val REQUEST_CODE_MANAGE_STORAGE = 101

    /**
     * 필요한 권한 목록
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        // 파일 접근 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상
            permissions.addAll(listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11, 12
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // Android 10 이하
            permissions.addAll(listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }

        // 알림 권한 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.toTypedArray()
    }

    /**
     * 모든 권한이 허용되었는지 확인
     */
    fun hasAllPermissions(context: Context): Boolean {
        // Android 11 이상에서는 MANAGE_EXTERNAL_STORAGE 권한도 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                return false
            }
        }

        // 일반 권한 확인
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 권한 요청
     */
    fun requestPermissions(activity: Activity) {
        // Android 11 이상에서는 MANAGE_EXTERNAL_STORAGE 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageStoragePermission(activity)
                return
            }
        }

        // 일반 권한 요청
        val permissions = getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    /**
     * MANAGE_EXTERNAL_STORAGE 권한 요청 (Android 11+)
     */
    private fun requestManageStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
            }
        }
    }

    /**
     * 권한 요청 결과 처리
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        @Suppress("UNUSED_PARAMETER") permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            return grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
        return false
    }

    /**
     * 권한 거부 시 설정 화면으로 이동
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }

    /**
     * 권한 요청이 필요한지 확인
     */
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return getRequiredPermissions().any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    /**
     * 요청할 권한 목록 반환 (최신 Activity Result API용)
     */
    fun getPermissionsToRequest(context: Context): Array<String> {
        return getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    /**
     * 권한 결과 처리 (최신 Activity Result API용)
     */
    fun handlePermissionResults(permissions: Map<String, Boolean>) {
        // 권한 결과 로그 또는 처리
        permissions.forEach { (permission, granted) ->
            if (!granted) {
                // 권한이 거부된 경우 처리
                println("Permission denied: $permission")
            }
        }
    }
}
