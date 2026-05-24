package com.restopro.captain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.restopro.captain.navigation.RestoProNavGraph
import com.restopro.captain.ui.theme.RestoProCaptainTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestoProCaptainTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RestoProNavGraph()
                }
            }
        }
    }
}
