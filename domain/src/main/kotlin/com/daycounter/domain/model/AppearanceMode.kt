package com.daycounter.domain.model

/**
 * User-controlled dark-mode preference. [SYSTEM] follows the device setting (default); [LIGHT] and
 * [DARK] force the corresponding brand palette.
 */
enum class AppearanceMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        /** Default appearance: follow the system setting. */
        val DEFAULT: AppearanceMode = SYSTEM

        /** Resolves a persisted name back to an [AppearanceMode], falling back to [DEFAULT]. */
        fun fromName(name: String?): AppearanceMode =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
