package com.example.smart_folder_1.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.smart_folder_1.ads.AdManager

/**
 * Compose용 배너 광고 뷰
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdManager.createBannerAdView(ctx).apply {
                AdManager.loadBannerAd(this)
            }
        }
    )
}
