package com.example.lolguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.lolguide.navigation.LolGuideNavGraph
import com.example.lolguide.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The app's only Activity.
 *
 * It does nothing but install the theme and the NavHost. Everything the
 * previous 821-line MainActivity did -- networking, caching, screen state,
 * and every Composable in the app -- now lives in the module that owns it.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                LolGuideNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
