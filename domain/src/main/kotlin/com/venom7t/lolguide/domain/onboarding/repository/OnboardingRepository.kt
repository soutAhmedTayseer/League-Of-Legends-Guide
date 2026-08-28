package com.venom7t.lolguide.domain.onboarding.repository

import com.venom7t.lolguide.domain.onboarding.model.OnboardingPreferences
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun observePreferences(): Flow<OnboardingPreferences>
    suspend fun setRegion(region: Region)
    suspend fun setPrimaryRole(role: PrimaryRole)
    suspend fun markOnboardingComplete()
}
