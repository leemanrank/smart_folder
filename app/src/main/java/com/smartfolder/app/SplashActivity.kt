package com.smartfolder.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smartfolder.app.ui.screens.SplashScreen
import com.smartfolder.app.ui.theme.SmartFolderTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartFolderTheme {
                SplashScreen(
                    onSplashComplete = {
                        // 메인 화면으로 이동
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
