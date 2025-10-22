package com.example.smart_folder_1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 온보딩 화면 (첫 실행 시 표시)
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.AutoAwesome,
            title = "AI 기반 자동 분류",
            description = "인공지능이 파일을 분석하여\n자동으로 카테고리별로 정리합니다",
            iconColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            icon = Icons.Default.ContentCopy,
            title = "중복 파일 감지",
            description = "동일한 파일을 찾아내어\n저장 공간을 효율적으로 관리합니다",
            iconColor = MaterialTheme.colorScheme.tertiary
        ),
        OnboardingPage(
            icon = Icons.Default.CloudDone,
            title = "저장 공간 절약",
            description = "불필요한 파일을 정리하고\n소중한 저장 공간을 확보하세요",
            iconColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            icon = Icons.Default.Security,
            title = "안전한 파일 관리",
            description = "모든 데이터는 기기에만 저장되며\n외부로 전송되지 않습니다",
            iconColor = MaterialTheme.colorScheme.error
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 스킵 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text("건너뛰기")
                }
            }

            // 현재 페이지 컨텐츠
            OnboardingPageContent(
                page = pages[currentPage],
                modifier = Modifier.weight(1f)
            )

            // 인디케이터
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (currentPage == index) 24.dp else 8.dp, 8.dp)
                            .background(
                                color = if (currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }

            // 하단 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("이전")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier.widthIn(min = 120.dp)
                ) {
                    Text(
                        if (currentPage == pages.size - 1)
                            "시작하기"
                        else
                            "다음"
                    )
                    if (currentPage < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 아이콘
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.large,
            color = page.iconColor.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = page.iconColor
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 제목
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 설명
        Text(
            text = page.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val iconColor: androidx.compose.ui.graphics.Color
)
