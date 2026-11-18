package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ai.configureAppCheck
import com.example.chat.ChatScreen
import com.example.ui.theme.FastAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before UI or ViewModel creation.
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.parseColor("#101827")),
            navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#162033"))
        )

        setContent {
            FastAiTheme {
                ChatScreen()
            }
        }
    }
}
