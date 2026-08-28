package com.venom7t.lolguide.presentation.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Text a ViewModel can put in state without touching Android resources.
 *
 * ViewModels must not resolve strings themselves: doing so would bake the
 * language in at the moment state was produced, so an in-app locale switch
 * would leave stale text on screen. Resolution happens at the point of use,
 * where the current configuration is available (AGENTS.md §10).
 */
sealed interface UiText {

    data class Resource(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    /**
     * Text that is already final and untranslatable -- a champion's proper
     * name, a patch number. Never use this for a sentence.
     */
    data class Raw(val value: String) : UiText

    /** Resolves inside composition, so a locale change recomposes the text. */
    @Composable
    fun asString(): String = when (this) {
        is Raw -> value
        is Resource -> if (args.isEmpty()) {
            stringResource(id)
        } else {
            stringResource(id, *args.toTypedArray())
        }
    }

    /**
     * Resolves outside composition.
     *
     * Needed for things that happen in a coroutine rather than during a
     * recomposition -- showing a snackbar, for instance -- where the
     * `@Composable` overload cannot be called.
     */
    fun resolve(context: Context): String = when (this) {
        is Raw -> value
        is Resource -> if (args.isEmpty()) {
            context.getString(id)
        } else {
            context.getString(id, *args.toTypedArray())
        }
    }
}

fun String.asUiText(): UiText = UiText.Raw(this)

fun uiText(@StringRes id: Int, vararg args: Any): UiText = UiText.Resource(id, args.toList())
