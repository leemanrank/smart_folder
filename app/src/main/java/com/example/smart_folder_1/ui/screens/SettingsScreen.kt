package com.example.smart_folder_1.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_folder_1.notification.NotificationScheduler

/**
 * 설정 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    var notificationEnabled by remember { mutableStateOf(true) }
    var notificationInterval by remember { mutableStateOf(24) } // 기본 24시간
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 알림 설정 섹션
            item {
                Text(
                    text = "알림",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 알림 활성화/비활성화
            item {
                SettingItem(
                    title = "파일 정리 알림",
                    description = "파일이 쌓였을 때 알림을 받습니다",
                    icon = Icons.Default.Notifications,
                    trailing = {
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { enabled ->
                                notificationEnabled = enabled
                                if (enabled) {
                                    NotificationScheduler.scheduleFileCheckNotification(
                                        context,
                                        notificationInterval.toLong()
                                    )
                                } else {
                                    NotificationScheduler.cancelFileCheckNotification(context)
                                }
                            }
                        )
                    }
                )
            }

            // 알림 주기 설정
            item {
                SettingItem(
                    title = "알림 주기",
                    description = "${notificationInterval}시간마다",
                    icon = Icons.Default.Schedule,
                    enabled = notificationEnabled,
                    onClick = { showIntervalDialog = true }
                )
            }

            // 즉시 체크 버튼 (테스트용)
            item {
                SettingItem(
                    title = "지금 파일 체크",
                    description = "즉시 파일을 체크하고 알림을 받습니다",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        NotificationScheduler.runFileCheckNow(context)
                    }
                )
            }

            // 일반 설정 섹션
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "일반",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 개인정보 처리방침
            item {
                SettingItem(
                    title = "개인정보 처리방침",
                    description = "개인정보 수집 및 이용 안내",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { showPrivacyPolicy = true }
                )
            }

            // 앱 정보
            item {
                SettingItem(
                    title = "앱 버전",
                    description = "1.0.0",
                    icon = Icons.Default.Info
                )
            }
        }

        // 개인정보 처리방침 화면
        if (showPrivacyPolicy) {
            PrivacyPolicyScreen(
                onBackPressed = { showPrivacyPolicy = false }
            )
        }

        // 알림 주기 선택 다이얼로그
        if (showIntervalDialog) {
            AlertDialog(
                onDismissRequest = { showIntervalDialog = false },
                title = { Text("알림 주기 선택") },
                text = {
                    Column {
                        listOf(
                            6 to "6시간마다",
                            12 to "12시간마다",
                            24 to "24시간마다 (권장)",
                            48 to "48시간마다",
                            72 to "72시간마다"
                        ).forEach { (hours, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        notificationInterval = hours
                                        NotificationScheduler.scheduleFileCheckNotification(
                                            context,
                                            hours.toLong()
                                        )
                                        showIntervalDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = notificationInterval == hours,
                                    onClick = {
                                        notificationInterval = hours
                                        NotificationScheduler.scheduleFileCheckNotification(
                                            context,
                                            hours.toLong()
                                        )
                                        showIntervalDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showIntervalDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
