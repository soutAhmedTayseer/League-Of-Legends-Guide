package com.venom7t.lolguide.domain.auth.model

/**
 * The signed-in identity, however it was reached.
 *
 * [isAnonymous] is what the UI keys off to decide whether to show a "sign in
 * with Google" prompt or an account summary -- every user has *some* account
 * (anonymous auth signs them in automatically), so "signed in or not" is the
 * wrong question; "anonymous or real" is the one that matters here.
 */
data class Account(
    val uid: String,
    val isAnonymous: Boolean,
    val displayName: String?,
    val email: String?,
)
