package com.daycounter.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.daycounter.data.datastore.OnboardingPreferencesDataStore
import com.daycounter.presentation.navigation.AppNavGraph
import com.daycounter.presentation.theme.DayCounterTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var onboardingPrefs: OnboardingPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        val onboardingShownState = mutableStateOf<Boolean?>(null)
        splash.setKeepOnScreenCondition { onboardingShownState.value == null }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Synchronous one-shot read so NavHost initialises with the correct start destination.
        onboardingShownState.value = runBlocking {
            onboardingPrefs.onboardingShown.first()
        }

        setContent {
            val onboardingShown by onboardingShownState
            DayCounterTheme {
                AppNavGraph(onboardingShown = onboardingShown ?: false)
            }
        }
    }
}
