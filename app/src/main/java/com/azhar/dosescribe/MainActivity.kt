package com.azhar.dosescribe

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.azhar.dosescribe.data.preferences.PreferencesManager
import com.azhar.dosescribe.ui.nav.AppNavigation
import com.azhar.dosescribe.ui.theme.DoseScribeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen API BEFORE super.onCreate() to prevent Android icon flash
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            val darkMode by preferencesManager.darkModeFlow.collectAsState(initial = false)
            val language by preferencesManager.languageFlow.collectAsState(initial = "English")
            
            // Apply language
            LaunchedEffect(language) {
                val localeCode = when (language) {
                    "Arabic" -> "ar"
                    "French" -> "fr"
                    "Spanish" -> "es"
                    "Urdu" -> "ur"
                    else -> "en"
                }
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

            DoseScribeTheme(darkTheme = darkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
