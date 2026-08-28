package com.venom7t.lolguide.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.champion.detail.ChampionDetailScreenRoot
import com.venom7t.lolguide.presentation.champion.list.ChampionListScreenRoot
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.compare.CompareScreenRoot
import com.venom7t.lolguide.presentation.favourite.FavouritesScreenRoot
import com.venom7t.lolguide.presentation.account.AccountScreenRoot
import com.venom7t.lolguide.presentation.account.SignInGateScreenRoot
import com.venom7t.lolguide.presentation.followed.FollowedSummonersScreenRoot
import com.venom7t.lolguide.presentation.game.hub.GameHubScreenRoot
import com.venom7t.lolguide.presentation.game.round.GameRoundScreenRoot
import com.venom7t.lolguide.presentation.home.HomeScreenRoot
import com.venom7t.lolguide.presentation.item.ItemDetailScreenRoot
import com.venom7t.lolguide.presentation.item.ItemListScreenRoot
import com.venom7t.lolguide.presentation.ladder.LadderScreenRoot
import com.venom7t.lolguide.presentation.livegame.LiveGameScreenRoot
import com.venom7t.lolguide.presentation.lptracker.LpHistoryScreenRoot
import com.venom7t.lolguide.presentation.mastery.MasteryScreenRoot
import com.venom7t.lolguide.presentation.match.detail.MatchDetailScreenRoot
import com.venom7t.lolguide.presentation.navigation.BuildSimulatorRoute
import com.venom7t.lolguide.presentation.navigation.ChampionDetailRoute
import com.venom7t.lolguide.presentation.navigation.ChampionListRoute
import com.venom7t.lolguide.presentation.navigation.CompareRoute
import com.venom7t.lolguide.presentation.navigation.FavouritesRoute
import com.venom7t.lolguide.presentation.navigation.AccountRoute
import com.venom7t.lolguide.presentation.navigation.SignInGateRoute
import com.venom7t.lolguide.presentation.navigation.FollowedSummonersRoute
import com.venom7t.lolguide.presentation.navigation.GameHubRoute
import com.venom7t.lolguide.presentation.navigation.GameRoundRoute
import com.venom7t.lolguide.presentation.navigation.GameTimersRoute
import com.venom7t.lolguide.presentation.navigation.HomeRoute
import com.venom7t.lolguide.presentation.navigation.ItemDetailRoute
import com.venom7t.lolguide.presentation.navigation.ItemListRoute
import com.venom7t.lolguide.presentation.navigation.LadderRoute
import com.venom7t.lolguide.presentation.navigation.LiveGameRoute
import com.venom7t.lolguide.presentation.navigation.LpHistoryRoute
import com.venom7t.lolguide.presentation.navigation.MasteryRoute
import com.venom7t.lolguide.presentation.navigation.MatchDetailRoute
import com.venom7t.lolguide.presentation.navigation.OnboardingRoute
import com.venom7t.lolguide.presentation.navigation.QuizRoute
import com.venom7t.lolguide.presentation.navigation.RouletteRoute
import com.venom7t.lolguide.presentation.navigation.RunesRoute
import com.venom7t.lolguide.presentation.navigation.SummonerProfileRoute
import com.venom7t.lolguide.presentation.navigation.SummonerSearchRoute
import com.venom7t.lolguide.presentation.navigation.SummonerSpellsRoute
import com.venom7t.lolguide.presentation.navigation.WhatsNewRoute
import com.venom7t.lolguide.presentation.onboarding.OnboardingScreenRoot
import com.venom7t.lolguide.presentation.quiz.QuizScreenRoot
import com.venom7t.lolguide.presentation.roulette.RouletteScreenRoot
import com.venom7t.lolguide.presentation.rune.RunesScreenRoot
import com.venom7t.lolguide.presentation.simulator.BuildSimulatorScreenRoot
import com.venom7t.lolguide.presentation.spell.SummonerSpellsScreenRoot
import com.venom7t.lolguide.presentation.summoner.profile.SummonerProfileScreenRoot
import com.venom7t.lolguide.presentation.summoner.search.SummonerSearchScreenRoot
import com.venom7t.lolguide.presentation.theme.AppTheme
import com.venom7t.lolguide.presentation.timer.GameTimersScreenRoot
import com.venom7t.lolguide.presentation.whatsnew.WhatsNewScreenRoot
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
    appStartViewModel: AppStartViewModel = hiltViewModel(),
) {
    val startState by appStartViewModel.state.collectAsStateWithLifecycle()

    // The NavHost's start destination cannot change after first composition,
    // so onboarding completion must be known before the NavHost exists at
    // all. This is the one deliberate blocking read in the app's launch path.
    val readyState = startState as? AppStartState.Ready ?: run {
        LoadingContent()
        return
    }

    LolGuideNavGraphContent(
        startDestination = when {
            readyState.needsGoogleSignIn -> SignInGateRoute
            readyState.hasCompletedOnboarding -> HomeRoute
            else -> OnboardingRoute
        },
        hasCompletedOnboarding = readyState.hasCompletedOnboarding,
        modifier = modifier,
        navController = navController,
    )
}

