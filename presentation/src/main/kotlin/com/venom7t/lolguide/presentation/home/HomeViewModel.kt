package com.venom7t.lolguide.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.RefreshChampionsUseCase
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import com.venom7t.lolguide.domain.patch.usecase.ComputePatchDiffUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.rotation.usecase.GetCurrentRotationUseCase
import org.koin.android.annotation.KoinViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class HomeState(
    val patchVersion: String? = null,
    val isPatchStale: Boolean = false,
    /** Null means "not yet checked" or "genuinely nothing to compare against". */
    val hasWhatsNew: Boolean = false,
    /**
     * The current free rotation, resolved from CHAMPION-V3's numeric keys
     * against the cached champion list. Null while loading or when no Riot
     * key is configured -- the hero falls back to a patch summary in that
     * case (AGENTS.md §8.2), it does not show an error.
     *
     * Full [Champion]s rather than ids: the hero needs a display name, and
     * deriving one from the Data Dragon id would print "MonkeyKing" instead
     * of "Wukong".
     */
    val freeRotation: List<Champion>? = null,
    /**
     * A champion picked from the local cache by date, so the hero has real
     * art to show even with no Riot key and no network. Free rotation needs
     * CHAMPION-V3, which is keyed and frequently unavailable (AGENTS.md
     * §8.2); without this the hero would be a large empty band whenever
     * that call fails, which reads as a bug rather than as a degraded
     * feature.
     */
    val dailyChampion: Champion? = null,
) {
    /** The champion the hero leads with. */
    val featured: Champion? get() = freeRotation?.firstOrNull() ?: dailyChampion

    /** Drives the hero's eyebrow, so it never mislabels the daily pick as the rotation. */
    val isFeaturedFreeRotation: Boolean get() = !freeRotation.isNullOrEmpty()
}

sealed interface HomeEvent {
    data object ScreenOpened : HomeEvent
}

@KoinViewModel
class HomeViewModel (
    private val resolvePatch: ResolvePatchUseCase,
    private val observeChampions: ObserveChampionsUseCase,
    private val observeItems: ObservePurchasableItemsUseCase,
    private val computePatchDiff: ComputePatchDiffUseCase,
    private val getCurrentRotation: GetCurrentRotationUseCase,
    private val onboardingRepository: OnboardingRepository,
    private val refreshChampions: RefreshChampionsUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var hasStarted = false

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    load()
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val patch = resolvePatch().getOrNull() ?: return@launch
            _state.update { it.copy(patchVersion = patch.version, isPatchStale = patch.isStale) }

            // Champion data only ever reaches Room through RefreshChampionsUseCase,
            // which normally runs from ChampionListViewModel -- landing here
            // first (a fresh install, or straight into Home before ever
            // opening the Champions tab) would otherwise read an empty cache
            // here and never pick a daily champion until something else
            // happened to populate it (same failure shape as the Favourites
            // bug this mirrors the fix for).
            var champions = observeChampions().first()
            if (champions.isEmpty()) {
                refreshChampions(patch.version, locale)
                champions = observeChampions().first()
            }
            _state.update { it.copy(dailyChampion = champions.pickForToday()) }

            val items = observeItems().first()
            // Cheap existence check only -- the full diff computation happens
            // in WhatsNewViewModel when the user actually opens that screen.
            // The home teaser only needs to know whether there is anything
            // to show at all.
            val diff = computePatchDiff(patch.version, champions, items)
            _state.update { it.copy(hasWhatsNew = diff != null && !diff.isEmpty) }

            // A missing/expired key or a network miss both degrade to "not
            // shown" here, matching the Phase 3 placeholder's contract --
            // the rotation card is not the place to surface a Riot API error.
            val region = onboardingRepository.observePreferences().first().region ?: Region.NA
            getCurrentRotation(region).onSuccess { rotation ->
                val byKey = champions.associateBy { it.key.toIntOrNull() }
                val rotationChampions = rotation.championIds.mapNotNull { byKey[it] }
                _state.update { it.copy(freeRotation = rotationChampions) }
            }
        }
    }
}

/**
 * The same champion for the whole day, and the same one on every device.
 *
 * Keyed on days-since-epoch over a name-sorted list rather than
 * [kotlin.random.Random], so the hero does not change on every recomposition
 * or reshuffle when the screen is reopened. Uses millis rather than
 * `java.time` because this module targets API 24 without desugaring.
 */
private fun List<Champion>.pickForToday(): Champion? {
    if (isEmpty()) return null
    val daysSinceEpoch = System.currentTimeMillis() / 86_400_000L
    return sortedBy { it.id }[(daysSinceEpoch % size).toInt()]
}
