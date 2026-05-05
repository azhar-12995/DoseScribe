package com.azhar.dosescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.azhar.dosescribe.ui.nav.AppNavigation
import com.azhar.dosescribe.ui.theme.DoseScribeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen API BEFORE super.onCreate() to prevent Android icon flash
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            DoseScribeTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
