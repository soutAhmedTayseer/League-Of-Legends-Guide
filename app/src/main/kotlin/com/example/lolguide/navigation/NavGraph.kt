package com.example.lolguide.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lolguide.presentation.champion.detail.ChampionDetailScreenRoot
import com.example.lolguide.presentation.champion.list.ChampionListScreenRoot
import com.example.lolguide.presentation.navigation.ChampionDetailRoute
import com.example.lolguide.presentation.navigation.ChampionListRoute

/**
 * The app's single NavHost.
 *
 * `:app` is the only module that knows about every screen at once. Screens
 * receive plain lambdas rather than the `NavController` itself, so a screen
 * cannot navigate somewhere its caller did not offer (AGENTS.md §6).
 */
@Composable
fun LolGuideNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = ChampionListRoute,
        modifier = modifier,
    ) {
        composable<ChampionListRoute> {
            ChampionListScreenRoot(
                onNavigateToDetail = { championId ->
                    navController.navigate(ChampionDetailRoute(championId = championId))
                },
            )
        }

        composable<ChampionDetailRoute> {
            ChampionDetailScreenRoot(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
