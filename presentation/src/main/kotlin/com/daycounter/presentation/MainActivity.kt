package com.daycounter.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.daycounter.data.datastore.OnboardingPreferencesDataStore
import com.daycounter.presentation.navigation.Contadores
import com.daycounter.presentation.navigation.DeepLinkResolver
import com.daycounter.presentation.navigation.MainScaffold
import com.daycounter.presentation.navigation.Onboarding
import com.daycounter.presentation.navigation.TopLevelBackStack
import com.daycounter.presentation.theme.DayCounterTheme
import androidx.navigation3.runtime.NavKey
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

        // Synchronous one-shot read so the back stack initialises with the correct start destination.
        onboardingShownState.value = runBlocking { onboardingPrefs.onboardingShown.first() }

        // Resolve the launch deep link once (widget / notification taps).
        val deepLinkStack = DeepLinkResolver.resolve(intent?.data)

        setContent {
            val onboardingShown by onboardingShownState
            DayCounterTheme {
                val backStack = remember {
                    TopLevelBackStack<NavKey>(Contadores).apply {
                        if (onboardingShown == false) {
                            // First-ever launch: render onboarding before the tab shell (FR-003/FR-004).
                            add(Onboarding)
                        } else {
                            // Seed any synthetic deep-link children onto the Contadores tab.
                            deepLinkStack.drop(1).forEach { add(it) }
                        }
                    }
                }
                MainScaffold(backStack)
            }
        }
    }
}
