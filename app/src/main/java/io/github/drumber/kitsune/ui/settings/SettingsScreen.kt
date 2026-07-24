package io.github.drumber.kitsune.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.AppLocales
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.preference.StartPagePref
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    callbacks: SettingsCallbacks,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = { KitsuneBackButton(callbacks.onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SettingsPreferenceList(uiState = uiState, callbacks = callbacks)
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun SettingsPreferenceList(uiState: SettingsUiState, callbacks: SettingsCallbacks) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { PreferenceCategoryHeader(stringResource(R.string.preference_category_ui)) }
        item { UiPreferenceSection(uiState = uiState, callbacks = callbacks) }
        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { PreferenceCategoryHeader(stringResource(R.string.preference_category_account)) }
        item { AccountPreferenceSection(uiState = uiState, callbacks = callbacks) }
        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { PreferenceCategoryHeader(stringResource(R.string.preference_category_advanced)) }
        item { AdvancedPreferenceSection(uiState = uiState, callbacks = callbacks) }
        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { PreferenceCategoryHeader(stringResource(R.string.preference_category_about)) }
        item { AboutPreferenceSection(uiState = uiState, callbacks = callbacks) }
    }
}

@Composable
private fun UiPreferenceSection(uiState: SettingsUiState, callbacks: SettingsCallbacks) {
    val loggedIn = uiState.user != null
    val loginRequired = if (!loggedIn) {
        stringResource(R.string.preference_not_logged_in)
    } else {
        null
    }
    PreferenceClickRow(
        title = stringResource(R.string.nav_appearance),
        summary = stringResource(R.string.preference_appearance_description),
        onClick = callbacks.onNavigateToAppearance
    )
    LanguagePreferenceRow(onLanguageSelected = callbacks.onLanguageSelected)
    StartFragmentPreferenceRow(
        selected = uiState.startFragment,
        onSelect = callbacks.onStartFragmentSelected
    )
    TitlesPreferenceRow(
        selected = uiState.titles,
        onSelect = callbacks.onTitlesSelected
    )
    CountryPreferenceRow(
        current = uiState.user?.country,
        isEnabled = loggedIn,
        loginRequired = loginRequired,
        onSelect = callbacks.onCountrySelected
    )
    AdultContentPreferenceRow(
        current = uiState.user?.sfwFilterPreference,
        isEnabled = loggedIn,
        loginRequired = loginRequired,
        onSelect = callbacks.onSfwFilterSelected
    )
    RatingSystemPreferenceRow(
        current = uiState.user?.ratingSystem,
        isEnabled = loggedIn,
        loginRequired = loginRequired,
        onSelect = callbacks.onRatingSystemSelected
    )
}

@Composable
private fun AccountPreferenceSection(uiState: SettingsUiState, callbacks: SettingsCallbacks) {
    val loggedIn = uiState.user != null
    val loginRequired = if (!loggedIn) {
        stringResource(R.string.preference_not_logged_in)
    } else {
        null
    }
    DisplayNamePreferenceRow(
        current = uiState.user?.name,
        isEnabled = loggedIn,
        loginRequired = loginRequired,
        onConfirm = callbacks.onDisplayNameChanged
    )
    ProfileUrlPreferenceRow(
        current = uiState.user?.slug,
        isEnabled = loggedIn,
        loginRequired = loginRequired,
        onConfirm = callbacks.onProfileUrlChanged
    )
}

@Composable
private fun AdvancedPreferenceSection(uiState: SettingsUiState, callbacks: SettingsCallbacks) {
    PreferenceSwitchRow(
        title = stringResource(R.string.preference_remember_search_filters),
        summary = stringResource(R.string.preference_remember_search_filters_description),
        checked = uiState.rememberSearchFilters,
        onCheckedChange = callbacks.onRememberSearchFiltersToggle
    )
    PreferenceSwitchRow(
        title = stringResource(R.string.preference_double_back_to_exit),
        summary = stringResource(R.string.preference_double_back_to_exit_description),
        checked = uiState.doubleBackToExit,
        onCheckedChange = callbacks.onDoubleBackToExitToggle
    )
    if (uiState.isPhotoPickerAvailable) {
        PreferenceSwitchRow(
            title = stringResource(R.string.preference_force_legacy_image_picker),
            summary = stringResource(R.string.preference_force_legacy_image_picker_description),
            checked = uiState.forceLegacyImagePicker,
            onCheckedChange = callbacks.onForceLegacyImagePickerToggle
        )
    }
    PreferenceSwitchRow(
        title = stringResource(R.string.preference_check_for_updates),
        summary = stringResource(R.string.preference_check_for_updates_description),
        checked = uiState.checkForUpdatesOnStart,
        onCheckedChange = callbacks.onCheckForUpdatesToggle
    )
    PreferenceClickRow(
        title = stringResource(R.string.preference_app_logs),
        summary = stringResource(R.string.preference_app_logs_description),
        onClick = callbacks.onNavigateToAppLogs
    )
}

