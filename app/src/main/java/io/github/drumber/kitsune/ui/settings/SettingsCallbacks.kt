package io.github.drumber.kitsune.ui.settings

import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.preference.StartPagePref

data class SettingsCallbacks(
    val onNavigateUp: () -> Unit = {},
    val onNavigateToAppearance: () -> Unit = {},
    val onNavigateToAppLogs: () -> Unit = {},
    val onNavigateToLibraries: () -> Unit = {},
    val onNavigateToGitHub: () -> Unit = {},
    val onLanguageSelected: (String) -> Unit = {},
    val onStartFragmentSelected: (StartPagePref) -> Unit = {},
    val onTitlesSelected: (LocalTitleLanguagePreference) -> Unit = {},
    val onCountrySelected: (String) -> Unit = {},
    val onSfwFilterSelected: (LocalSfwFilterPreference) -> Unit = {},
    val onRatingSystemSelected: (LocalRatingSystemPreference) -> Unit = {},
    val onDisplayNameChanged: (String) -> Unit = {},
    val onProfileUrlChanged: (String) -> Unit = {},
    val onRememberSearchFiltersToggle: (Boolean) -> Unit = {},
    val onDoubleBackToExitToggle: (Boolean) -> Unit = {},
    val onForceLegacyImagePickerToggle: (Boolean) -> Unit = {},
    val onCheckForUpdatesToggle: (Boolean) -> Unit = {},
    val onAppVersionClick: () -> Unit = {}
)
