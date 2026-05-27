package com.daycounter.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.daycounter.presentation.counter.CounterDetailScreen
import com.daycounter.presentation.counter.CreateCounterScreen
import com.daycounter.presentation.counter.EditCounterScreen
import com.daycounter.presentation.home.HomeScreen
import com.daycounter.presentation.onboarding.OnboardingScreen
import com.daycounter.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(
    onboardingShown: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val start = if (onboardingShown) Screen.Home.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = start,
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddCounter = { navController.navigate(Screen.CreateCounter.route) },
                onEditCounter = { id -> navController.navigate(Screen.EditCounter.routeFor(id)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.CreateCounter.route) {
            CreateCounterScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.EditCounter.route,
            arguments = listOf(navArgument(Screen.EditCounter.ARG_COUNTER_ID) { type = NavType.LongType }),
        ) { entry ->
            val counterId = entry.arguments?.getLong(Screen.EditCounter.ARG_COUNTER_ID) ?: -1L
            EditCounterScreen(
                counterId = counterId,
                onDone = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.CounterDetail.route,
            arguments = listOf(navArgument(Screen.CounterDetail.ARG_COUNTER_ID) { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = Screen.CounterDetail.DEEP_LINK_URI }),
        ) { entry ->
            val counterId = entry.arguments?.getLong(Screen.CounterDetail.ARG_COUNTER_ID) ?: -1L
            CounterDetailScreen(
                counterId = counterId,
                onEdit = { id -> navController.navigate(Screen.EditCounter.routeFor(id)) },
                onCounterMissing = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