@Composable
private fun AboutPreferenceSection(uiState: SettingsUiState, callbacks: SettingsCallbacks) {
    val versionSummary = uiState.appVersion +
        System.lineSeparator() +
        stringResource(R.string.preference_app_version_description)
    PreferenceClickRow(
        title = stringResource(R.string.preference_app_version),
        summary = versionSummary,
        onClick = callbacks.onAppVersionClick
    )
    PreferenceClickRow(
        title = stringResource(R.string.preference_github_repo),
        summary = stringResource(R.string.github_repo_url),
        onClick = callbacks.onNavigateToGitHub
    )
    PreferenceClickRow(
        title = stringResource(R.string.preference_open_source_libraries),
        summary = stringResource(R.string.preference_open_source_libraries_description),
        onClick = callbacks.onNavigateToLibraries
    )
}

@Composable
private fun LanguagePreferenceRow(onLanguageSelected: (String) -> Unit) {
    val context = LocalContext.current
    val (options, selectedTag) = remember { computeLanguageData(context) }
    var showDialog by remember { mutableStateOf(false) }
    val summary = options.find { it.first == selectedTag }?.second ?: ""
    if (showDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.preference_language),
            options = options,
            selected = selectedTag,
            onSelect = { value ->
                onLanguageSelected(value)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_language),
        summary = summary,
        onClick = { showDialog = true }
    )
}

