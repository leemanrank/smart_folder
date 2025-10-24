package com.smartfolder.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.smartfolder.app.ads.AdManager

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
