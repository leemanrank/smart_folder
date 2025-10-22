package com.example.smart_folder_1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smart_folder_1.ui.screens.SplashScreen
import com.example.smart_folder_1.ui.theme.SmartFolderTheme

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