@Composable
private fun StartFragmentPreferenceRow(
    selected: StartPagePref,
    onSelect: (StartPagePref) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedName = stringResource(selected.toNameResId())
    val summary = stringResource(R.string.preference_start_fragment_description, selectedName)
    if (showDialog) {
        val options = StartPagePref.entries.map { pref -> pref to stringResource(pref.toNameResId()) }
        SingleChoiceDialog(
            title = stringResource(R.string.preference_start_fragment),
            options = options,
            selected = selected,
            onSelect = { value -> onSelect(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_start_fragment),
        summary = summary,
        onClick = { showDialog = true }
    )
}

@Composable
private fun TitlesPreferenceRow(
    selected: LocalTitleLanguagePreference,
    onSelect: (LocalTitleLanguagePreference) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        val options = LocalTitleLanguagePreference.entries.map { pref ->
            pref to stringResource(pref.toNameResId())
        }
        SingleChoiceDialog(
            title = stringResource(R.string.preference_titles),
            options = options,
            selected = selected,
            onSelect = { value -> onSelect(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_titles),
        summary = stringResource(R.string.preference_titles_description),
        onClick = { showDialog = true }
    )
}

@Composable
private fun CountryPreferenceRow(
    current: String?,
    isEnabled: Boolean,
    loginRequired: String?,
    onSelect: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val countryOptions = remember { computeCountryOptions() }
    val summary = when {
        !isEnabled -> loginRequired
        current == null -> stringResource(R.string.preference_country_summary_non)
        else -> {
            val name = Locale.Builder().setRegion(current).build().displayCountry
            stringResource(R.string.preference_country_summary, name)
        }
    }
    if (showDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.preference_country),
            options = countryOptions,
            selected = current ?: "",
            onSelect = { value -> onSelect(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_country),
        summary = summary,
        onClick = { showDialog = true },
        enabled = isEnabled
    )
}

@Composable
private fun AdultContentPreferenceRow(
    current: LocalSfwFilterPreference?,
    isEnabled: Boolean,
    loginRequired: String?,
    onSelect: (LocalSfwFilterPreference) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val summary = if (!isEnabled) {
        loginRequired
    } else {
        stringResource(current.toSummaryResId())
    }
    if (showDialog) {
        val options = LocalSfwFilterPreference.entries.map { pref ->
            pref to stringResource(pref.toNameResId())
        }
        SingleChoiceDialog(
            title = stringResource(R.string.preference_adult_content),
            options = options,
            selected = current ?: LocalSfwFilterPreference.SFW,
            onSelect = { value -> onSelect(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_adult_content),
        summary = summary,
        onClick = { showDialog = true },
        enabled = isEnabled
    )
}

@Composable
private fun RatingSystemPreferenceRow(
    current: LocalRatingSystemPreference?,
    isEnabled: Boolean,
    loginRequired: String?,
    onSelect: (LocalRatingSystemPreference) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val summary = if (!isEnabled) {
        loginRequired
    } else {
        current?.let { stringResource(it.toNameResId()) } ?: stringResource(R.string.no_information)
    }
    if (showDialog) {
        val options = LocalRatingSystemPreference.entries.reversed().map { pref ->
            pref to stringResource(pref.toNameResId())
        }
        SingleChoiceDialog(
            title = stringResource(R.string.preference_rating_system),
            options = options,
            selected = current ?: LocalRatingSystemPreference.Regular,
            onSelect = { value -> onSelect(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_rating_system),
        summary = summary,
        onClick = { showDialog = true },
        enabled = isEnabled
    )
}

@Composable
private fun DisplayNamePreferenceRow(
    current: String?,
    isEnabled: Boolean,
    loginRequired: String?,
    onConfirm: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val summary = if (!isEnabled) loginRequired else current
    if (showDialog) {
        PreferenceEditTextDialog(
            title = stringResource(R.string.preference_display_name),
            initialValue = current ?: "",
            onConfirm = { value -> onConfirm(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_display_name),
        summary = summary,
        onClick = { showDialog = true },
        enabled = isEnabled
    )
}

@Composable
private fun ProfileUrlPreferenceRow(
    current: String?,
    isEnabled: Boolean,
    loginRequired: String?,
    onConfirm: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val summary = when {
        !isEnabled -> loginRequired
        current.isNullOrBlank() -> stringResource(R.string.preference_profile_url_not_set)
        else -> Kitsu.USER_URL_PREFIX + current
    }
    if (showDialog) {
        PreferenceEditTextDialog(
            title = stringResource(R.string.preference_profile_url),
            initialValue = current ?: "",
            onConfirm = { value -> onConfirm(value); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
    PreferenceClickRow(
        title = stringResource(R.string.preference_profile_url),
        summary = summary,
        onClick = { showDialog = true },
        enabled = isEnabled
    )
}

// region Helpers

private fun computeLanguageData(context: Context): Pair<List<Pair<String, String>>, String> {
    val supportedLocales = AppLocales.SUPPORTED_LOCALES
    val selectedLocale = AppCompatDelegate.getApplicationLocales().getFirstMatch(supportedLocales)
    val contextLocale = selectedLocale ?: Locale.getDefault()
    val selectedTag = supportedLocales.find { tag ->
        val locale = Locale.forLanguageTag(tag)
        selectedLocale != null &&
            locale.language == selectedLocale.language &&
            locale.country == selectedLocale.country
    } ?: ""
    val options = buildList {
        add("" to context.getString(R.string.preference_language_default))
        for (tag in supportedLocales) {
            val locale = Locale.forLanguageTag(tag)
            val name = locale.getDisplayLanguage(contextLocale)
            val country = locale.getDisplayCountry(contextLocale)
            add(tag to if (country.isNotBlank()) "$name ($country)" else name)
        }
    }
    return options to selectedTag
}

private fun computeCountryOptions(): List<Pair<String, String>> =
    Locale.getISOCountries().map { code ->
        code to Locale.Builder().setRegion(code).build().displayCountry
    }

private fun StartPagePref.toNameResId(): Int = when (this) {
    StartPagePref.Home -> R.string.preference_start_fragment_home
    StartPagePref.Search -> R.string.preference_start_fragment_search
    StartPagePref.Library -> R.string.preference_start_fragment_library
    StartPagePref.Profile -> R.string.preference_start_fragment_profile
}

private fun LocalTitleLanguagePreference.toNameResId(): Int = when (this) {
    LocalTitleLanguagePreference.Canonical -> R.string.preference_titles_canonical
    LocalTitleLanguagePreference.Romanized -> R.string.preference_titles_romanized
    LocalTitleLanguagePreference.English -> R.string.preference_titles_english
}

private fun LocalSfwFilterPreference.toNameResId(): Int = when (this) {
    LocalSfwFilterPreference.SFW -> R.string.preference_sfw_filter_hide_all
    LocalSfwFilterPreference.NSFW_SOMETIMES -> R.string.preference_sfw_filter_limit_to_feed
    LocalSfwFilterPreference.NSFW_EVERYWHERE -> R.string.preference_sfw_filter_show_all
}

private fun LocalSfwFilterPreference?.toSummaryResId(): Int = when (this) {
    LocalSfwFilterPreference.SFW -> R.string.preference_adult_content_description_sfw
    LocalSfwFilterPreference.NSFW_SOMETIMES -> R.string.preference_adult_content_description_sometimes
    LocalSfwFilterPreference.NSFW_EVERYWHERE -> R.string.preference_adult_content_description_everywhere
    null -> R.string.no_information
}

private fun LocalRatingSystemPreference.toNameResId(): Int = when (this) {
    LocalRatingSystemPreference.Advanced -> R.string.preference_rating_system_advanced
    LocalRatingSystemPreference.Regular -> R.string.preference_rating_system_regular
    LocalRatingSystemPreference.Simple -> R.string.preference_rating_system_simple
}

// endregion

// region Previews

@Preview(showBackground = true, name = "Settings — not logged in")
@Composable
private fun SettingsScreenPreview() {
    KitsuneTheme {
        SettingsScreen(
            uiState = SettingsUiState(appVersion = "1.0.0 (1)"),
            callbacks = SettingsCallbacks()
        )
    }
}

@Preview(showBackground = true, name = "Settings — logged in")
@Composable
private fun SettingsScreenLoggedInPreview() {
    KitsuneTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                user = LocalUser.empty("1").copy(
                    name = "Preview User",
                    slug = "preview-user",
                    country = "US",
                    sfwFilterPreference = LocalSfwFilterPreference.SFW,
                    ratingSystem = LocalRatingSystemPreference.Regular
                ),
                titles = LocalTitleLanguagePreference.Canonical,
                startFragment = StartPagePref.Home,
                rememberSearchFilters = true,
                appVersion = "1.0.0 (1)"
            ),
            callbacks = SettingsCallbacks()
        )
    }
}

// endregion
