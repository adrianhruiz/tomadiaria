package com.adrianhruiz.tomadiaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adrianhruiz.tomadiaria.ui.navigation.TomaDiariaNavHost
import com.adrianhruiz.tomadiaria.ui.theme.TomaDiariaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialScheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID)
        setContent {
            TomaDiariaTheme {
                TomaDiariaNavHost(initialScheduleId = initialScheduleId)
            }
        }
    }

    companion object {
        const val EXTRA_SCHEDULE_ID = "scheduleId"
    }
}
