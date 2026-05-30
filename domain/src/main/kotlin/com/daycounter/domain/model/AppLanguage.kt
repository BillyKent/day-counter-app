package com.daycounter.domain.model

/**
 * The app's selectable display language. English is the base/default and the fallback (Q1).
 * The handoff's other locales (pt/fr/de/it) are intentionally deferred to a future feature.
 *
 * @property tag BCP-47 language tag used to build the app [java.util.Locale].
 */
enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    SPANISH("es"),
    ;

    companion object {
        /** Default/fallback language. */
        val DEFAULT: AppLanguage = ENGLISH

        /** Resolves a persisted tag back to an [AppLanguage], falling back to [DEFAULT]. */
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: DEFAULT
    }
}
