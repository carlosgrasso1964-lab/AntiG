package com.genas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genas.app.ui.screens.MainScreen
import com.genas.app.ui.theme.GenASTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GenASTheme {
                val viewModel: com.genas.app.viewmodel.MainViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