@Composable
private fun LolGuideNavGraphContent(
    startDestination: Any,
    hasCompletedOnboarding: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // The bar is shown only on top-level destinations. Keeping it visible on
    // detail/compare/roulette/etc. would offer a sideways jump out of a
    // screen the user navigated into deliberately.
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
                                    popUpTo(HomeRoute) { saveState = true }
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
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable<SignInGateRoute> {
                SignInGateScreenRoot(
                    onSignedIn = {
                        navController.navigate(if (hasCompletedOnboarding) HomeRoute else OnboardingRoute) {
                            popUpTo(SignInGateRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<OnboardingRoute> {
                OnboardingScreenRoot(
                    onFinished = {
                        navController.navigate(HomeRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<HomeRoute> {
                HomeScreenRoot(
                    onNavigateToWhatsNew = { navController.navigate(WhatsNewRoute) },
                    onNavigateToSimulator = { navController.navigate(BuildSimulatorRoute()) },
                    onNavigateToRoulette = { navController.navigate(RouletteRoute) },
                    onNavigateToQuiz = { navController.navigate(QuizRoute) },
                    onNavigateToTimers = { navController.navigate(GameTimersRoute) },
                    onNavigateToLadder = { navController.navigate(LadderRoute) },
                    onNavigateToFollowedSummoners = { navController.navigate(FollowedSummonersRoute) },
                    onNavigateToGame = { navController.navigate(GameHubRoute) },
                    onNavigateToAccount = { navController.navigate(AccountRoute) },
                )
            }

            composable<WhatsNewRoute> {
                WhatsNewScreenRoot(onNavigateBack = { navController.popBackStack() })
            }

            composable<QuizRoute> {
                QuizScreenRoot(onNavigateBack = { navController.popBackStack() })
            }

            composable<GameTimersRoute> {
                GameTimersScreenRoot(onNavigateBack = { navController.popBackStack() })
            }

            composable<AccountRoute> {
                AccountScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<GameHubRoute> {
                GameHubScreenRoot(
                    onNavigateToRound = { mode -> navController.navigate(GameRoundRoute(mode = mode.name)) },
                    onBack = { navController.popBackStack() },
                )
            }

            composable<GameRoundRoute> {
                GameRoundScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<ChampionListRoute> {
                ChampionListScreenRoot(
                    onNavigateToDetail = { championId ->
                        navController.navigate(ChampionDetailRoute(championId = championId))
                    },
                    onNavigateToCompare = { navController.navigate(CompareRoute) },
                    onNavigateToRoulette = { navController.navigate(RouletteRoute) },
                )
            }

            composable<ChampionDetailRoute>(
                // Feature 41: lolguide://champion/{championId}, mirrored on
                // the activity's intent filter in AndroidManifest.xml
                // (AGENTS.md §6 -- deep links are declared on the route and
                // mirrored in the manifest).
                deepLinks = listOf(
                    navDeepLink { uriPattern = "lolguide://champion/{championId}" },
                ),
            ) {
                ChampionDetailScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSimulator = { savedBuildId ->
                        navController.navigate(BuildSimulatorRoute(savedBuildId = savedBuildId))
                    },
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

            composable<ItemListRoute> {
                ItemListScreenRoot(
                    onNavigateToDetail = { itemId ->
                        navController.navigate(ItemDetailRoute(itemId = itemId))
                    },
                    onNavigateToBuildSimulator = { navController.navigate(BuildSimulatorRoute()) },
                    onNavigateToRunes = { navController.navigate(RunesRoute) },
                    onNavigateToSummonerSpells = { navController.navigate(SummonerSpellsRoute) },
                )
            }

            composable<ItemDetailRoute> {
                ItemDetailScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToItem = { itemId ->
                        // Following a build-path link replaces rather than
                        // stacks, so tapping through several tiers of an item
                        // line does not leave a long chain to back out of.
                        navController.navigate(ItemDetailRoute(itemId = itemId)) {
                            popUpTo(ItemListRoute)
                        }
                    },
                )
            }

            composable<BuildSimulatorRoute> {
                BuildSimulatorScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable<RunesRoute> {
                RunesScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable<SummonerSpellsRoute> {
                SummonerSpellsScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // --- Phase 4: player data ---

            composable<SummonerSearchRoute> {
                SummonerSearchScreenRoot(
                    onNavigateToProfile = { name, tagline, region ->
                        navController.navigate(
                            SummonerProfileRoute(
                                riotIdName = name,
                                riotIdTagline = tagline,
                                region = region.name,
                            ),
                        )
                    },
                )
            }

            composable<SummonerProfileRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<SummonerProfileRoute>()
                SummonerProfileScreenRoot(
                    onNavigateToMatchDetail = { matchId, viewingPuuid ->
                        navController.navigate(
                            MatchDetailRoute(
                                matchId = matchId,
                                region = route.region,
                                viewingPuuid = viewingPuuid,
                            ),
                        )
                    },
                    onNavigateToLiveGame = { puuid ->
                        navController.navigate(LiveGameRoute(puuid = puuid, region = route.region))
                    },
                    onNavigateToMasteries = { puuid ->
                        navController.navigate(MasteryRoute(puuid = puuid, region = route.region))
                    },
                    onNavigateToLpHistory = { puuid, riotIdName, riotIdTagline ->
                        navController.navigate(
                            LpHistoryRoute(
                                puuid = puuid,
                                riotIdName = riotIdName,
                                riotIdTagline = riotIdTagline,
                            ),
                        )
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable<LpHistoryRoute> {
                LpHistoryScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<MatchDetailRoute> {
                MatchDetailScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<LiveGameRoute> {
                LiveGameScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<MasteryRoute> {
                MasteryScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<LadderRoute> {
                LadderScreenRoot(onBack = { navController.popBackStack() })
            }

            composable<FollowedSummonersRoute> {
                FollowedSummonersScreenRoot(
                    onNavigateToProfile = { name, tagline, region ->
                        navController.navigate(
                            SummonerProfileRoute(
                                riotIdName = name,
                                riotIdTagline = tagline,
                                region = region.name,
                            ),
                        )
                    },
                    onBack = { navController.popBackStack() },
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
        route = HomeRoute::class,
        navigate = { HomeRoute },
        icon = Icons.Default.Home,
        labelRes = R.string.nav_home,
    ),
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
    TopLevelDestination(
        route = ItemListRoute::class,
        navigate = { ItemListRoute },
        icon = Icons.Default.ShoppingCart,
        labelRes = R.string.nav_items,
    ),
    TopLevelDestination(
        route = SummonerSearchRoute::class,
        navigate = { SummonerSearchRoute },
        icon = Icons.Default.Person,
        labelRes = R.string.nav_summoner,
    ),
)
