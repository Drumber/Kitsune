package io.github.drumber.kitsune.ui.settings

import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.preference.StartPagePref

data class SettingsUiState(
    val user: LocalUser? = null,
    val isLoading: Boolean = false,
    val titles: LocalTitleLanguagePreference = LocalTitleLanguagePreference.Canonical,
    val startFragment: StartPagePref = StartPagePref.Home,
    val rememberSearchFilters: Boolean = true,
    val doubleBackToExit: Boolean = false,
    val isPhotoPickerAvailable: Boolean = false,
    val forceLegacyImagePicker: Boolean = false,
    val checkForUpdatesOnStart: Boolean = false,
    val appVersion: String = ""
)
