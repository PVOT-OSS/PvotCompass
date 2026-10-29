package org.prauga.compass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.prauga.compass.screens.CompassScreen
import org.prauga.compass.ui.theme.CompassTheme
import org.prauga.compass.viewmodel.CompassViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompassTheme {
                val context = LocalContext.current
                val viewModel = remember {
                    CompassViewModel(context)
                }
                CompassScreen(viewModel)
            }
        }
    }
}