package com.venom7t.lolguide.presentation.di

import com.venom7t.lolguide.presentation.account.AccountViewModel
import com.venom7t.lolguide.presentation.champion.detail.ChampionDetailViewModel
import com.venom7t.lolguide.presentation.champion.list.ChampionListViewModel
import com.venom7t.lolguide.presentation.compare.CompareViewModel
import com.venom7t.lolguide.presentation.favourite.FavouritesViewModel
import com.venom7t.lolguide.presentation.followed.FollowedSummonersViewModel
import com.venom7t.lolguide.presentation.game.hub.GameHubViewModel
import com.venom7t.lolguide.presentation.game.round.GameRoundViewModel
import com.venom7t.lolguide.presentation.home.HomeViewModel
import com.venom7t.lolguide.presentation.item.ItemDetailViewModel
import com.venom7t.lolguide.presentation.item.ItemListViewModel
import com.venom7t.lolguide.presentation.ladder.LadderViewModel
import com.venom7t.lolguide.presentation.livegame.LiveGameViewModel
import com.venom7t.lolguide.presentation.lptracker.LpHistoryViewModel
import com.venom7t.lolguide.presentation.mastery.MasteryViewModel
import com.venom7t.lolguide.presentation.match.detail.MatchDetailViewModel
import com.venom7t.lolguide.presentation.onboarding.OnboardingViewModel
import com.venom7t.lolguide.presentation.quiz.QuizViewModel
import com.venom7t.lolguide.presentation.roulette.RouletteViewModel
import com.venom7t.lolguide.presentation.rune.RunesViewModel
import com.venom7t.lolguide.presentation.simulator.BuildSimulatorViewModel
import com.venom7t.lolguide.presentation.spell.SummonerSpellsViewModel
import com.venom7t.lolguide.presentation.summoner.profile.SummonerProfileViewModel
import com.venom7t.lolguide.presentation.summoner.search.SummonerSearchViewModel
import com.venom7t.lolguide.presentation.timer.GameTimersSessionStore
import com.venom7t.lolguide.presentation.timer.GameTimersViewModel
import com.venom7t.lolguide.presentation.voiceline.VoiceLinePlayerViewModel
import com.venom7t.lolguide.presentation.whatsnew.WhatsNewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Hand-written, not KSP-generated: `com.android.kotlin.multiplatform.library`
 * (required for a KMP module's Android target under AGP 9) has no KSP
 * support yet (google/ksp#2476, open), so Koin Annotations' `@KoinViewModel`
 * + `@ComponentScan` codegen -- what generated this exact binding list in
 * Phase 2 -- can't run here anymore. Every line below is a verbatim copy of
 * that generator's own last-known-good output, not a re-transcription, to
 * avoid reintroducing a wiring mistake by hand.
 */
val presentationModule = module {
    viewModel {
        AccountViewModel(
            observeAccount = get(),
            signInWithGoogle = get(),
            signOut = get(),
            syncOnStart = get(),
            settingsRepository = get(),
            onboardingRepository = get(),
        )
    }
    viewModel {
        ChampionDetailViewModel(
            savedStateHandle = get(),
            getChampionDetail = get(),
            getStatsAtLevel = get(),
            observeFavouriteIds = get(),
            toggleFavourite = get(),
            resolvePatch = get(),
            locale = get(),
            observeSavedBuilds = get(),
            deleteSavedBuild = get(),
        )
    }
    viewModel {
        ChampionListViewModel(
            observeChampions = get(),
            refreshChampions = get(),
            searchChampions = get(),
            filterChampions = get(),
            observeFavouriteIds = get(),
            toggleFavourite = get(),
            resolvePatch = get(),
            locale = get(),
        )
    }
    viewModel {
        CompareViewModel(
            observeChampions = get(),
            searchChampions = get(),
            compareChampions = get(),
        )
    }
    viewModel {
        FavouritesViewModel(
            observeChampions = get(),
            observeFavouriteIds = get(),
            toggleFavourite = get(),
            refreshChampions = get(),
            resolvePatch = get(),
            locale = get(),
        )
    }
    viewModel {
        FollowedSummonersViewModel(
            observeFollowedSummoners = get(),
            followedRepository = get(),
            searchSummoner = get(),
            resolvePatch = get(),
        )
    }
    viewModel {
        GameHubViewModel(
            observeGameStats = get(),
            pickDailyChampion = get(),
        )
    }
    viewModel {
        GameRoundViewModel(
            savedStateHandle = get(),
            observeChampions = get(),
            searchChampions = get(),
            startOrResumeRound = get(),
            submitGuess = get(),
            giveUpRound = get(),
            evaluateGuess = get(),
            observeGameStats = get(),
            getChampionDetail = get(),
            resolvePatch = get(),
        )
    }
    viewModel {
        HomeViewModel(
            resolvePatch = get(),
            observeChampions = get(),
            observeItems = get(),
            computePatchDiff = get(),
            getCurrentRotation = get(),
            onboardingRepository = get(),
            refreshChampions = get(),
            locale = get(),
        )
    }
    viewModel {
        ItemDetailViewModel(
            savedStateHandle = get(),
            itemRepository = get(),
            getBuildPath = get(),
            observeItems = get(),
            efficiencyCalculator = get(),
        )
    }
    viewModel {
        ItemListViewModel(
            observeItems = get(),
            refreshItems = get(),
            resolvePatch = get(),
            locale = get(),
        )
    }
    viewModel {
        LadderViewModel(
            getChallengerLadder = get(),
            getSummonerByPuuid = get(),
            resolvePatch = get(),
        )
    }
    viewModel {
        LiveGameViewModel(
            savedStateHandle = get(),
            getLiveGame = get(),
            resolvePatch = get(),
        )
    }
    viewModel {
        LpHistoryViewModel(
            savedStateHandle = get(),
            observeLpHistory = get(),
        )
    }
    viewModel {
        MasteryViewModel(
            savedStateHandle = get(),
            getChampionMasteries = get(),
            resolvePatch = get(),
        )
    }
    viewModel {
        MatchDetailViewModel(
            savedStateHandle = get(),
            getMatchDetail = get(),
            resolvePatch = get(),
            observeChampions = get(),
        )
    }
    viewModel { OnboardingViewModel(onboardingRepository = get()) }
    viewModel {
        QuizViewModel(
            observeChampions = get(),
            resolvePatch = get(),
            generateQuestion = get(),
            locale = get(),
        )
    }
    viewModel {
        RouletteViewModel(
            observeChampions = get(),
            randomChampion = get(),
        )
    }
    viewModel {
        RunesViewModel(
            runeRepository = get(),
            resolvePatch = get(),
            locale = get(),
        )
    }
    viewModel {
        BuildSimulatorViewModel(
            savedStateHandle = get(),
            observeChampions = get(),
            searchChampions = get(),
            observeItems = get(),
            simulator = get(),
            getSavedBuild = get(),
            saveBuild = get(),
        )
    }
    viewModel {
        SummonerSpellsViewModel(
            spellRepository = get(),
            resolvePatch = get(),
            locale = get(),
        )
    }
    viewModel {
        SummonerProfileViewModel(
            savedStateHandle = get(),
            searchSummoner = get(),
            getRankedEntries = get(),
            getMatchHistory = get(),
            getChampionMasteries = get(),
            getLiveGame = get(),
            computeDuoStats = get(),
            getClashTeam = get(),
            resolvePatch = get(),
            toggleFollowed = get(),
            followedRepository = get(),
            observeChampions = get(),
        )
    }
    viewModel {
        SummonerSearchViewModel(
            searchSummoner = get(),
            recentSearchRepository = get(),
            onboardingRepository = get(),
        )
    }
    single { GameTimersSessionStore() }
    viewModel {
        GameTimersViewModel(
            spellRepository = get(),
            resolvePatch = get(),
            locale = get(),
            sessionStore = get(),
        )
    }
    viewModel {
        VoiceLinePlayerViewModel(
            context = get(),
            voiceLineRepository = get(),
        )
    }
    viewModel {
        WhatsNewViewModel(
            resolvePatch = get(),
            observeChampions = get(),
            observeItems = get(),
            computePatchDiff = get(),
        )
    }
}
