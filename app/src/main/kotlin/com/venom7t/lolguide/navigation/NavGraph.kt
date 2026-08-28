package com.venom7t.lolguide.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.champion.detail.ChampionDetailScreenRoot
import com.venom7t.lolguide.presentation.champion.list.ChampionListScreenRoot
import com.venom7t.lolguide.presentation.compare.CompareScreenRoot
import com.venom7t.lolguide.presentation.favourite.FavouritesScreenRoot
import com.venom7t.lolguide.presentation.navigation.ChampionDetailRoute
import com.venom7t.lolguide.presentation.navigation.ChampionListRoute
import com.venom7t.lolguide.presentation.navigation.CompareRoute
import com.venom7t.lolguide.presentation.navigation.FavouritesRoute
import com.venom7t.lolguide.presentation.navigation.RouletteRoute
import com.venom7t.lolguide.presentation.roulette.RouletteScreenRoot
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlin.reflect.KClass

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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // The bar is shown only on top-level destinations. Keeping it visible on
    // the detail, compare and roulette screens would offer a sideways jump out
    // of a screen the user navigated into deliberately.
    val showBottomBar = topLevelDestinations.any { destination ->
        currentDestination?.hasRoute(destination.route) == true
    }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = AppTheme.colors.surface) {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentDestination?.hasRoute(destination.route) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.navigate()) {
                                    // Switching tabs must not stack them, and
                                    // must restore where the user left off.
                                    popUpTo(ChampionListRoute) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(text = stringResource(destination.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppTheme.colors.onPrimary,
                                selectedTextColor = AppTheme.colors.primary,
                                indicatorColor = AppTheme.colors.primary,
                                unselectedIconColor = AppTheme.colors.textDisabled,
                                unselectedTextColor = AppTheme.colors.textDisabled,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ChampionListRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable<ChampionListRoute> {
                ChampionListScreenRoot(
                    onNavigateToDetail = { championId ->
                        navController.navigate(ChampionDetailRoute(championId = championId))
                    },
                    onNavigateToCompare = { navController.navigate(CompareRoute) },
                    onNavigateToRoulette = { navController.navigate(RouletteRoute) },
                )
            }

            composable<ChampionDetailRoute> {
                ChampionDetailScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable<FavouritesRoute> {
                FavouritesScreenRoot(
                    onNavigateToDetail = { championId ->
                        navController.navigate(ChampionDetailRoute(championId = championId))
                    },
                )
            }

            composable<RouletteRoute> {
                RouletteScreenRoot(
                    onNavigateToDetail = { championId ->
                        navController.navigate(ChampionDetailRoute(championId = championId))
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable<CompareRoute> {
                CompareScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private class TopLevelDestination(
    val route: KClass<*>,
    val navigate: () -> Any,
    val icon: ImageVector,
    val labelRes: Int,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = ChampionListRoute::class,
        navigate = { ChampionListRoute },
        icon = Icons.Default.Shield,
        labelRes = R.string.nav_champions,
    ),
    TopLevelDestination(
        route = FavouritesRoute::class,
        navigate = { FavouritesRoute },
        icon = Icons.Default.Star,
        labelRes = R.string.nav_favourites,
    ),
)
