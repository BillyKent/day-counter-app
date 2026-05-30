package com.daycounter.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.daycounter.data.datastore.OnboardingPreferencesDataStore
import com.daycounter.data.datastore.SettingsPreferencesDataStore
import com.daycounter.domain.model.AppLanguage
import com.daycounter.presentation.locale.LocaleManager
import com.daycounter.presentation.navigation.Contadores
import com.daycounter.presentation.navigation.DeepLinkResolver
import com.daycounter.presentation.navigation.MainScaffold
import com.daycounter.presentation.navigation.Onboarding
import com.daycounter.presentation.navigation.TopLevelBackStack
import com.daycounter.presentation.theme.DayCounterTheme
import androidx.navigation3.runtime.NavKey
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var onboardingPrefs: OnboardingPreferencesDataStore

    /** Entry point to read the persisted language before Hilt field injection is available. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocaleEntryPoint {
        fun settingsPreferences(): SettingsPreferencesDataStore
    }

    override fun attachBaseContext(newBase: Context) {
        val settings = EntryPointAccessors
            .fromApplication(newBase.applicationContext, LocaleEntryPoint::class.java)
            .settingsPreferences()
        val language = AppLanguage.fromTag(runBlocking { settings.languageTag.first() })
        super.attachBaseContext(LocaleManager.wrap(newBase, language))
    }

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
