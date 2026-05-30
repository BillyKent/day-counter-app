package com.daycounter.presentation.locale

import android.content.Context
import android.content.res.Configuration
import com.daycounter.domain.model.AppLanguage
import java.util.Locale

/**
 * Applies the in-app [AppLanguage] by wrapping a base [Context] with an updated locale
 * [Configuration] (research R4). Works on all supported API levels (26+) without requiring an
 * `AppCompatActivity`. The selected language is persisted in settings; `MainActivity` reads it in
 * `attachBaseContext` and calls [wrap], and `recreate()`s on change.
 */
object LocaleManager {

    /** Returns a context configured for [language]; also updates the JVM default locale. */
    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
