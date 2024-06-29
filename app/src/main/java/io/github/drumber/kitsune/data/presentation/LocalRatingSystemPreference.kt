package io.github.drumber.kitsune.data.presentation

import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference

fun LocalRatingSystemPreference.getStringRes() = when (this) {
    LocalRatingSystemPreference.Simple -> R.string.preference_rating_system_simple
    LocalRatingSystemPreference.Regular -> R.string.preference_rating_system_regular
    LocalRatingSystemPreference.Advanced -> R.string.preference_rating_system_advanced
}
