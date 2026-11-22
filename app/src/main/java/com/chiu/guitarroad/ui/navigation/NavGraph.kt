package com.chiu.guitarroad.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chiu.guitarroad.di.AppContainer
import com.chiu.guitarroad.ui.screens.onboarding.OnboardingScreen
import com.chiu.guitarroad.ui.screens.task.TaskDetailScreen
import com.chiu.guitarroad.ui.screens.tools.ChordDetailScreen
import com.chiu.guitarroad.ui.screens.tools.ChordLibraryScreen
import com.chiu.guitarroad.ui.screens.tools.MetronomeScreen
import com.chiu.guitarroad.ui.screens.tools.TheoryCardScreen
import com.chiu.guitarroad.ui.screens.tools.TheoryScreen
import com.chiu.guitarroad.ui.screens.tools.TunerScreen

/**
 * 全应用唯一的 NavHost（在 MainActivity 内）。
 * "main" 目的地显示带底部栏的 MainScreen；其余路由隐藏底部栏。
 */
@Composable
fun GuitarNavGraph(
    navController: NavHostController,
    container: AppContainer,
    startDestination: String,
) {
    val repo = container.repository
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(navController, repo)
        }
        composable(Routes.MAIN) {
            MainScreen(navController, container)
        }
        composable(
            route = Routes.TASK,
            arguments = listOf(navArgument("nodeId") { type = NavType.StringType }),
        ) { entry ->
            TaskDetailScreen(
                nodeId = entry.arguments?.getString("nodeId") ?: "",
                navController = navController,
                repo = repo,
            )
        }
        composable(
            route = Routes.TUNER,
            arguments = listOf(
                navArgument("nodeId") { type = NavType.StringType; defaultValue = "none" },
            ),
        ) { entry ->
            TunerScreen(
                nodeId = entry.arguments?.getString("nodeId") ?: "none",
                navController = navController,
                repo = repo,
            )
        }
        composable(
            route = Routes.METRONOME,
            arguments = listOf(
                navArgument("nodeId") { type = NavType.StringType; defaultValue = "none" },
                navArgument("bpm") { type = NavType.IntType; defaultValue = 60 },
            ),
        ) { entry ->
            MetronomeScreen(
                nodeId = entry.arguments?.getString("nodeId") ?: "none",
                bpm = entry.arguments?.getInt("bpm") ?: 60,
                navController = navController,
                repo = repo,
            )
        }
        composable(Routes.CHORDS) {
            ChordLibraryScreen(navController, repo)
        }
        composable(
            route = Routes.CHORD,
            arguments = listOf(navArgument("chordId") { type = NavType.StringType }),
        ) { entry ->
            ChordDetailScreen(
                chordId = entry.arguments?.getString("chordId") ?: "",
                navController = navController,
                repo = repo,
            )
        }
        composable(Routes.THEORY) {
            TheoryScreen(navController, repo)
        }
        composable(
            route = Routes.THEORY_CARD,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType }),
        ) { entry ->
            TheoryCardScreen(
                cardId = entry.arguments?.getString("cardId") ?: "",
                navController = navController,
                repo = repo,
                settings = container.settings,
            )
        }
    }
}
