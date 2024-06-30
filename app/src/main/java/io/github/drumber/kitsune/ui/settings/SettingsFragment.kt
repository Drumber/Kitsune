package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.ListPreference.SimpleSummaryProvider
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.AppLocales
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.mapper.UserMapper.toNetworkRatingSystemPreference
import io.github.drumber.kitsune.data.mapper.UserMapper.toNetworkSfwFilterPreference
import io.github.drumber.kitsune.data.mapper.UserMapper.toNetworkTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.databinding.FragmentPreferenceBinding
import io.github.drumber.kitsune.domain_old.manager.GitHubUpdateChecker
import io.github.drumber.kitsune.domain_old.model.preference.StartPagePref
import io.github.drumber.kitsune.notification.Notifications
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BasePreferenceFragment
import io.github.drumber.kitsune.ui.permissions.isNotificationPermissionGranted
import io.github.drumber.kitsune.ui.permissions.requestNotificationPermission
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class SettingsFragment : BasePreferenceFragment() {

    private val viewModel: SettingsViewModel by viewModel()

    private val updateChecker: GitHubUpdateChecker by inject()

    // this result listener will be called on requesting notification permission after the
    // 'check for updates on launch' permission was changed and notification permission is not granted
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                val preference =
                    findPreference<SwitchPreferenceCompat>(R.string.preference_key_check_for_updates_on_start)
                if (isGranted) {
                    KitsunePref.flagUserDeniedNotificationPermission = false
                } else {
                    preference?.isChecked = false
                    KitsunePref.flagUserDeniedNotificationPermission = true
                    Toast.makeText(
                        requireContext(),
                        R.string.error_requires_notification_permission,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.preference_file_key)
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        //---- Appearance
        findPreference<Preference>(R.string.preference_key_fragment_appearance)?.setOnPreferenceClickListener {
            val action =
                SettingsFragmentDirections.actionSettingsFragmentToAppearanceFragment()
            findNavController().navigate(action)
            true
        }

        //---- App Language
        findPreference<ListPreference>(R.string.preference_key_language)?.apply {
            val supportedLocales = AppLocales.SUPPORTED_LOCALES
            val selectedLocale = AppCompatDelegate.getApplicationLocales()
                .getFirstMatch(supportedLocales)
            val selectedLocaleValue =
                supportedLocales.find { Locale.forLanguageTag(it).language == selectedLocale?.language }
            val languageDisplayNames = supportedLocales.map {
                Locale.forLanguageTag(it)
                    .getDisplayLanguage(selectedLocale ?: Locale.getDefault())
            }.toTypedArray()
            entryValues = arrayOf("", *supportedLocales)
            entries = arrayOf(
                getString(R.string.preference_language_default),
                *languageDisplayNames
            )
            value = selectedLocaleValue ?: ""
            setOnPreferenceChangeListener { _, newValue ->
                val localeList = when (newValue.toString()) {
                    "" -> LocaleListCompat.getEmptyLocaleList()
                    else -> LocaleListCompat.forLanguageTags(newValue.toString())
                }
                AppCompatDelegate.setApplicationLocales(localeList)
                true
            }
        }

        //---- Start Fragment
        findPreference<ListPreference>(R.string.preference_key_start_fragment)?.apply {
            entryValues = StartPagePref.entries.map { it.name }.toTypedArray()
            value = KitsunePref.startFragment.name
            setSummaryProvider {
                getString(R.string.preference_start_fragment_description, entry)
            }
            setOnPreferenceChangeListener { _, newValue ->
                KitsunePref.startFragment = StartPagePref.valueOf(newValue as String)
                true
            }
        }

        //---- Force legacy image picker
        findPreference<SwitchPreferenceCompat>(R.string.preference_key_force_legacy_image_picker)?.isVisible =
            ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(requireContext())

        //---- Check for Updates on Launch
        findPreference<SwitchPreferenceCompat>(R.string.preference_key_check_for_updates_on_start)
            ?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean && !requireContext().isNotificationPermissionGranted()) {
                    requireActivity().requestNotificationPermission(
                        requestNotificationPermissionLauncher
                    )
                    return@setOnPreferenceChangeListener false
                }
                true
            }

        //---- App Logs
        findPreference<Preference>(R.string.preference_key_app_logs)?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAppLogsFragment()
            findNavController().navigate(action)
            true
        }

        //---- App Version
        val appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        findPreference<Preference>(R.string.preference_key_app_version)?.apply {
            summary = appVersion + System.lineSeparator() +
                    getString(R.string.preference_app_version_description)
            setOnPreferenceClickListener {
                checkForNewVersion()
                true
            }
        }

        //---- Open Source Libraries
        findPreference<Preference>(R.string.preference_key_open_source_libraries)?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToLibrariesFragment()
            findNavController().navigate(action)
            true
        }

        observeUserModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPreferenceBinding.bind(view)

        viewModel.errorMessageListener = {
            Snackbar.make(view, "Error: ${it.getMessage(requireContext())}", Snackbar.LENGTH_LONG)
                .setAction(R.string.action_dismiss) { /* dismiss */ }
                .apply {
                    this.view.initMarginWindowInsetsListener(bottom = true, consume = false)
                }
                .show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.isVisible = isLoading
        }
    }

    private fun checkForNewVersion() {
        Toast.makeText(
            requireContext(),
            R.string.info_update_checking_new_version,
            Toast.LENGTH_SHORT
        ).show()

        lifecycleScope.launch {
            when (val result = updateChecker.checkForUpdates()) {
                is GitHubUpdateChecker.UpdateCheckerResult.NewVersion -> {
                    val release = result.release
                    Notifications.showNewVersion(requireContext(), release)

                    val message = getString(
                        R.string.info_update_new_version_available_text,
                        release.version
                    )
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_view) { openUrl(release.url) }
                        .apply {
                            this.view.initMarginWindowInsetsListener(bottom = true, consume = false)
                        }
                        .show()
                }

                is GitHubUpdateChecker.UpdateCheckerResult.NoNewVersion -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.info_update_no_new_version_available,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is GitHubUpdateChecker.UpdateCheckerResult.Failed -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.info_update_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeUserModel() {
        viewModel.userModel.observe(this) { user ->
            //---- Title Language Preference
            findPreference<ListPreference>(R.string.preference_key_titles)?.apply {
                entryValues = LocalTitleLanguagePreference.entries.map { it.name }.toTypedArray()
                setDefaultValue(KitsunePref.titles.name)
                value = KitsunePref.titles.name
                setOnPreferenceChangeListener { _, newValue ->
                    val titlesPref = LocalTitleLanguagePreference.valueOf(newValue.toString())
                    KitsunePref.titles = titlesPref

                    // Title preference can be also changed without being logged in.
                    // Do only try to update the user model if logged in.
                    if (user != null) {
                        updateUserIfChanged(
                            value,
                            newValue,
                            NetworkUser(
                                id = user.id,
                                titleLanguagePreference = titlesPref.toNetworkTitleLanguagePreference()
                            )
                        )
                    }
                    true
                }
            }

            //---- Country
            findPreference<ListPreference>(R.string.preference_key_country)?.apply {
                entryValues = Locale.getISOCountries()
                entries =
                    Locale.getISOCountries().map { Locale("", it).displayCountry }.toTypedArray()
                value = user?.country
                setOnPreferenceChangeListener { _, newValue ->
                    if (user == null) return@setOnPreferenceChangeListener false
                    updateUserIfChanged(
                        value,
                        newValue,
                        NetworkUser(id = user.id, country = newValue as String)
                    )
                    true
                }
                requireUserLoggedIn(user) {
                    if (it.value == null) {
                        getString(R.string.preference_country_summary_non)
                    } else {
                        val countryName = Locale("", it.value).displayName
                        getString(R.string.preference_country_summary, countryName)
                    }
                }
            }

            //---- Adult Content
            findPreference<ListPreference>(R.string.preference_key_sfw_filter)?.apply {
                value = user?.sfwFilterPreference?.name
                entryValues = LocalSfwFilterPreference.entries.map { it.name }.toTypedArray()
                setOnPreferenceChangeListener { _, newValue ->
                    if (user == null) return@setOnPreferenceChangeListener false
                    updateUserIfChanged(
                        value,
                        newValue,
                        NetworkUser(
                            id = user.id,
                            sfwFilterPreference = LocalSfwFilterPreference.valueOf(newValue as String).toNetworkSfwFilterPreference()
                        )
                    )
                    true
                }
                requireUserLoggedIn(user) {
                    val filterPreference =
                        it.value?.let { filter -> LocalSfwFilterPreference.valueOf(filter) }
                    getString(
                        when (filterPreference) {
                            LocalSfwFilterPreference.SFW -> R.string.preference_adult_content_description_sfw
                            LocalSfwFilterPreference.NSFW_SOMETIMES -> R.string.preference_adult_content_description_sometimes
                            LocalSfwFilterPreference.NSFW_EVERYWHERE -> R.string.preference_adult_content_description_everywhere
                            else -> R.string.no_information
                        }
                    )
                }
            }

            //---- Rating System
            findPreference<ListPreference>(R.string.preference_key_rating_system)?.apply {
                entryValues =
                    LocalRatingSystemPreference.entries.reversed().map { it.name }.toTypedArray()
                value = user?.ratingSystem?.name
                setOnPreferenceChangeListener { _, newValue ->
                    if (user == null) return@setOnPreferenceChangeListener false
                    updateUserIfChanged(
                        value,
                        newValue,
                        NetworkUser(
                            id = user.id,
                            ratingSystem = LocalRatingSystemPreference.valueOf(newValue as String).toNetworkRatingSystemPreference()
                        )
                    )
                    true
                }
                requireUserLoggedIn(user, summaryProvider = SimpleSummaryProvider.getInstance())
            }

            //---- Display Name
            findPreference<EditTextPreference>(R.string.preference_key_display_name)?.apply {
                text = user?.name
                setOnPreferenceChangeListener { _, newValue ->
                    if (user == null) return@setOnPreferenceChangeListener false
                    updateUserIfChanged(text, newValue, NetworkUser(id = user.id, name = newValue as String))
                    true
                }
                requireUserLoggedIn(user) { it.text }
            }

            //---- Profile URL
            findPreference<EditTextPreference>(R.string.preference_key_profile_url)?.apply {
                text = user?.slug
                setOnPreferenceChangeListener { _, newValue ->
                    if (user == null) return@setOnPreferenceChangeListener false
                    updateUserIfChanged(text, newValue, NetworkUser(id = user.id, slug = newValue as String))
                    true
                }
                requireUserLoggedIn(user) {
                    if (!it.text.isNullOrBlank())
                        Kitsu.USER_URL_PREFIX + it.text
                    else
                        getString(R.string.preference_profile_url_not_set)
                }
            }
        }
    }

    private fun updateUserIfChanged(oldValue: Any?, newValue: Any?, user: NetworkUser) {
        if (oldValue != newValue) {
            viewModel.updateUser(user)
        }
    }

    private inline fun <reified T : Preference> T.requireUserLoggedIn(
        user: LocalUser?,
        @StringRes messageRes: Int = R.string.preference_not_logged_in,
        summaryProvider: Preference.SummaryProvider<T>? = null
    ) {
        isEnabled = user != null
        if (user == null) {
            summary = getString(messageRes)
        }
        this.summaryProvider = if (user != null) {
            summaryProvider
        } else {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.errorMessageListener = null
    }

}