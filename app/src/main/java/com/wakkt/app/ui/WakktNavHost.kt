package com.wakkt.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wakkt.app.ui.applist.AppListScreen
import com.wakkt.app.ui.editor.EditorScreen
import com.wakkt.app.ui.home.HomeScreen
import com.wakkt.app.ui.navigation.Routes
import com.wakkt.app.ui.onboarding.OnboardingScreen
import com.wakkt.app.ui.settings.SettingsScreen

@Composable
fun WakktNavHost(startDestination: String) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenEditor = { nav.navigate(Routes.editor(it)) },
                onAdd = { nav.navigate(Routes.APP_LIST) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.APP_LIST) {
            AppListScreen(
                // Replace the picker with the editor, so Back from the editor returns home.
                onOpenEditor = { nav.navigate(Routes.editor(it)) { popUpTo(Routes.APP_LIST) { inclusive = true } } },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            Routes.EDITOR,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
        ) {
            EditorScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
