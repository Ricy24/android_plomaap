package com.example.plomaap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plomaap.ui.navigation.AppNavigation
import com.example.plomaap.ui.theme.PlomAppTheme
import com.example.plomaap.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            PlomAppTheme {
                val authViewModel: AuthViewModel = viewModel()
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
