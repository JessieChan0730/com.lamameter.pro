package com.yourbrand.lumameter.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yourbrand.lumameter.pro.ui.theme.LumaMeterTheme
import com.yourbrand.lumameter.pro.ui.meter.MeterRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumaMeterTheme {
                MeterRoute()
            }
        }
    }
}
