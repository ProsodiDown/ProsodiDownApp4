package com.example.prosodidownapp4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prosodidownapp4.ui.navigation.ProsodiDownNavGraph
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkModeOverride by mainViewModel.isDarkMode.collectAsState()
            val darkTheme = isDarkModeOverride ?: isSystemInDarkTheme()

            ProsodiDownApp4Theme(darkTheme = darkTheme) {
                ProsodiDownNavGraph(mainViewModel = mainViewModel)
            }
        }
    }
}
