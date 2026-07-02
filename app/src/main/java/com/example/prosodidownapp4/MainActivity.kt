package com.example.prosodidownapp4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.prosodidownapp4.ui.navigation.ProsodiDownNavGraph
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProsodiDownApp4Theme {
                ProsodiDownNavGraph()
            }
        }
    }
}