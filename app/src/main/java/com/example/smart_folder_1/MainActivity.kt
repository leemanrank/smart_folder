package com.example.smart_folder_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.smart_folder_1.ads.AdManager
import com.example.smart_folder_1.notification.NotificationScheduler
import com.example.smart_folder_1.ui.screens.MainScreen
import com.example.smart_folder_1.ui.screens.OnboardingScreen
import com.example.smart_folder_1.ui.theme.SmartFolderTheme
import com.example.smart_folder_1.ui.viewmodel.MainViewModel
import com.example.smart_folder_1.utils.PermissionManager
import com.example.smart_folder_1.utils.PreferenceManager
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        PermissionManager.handlePermissionResults(permissions)
    }

    private val manageStorageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // MANAGE_EXTERNAL_STORAGE 권한 요청 결과 처리
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (android.os.Environment.isExternalStorageManager()) {
                android.util.Log.d("MainActivity", "MANAGE_EXTERNAL_STORAGE granted")
                // 권한 획득 후 다시 일반 권한 요청
                requestNormalPermissions()
            } else {
                android.util.Log.w("MainActivity", "MANAGE_EXTERNAL_STORAGE denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AdMob 초기화
        AdManager.initialize(this)

        // FCM 토큰 가져오기
        initializeFCM()

        // 알림 스케줄 시작
        NotificationScheduler.scheduleFileCheckNotification(this, repeatIntervalHours = 24)

        // 전면 광고 미리 로드
        AdManager.loadInterstitialAd(this)

        // 보상형 광고 미리 로드
        AdManager.loadRewardedAd(this)

        setContent {
            SmartFolderTheme {
                var showExitDialog by remember { mutableStateOf(false) }
                var showOnboarding by remember { mutableStateOf(!PreferenceManager.isOnboardingCompleted(this)) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showOnboarding) {
                        // 온보딩 화면 표시
                        OnboardingScreen(
                            onFinish = {
                                PreferenceManager.setOnboardingCompleted(this@MainActivity)
                                PreferenceManager.setFirstLaunchCompleted(this@MainActivity)
                                showOnboarding = false
                            }
                        )
                    } else {
                        // 메인 화면 표시
                        MainScreen(
                            viewModel = viewModel,
                            onRequestPermissions = {
                                requestPermissions()
                            },
                            onBackPressed = {
                                // 메인 화면에서 뒤로가기 시 종료 다이얼로그 표시
                                showExitDialog = true
                            }
                        )
                    }
                }

                // 종료 확인 다이얼로그
                if (showExitDialog) {
                    ExitConfirmDialog(
                        onConfirm = {
                            showExitDialog = false
                            finish()
                        },
                        onDismiss = {
                            showExitDialog = false
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 권한 상태 확인
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasAllPermissions(this)) {
            // 권한이 없으면 자동으로 요청하지 않음 (사용자가 버튼을 눌러야 함)
        }
    }

    private fun requestPermissions() {
        // Android 11 이상에서는 MANAGE_EXTERNAL_STORAGE 권한 먼저 요청
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                requestManageStoragePermission()
                return
            }
        }

        // 일반 권한 요청
        requestNormalPermissions()
    }

    private fun requestManageStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:$packageName")
                manageStorageActivityResult.launch(intent)
            } catch (e: Exception) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageActivityResult.launch(intent)
            }
        }
    }

    private fun requestNormalPermissions() {
        val permissionsToRequest = PermissionManager.getPermissionsToRequest(this)
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    /**
     * FCM 초기화 및 토큰 가져오기
     */
    private fun initializeFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("MainActivity", "FCM token fetch failed", task.exception)
                return@addOnCompleteListener
            }

            // FCM 토큰 가져오기 성공
            val token = task.result
            android.util.Log.d("MainActivity", "FCM Token: $token")

            // TODO: 서버에 토큰 전송 (추후 백엔드 구현 시)
        }
    }
}

@Composable
fun ExitConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = "앱 종료")
        },
        text = {
            Text(text = "Smart Folder 앱을 종료하시겠습니까?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("종료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }

        }
    )
}