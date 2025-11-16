package com.bpn.comics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bpn.comics.navigation.ComicNavGraph
import com.bpn.comics.ui.theme.ComicsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ComicsTheme {
                ComicNavGraph()
            }
        }
    }
}

