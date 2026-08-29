package com.venom7t.lolguide.domain.clash.model

data class ClashTeam(
    val teamId: String,
    val name: String,
    val tier: Int,
    val members: List<ClashTeamMember>,
    val nextMatchEpochMillis: Long?,
)

data class ClashTeamMember(
    /** CLASH-V1 identifies team members by encrypted summonerId, not puuid. */
    val summonerId: String,
    val role: String,
)
