package com.cryptotracker.app

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.cryptotracker.data.CryptoRepositoryImpl
import com.cryptotracker.feature.tracker.TrackerScreen
import com.cryptotracker.feature.tracker.TrackerViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E676),       
    background = Color(0xFF080B11),    
    surface = Color(0xFF121722),       
    onBackground = Color(0xFFFFFFFF),  
    onSurface = Color(0xFFFFFFFF)      
)

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        super.onCreate(savedInstanceState)
        
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        
        val repository = CryptoRepositoryImpl()
        val viewModel = TrackerViewModel(repository = repository)
        
        setContent {
            MaterialTheme(colorScheme = DarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background 
                ) {
                    TrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}