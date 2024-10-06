package io.github.drumber.kitsune.ui.onboarding

import io.github.drumber.kitsune.data.source.local.user.model.LocalUser

data class OnboardingUiState(
    val backgroundImages: List<String> = emptyList(),
    val user: LocalUser? = null
)