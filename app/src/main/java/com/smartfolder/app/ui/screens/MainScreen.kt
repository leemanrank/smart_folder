package com.smartfolder.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfolder.app.MainActivity
import com.smartfolder.app.data.model.FileCategory
import com.smartfolder.app.data.model.FileItem
import com.smartfolder.app.data.repository.DirectoryType
import com.smartfolder.app.ui.components.BannerAdView
import com.smartfolder.app.ui.viewmodel.MainViewModel
import com.smartfolder.app.ui.viewmodel.UiState

/**
 * 메인 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onRequestPermissions: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    val uiState by viewModel.uiState.collectAsState()
    val scannedFiles by viewModel.scannedFiles.collectAsState()
    val categorizedFiles by viewModel.categorizedFiles.collectAsState()
    val learningDataCount by viewModel.learningDataCount.collectAsState()
    val selectedDirectory by viewModel.selectedDirectory.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val duplicateGroups by viewModel.duplicateFileGroups.collectAsState()

    var showDirectoryPicker by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showStatisticsScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showDuplicatesScreen by remember { mutableStateOf(false) }

    // 뒤로가기 처리 - 화면 계층 구조에 따라 처리
    BackHandler {
        when {
            // 1순위: 서브 화면들 닫기
            showStatisticsScreen -> showStatisticsScreen = false
            showSettingsScreen -> showSettingsScreen = false
            showDuplicatesScreen -> showDuplicatesScreen = false
            // 2순위: 다이얼로그 닫기
            showCategoryPicker -> {
                showCategoryPicker = false
                selectedFile = null
            }
            showDeleteConfirmDialog -> showDeleteConfirmDialog = false
            showDirectoryPicker -> showDirectoryPicker = false
            // 3순위: 선택 모드 종료
            isSelectionMode -> viewModel.exitSelectionMode()
            // 4순위: 메인 화면에서 뒤로가기 → 종료 확인
            else -> onBackPressed()
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text("${selectedFiles.size}개 선택됨")
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Default.Close, "선택 취소")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSelectAll() }) {
                            Icon(
                                if (selectedFiles.size == scannedFiles.size)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.CheckCircleOutline,
                                "전체 선택"
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("스마트 폴더 정리")
                            Text(
                                "학습된 데이터: ${learningDataCount}개",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showStatisticsScreen = true }) {
                            Icon(Icons.Default.BarChart, "통계")
                        }
                        IconButton(onClick = { showDirectoryPicker = true }) {
                            Icon(Icons.Default.Folder, "폴더 선택")
                        }
                        IconButton(onClick = { showSettingsScreen = true }) {
                            Icon(Icons.Default.Settings, "설정")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (scannedFiles.isNotEmpty() && !isSelectionMode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            viewModel.findDuplicates()
                            showDuplicatesScreen = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.ContentCopy, "중복 파일 찾기")
                    }

                    FloatingActionButton(
                        onClick = { viewModel.autoOrganizeAll(activity) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.AutoAwesome, "자동 정리")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 배너 광고
            BannerAdView()

            // 상태 메시지
            when (val state = uiState) {
                is UiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is UiState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is UiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is UiState.Empty -> {
                    EmptyStateView(
                        message = state.message,
                        onScanClick = { viewModel.scanFiles(selectedDirectory) }
                    )
                }
                is UiState.Idle -> {
                    WelcomeView(
                        onScanClick = { viewModel.scanFiles(selectedDirectory) },
                        onRequestPermissions = onRequestPermissions
                    )
                }
            }

            // 파일 목록
            if (scannedFiles.isNotEmpty()) {
                FileListView(
                    files = scannedFiles,
                    categorizedFiles = categorizedFiles,
                    selectedFiles = selectedFiles,
                    isSelectionMode = isSelectionMode,
                    onFileClick = { file ->
                        if (isSelectionMode) {
                            viewModel.toggleFileSelection(file.path)
                        } else {
                            selectedFile = file
                            showCategoryPicker = true
                        }
                    },
                    onFileLongClick = { file ->
                        viewModel.startSelectionMode(file.path)
                    }
                )
            }
        }

        // 삭제 확인 다이얼로그
        if (showDeleteConfirmDialog) {
            DeleteConfirmDialog(
                fileCount = selectedFiles.size,
                onConfirm = {
                    viewModel.deleteSelectedFiles()
                    showDeleteConfirmDialog = false
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                }
            )
        }

        // 디렉토리 선택 다이얼로그
        if (showDirectoryPicker) {
            DirectoryPickerDialog(
                currentDirectory = selectedDirectory,
                onDirectorySelected = { directory ->
                    viewModel.scanFiles(directory)
                    showDirectoryPicker = false
                },
                onDismiss = { showDirectoryPicker = false }
            )
        }

        // 카테고리 선택 다이얼로그
        if (showCategoryPicker && selectedFile != null) {
            CategoryPickerDialog(
                file = selectedFile!!,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(selectedFile!!, category)
                    showCategoryPicker = false
                    selectedFile = null
                },
                onDismiss = {
                    showCategoryPicker = false
                    selectedFile = null
                }
            )
        }

        // 통계 화면
        if (showStatisticsScreen) {
            StatisticsScreen(
                statistics = statistics,
                onBackPressed = { showStatisticsScreen = false },
                onFileClick = { filePath ->
                    // 파일 경로에서 폴더 경로 추출하여 파일 탐색기 열기
                    openFileInExplorer(context, filePath)
                },
                onFileDelete = { filePath ->
                    // 파일 삭제
                    viewModel.deleteSingleFile(filePath)
                }
            )
        }

        // 설정 화면
        if (showSettingsScreen) {
            SettingsScreen(
                onBackPressed = { showSettingsScreen = false }
            )
        }

        // 중복 파일 화면
        if (showDuplicatesScreen) {
            DuplicatesScreen(
                duplicateGroups = duplicateGroups,
                onBackPressed = { showDuplicatesScreen = false },
                onDeleteDuplicates = { group, filesToKeep ->
                    viewModel.deleteDuplicatesFromGroup(group, filesToKeep)
                }
            )
        }
    }
}

@Composable
fun WelcomeView(
    onScanClick: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "스마트 폴더 정리",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI가 당신의 파일을 자동으로 분류해줍니다",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Security, "권한 요청")
            Spacer(modifier = Modifier.width(8.dp))
            Text("권한 허용하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, "스캔")
            Spacer(modifier = Modifier.width(8.dp))
            Text("파일 스캔 시작")
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onScanClick) {
            Text("다시 스캔")
        }
    }
}

@Composable
fun FileListView(
    files: List<FileItem>,
    categorizedFiles: Map<FileCategory, List<FileItem>>,
    selectedFiles: Set<String>,
    isSelectionMode: Boolean,
    onFileClick: (FileItem) -> Unit,
    onFileLongClick: (FileItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 카테고리별로 그룹화된 파일 표시
        categorizedFiles.forEach { (category, categoryFiles) ->
            item {
                Text(
                    text = "${category.displayName} (${categoryFiles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(categoryFiles) { file ->
                FileItemCard(
                    file = file,
                    isSelected = selectedFiles.contains(file.path),
                    isSelectionMode = isSelectionMode,
                    onClick = { onFileClick(file) },
                    onLongClick = { onFileLongClick(file) }
                )
            }
        }

        // 분류되지 않은 파일들
        val uncategorized = files.filter { it.suggestedCategory == null }
        if (uncategorized.isNotEmpty()) {
            item {
                Text(
                    text = "분류되지 않음 (${uncategorized.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(uncategorized) { file ->
                FileItemCard(
                    file = file,
                    isSelected = selectedFiles.contains(file.path),
                    isSelectionMode = isSelectionMode,
                    onClick = { onFileClick(file) },
                    onLongClick = { onFileLongClick(file) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemCard(
    file: FileItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 선택 모드일 때 체크박스 표시
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 이미지 미리보기 또는 파일 아이콘
            if (file.isImage) {
                // 이미지 썸네일
                Image(
                    painter = rememberAsyncImagePainter(model = java.io.File(file.path)),
                    contentDescription = file.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 파일 아이콘
                Icon(
                    imageVector = getFileIcon(file),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 파일 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // AI 추천 카테고리
                if (file.suggestedCategory != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(file.suggestedCategory!!.color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${file.suggestedCategory!!.displayName} (${(file.confidence * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(file.suggestedCategory!!.color)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getFileIcon(file: FileItem) = when {
    file.isImage -> Icons.Default.Image
    file.isVideo -> Icons.Default.VideoFile
    file.isDocument -> Icons.Default.Description
    file.isAudio -> Icons.Default.AudioFile
    file.isArchive -> Icons.Default.FolderZip
    else -> Icons.Default.InsertDriveFile
}

fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}

@Composable
fun DirectoryPickerDialog(
    currentDirectory: DirectoryType,
    onDirectorySelected: (DirectoryType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("스캔할 폴더 선택") },
        text = {
            Column {
                DirectoryType.values().forEach { directory ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDirectorySelected(directory) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = directory == currentDirectory,
                            onClick = { onDirectorySelected(directory) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (directory) {
                                DirectoryType.ALL_STORAGE -> "전체 저장소 (AI 자동 스캔)"
                                DirectoryType.DOWNLOADS -> "다운로드"
                                DirectoryType.PICTURES -> "사진"
                                DirectoryType.DOCUMENTS -> "문서"
                                DirectoryType.DCIM -> "카메라"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun CategoryPickerDialog(
    file: FileItem,
    onCategorySelected: (FileCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("카테고리 선택") },
        text = {
            LazyColumn {
                items(FileCategory.values()) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onCategorySelected(category) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (category == file.suggestedCategory)
                                Color(category.color).copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        Color(category.color),
                                        RoundedCornerShape(4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (category == file.suggestedCategory) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI 추천",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(category.color)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    fileCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(text = "파일 삭제")
        },
        text = {
            Text(
                text = "선택한 ${fileCount}개의 파일을 삭제하시겠습니까?\n\n삭제된 파일은 복구할 수 없습니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * 파일 탐색기에서 파일 열기
 */
fun openFileInExplorer(context: android.content.Context, filePath: String) {
    try {
        val file = java.io.File(filePath)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // 파일을 열 수 있는 앱이 없으면 파일 관리자로 폴더 열기
            openFolderInExplorer(context, file.parent ?: filePath)
        }
    } catch (e: Exception) {
        android.util.Log.e("MainScreen", "파일 열기 실패", e)
        // 실패하면 기본 파일 관리자로 폴더 열기
        openFolderInExplorer(context, java.io.File(filePath).parent ?: filePath)
    }
}

/**
 * 파일 관리자에서 폴더 열기
 */
fun openFolderInExplorer(context: android.content.Context, folderPath: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(android.net.Uri.parse(folderPath), "resource/folder")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // DocumentsUI로 시도
            val documentsIntent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(documentsIntent)
        }
    } catch (e: Exception) {
        android.util.Log.e("MainScreen", "폴더 열기 실패", e)
    }
}
