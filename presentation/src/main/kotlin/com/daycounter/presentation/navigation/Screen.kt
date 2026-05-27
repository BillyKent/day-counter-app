package com.daycounter.presentation.navigation

/** Type-safe Navigation Compose route definitions. */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object CreateCounter : Screen("counter/create")
    data object Settings : Screen("settings")

    data object EditCounter : Screen("counter/{counterId}/edit") {
        const val ARG_COUNTER_ID = "counterId"
        fun routeFor(counterId: Long): String = "counter/$counterId/edit"
    }

    data object CounterDetail : Screen("counter/{counterId}") {
        const val ARG_COUNTER_ID = "counterId"
        fun routeFor(counterId: Long): String = "counter/$counterId"
        const val DEEP_LINK_URI: String = "daycounter://counter/{counterId}"
    }
}
