package com.chiu.guitarroad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.chiu.guitarroad.ui.navigation.GuitarNavGraph
import com.chiu.guitarroad.ui.navigation.Routes
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.GuitarRoadTheme
import com.chiu.guitarroad.ui.theme.InkPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as GuitarRoadApp).container
        var startDestination by mutableStateOf<String?>(null)
        lifecycleScope.launch {
            val done = withContext(Dispatchers.IO) {
                container.repository.seedIfNeeded()
                container.repository.ensureUser()
                container.repository.isOnboardingDone()
            }
            startDestination = if (done) Routes.MAIN else Routes.ONBOARDING
        }
        setContent {
            GuitarRoadTheme {
                val start = startDestination
                if (start == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CreamBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "吉他提升之路", color = InkPrimary)
                    }
                } else {
                    val navController = rememberNavController()
                    GuitarNavGraph(
                        navController = navController,
                        container = container,
                        startDestination = start,
                    )
                }
            }
        }
    }
}
