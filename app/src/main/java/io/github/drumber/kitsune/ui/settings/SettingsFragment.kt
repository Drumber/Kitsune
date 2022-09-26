package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.aboutlibraries.LibsBuilder
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.manager.GitHubUpdateChecker
import io.github.drumber.kitsune.data.model.TitlesPref
import io.github.drumber.kitsune.data.model.user.SfwFilterPreference
import io.github.drumber.kitsune.data.model.user.User
import io.github.drumber.kitsune.databinding.FragmentPreferenceBinding
import io.github.drumber.kitsune.notification.Notifications
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModel()

    private val updateChecker: GitHubUpdateChecker by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.preference_file_key)
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        //---- Appearance
        findPreference<Preference>(R.string.preference_key_fragment_appearance)?.setOnPreferenceClickListener {
            val action =
                SettingsFragmentDirections.actionSettingsFragmentToThemePreferenceFragment()
            findNavController().navigate(action)
            true
        }

        //---- Start Fragment
        findPreference<ListPreference>(R.string.preference_key_start_fragment)?.apply {
            entryValues = arrayOf(
                R.id.main_fragment,
                R.id.search_fragment,
                R.id.library_fragment,
                R.id.profile_fragment
            ).map { it.toString() }.toTypedArray()
            value = KitsunePref.startFragment.toString()
            setSummaryProvider {
                getString(R.string.preference_start_fragment_description, entry)
            }
            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? String)?.toIntOrNull()?.let {
                    KitsunePref.startFragment = it
                    return@setOnPreferenceChangeListener true
                }
                false
            }
        }

        //---- App Version
        val appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        findPreference<Preference>(R.string.preference_key_app_version)?.apply {
            summary = appVersion
            setOnPreferenceClickListener {
                checkForNewVersion()
                true
            }
        }

        //---- Open Source Libraries
        findPreference<Preference>(R.string.preference_key_open_source_libraries)?.setOnPreferenceClickListener {
            val libsBuilder = LibsBuilder()
                .withLicenseShown(true)

            val bundle = Bundle()
            bundle.putSerializable("data", libsBuilder)

            findNavController().navigate(R.id.action_settings_fragment_to_librariesFragment, bundle)
            true
        }

        observeUserModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.initPaddingWindowInsetsListener(left = true, top = true, right = true, consume = false)
        val binding = FragmentPreferenceBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

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

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.initPaddingWindowInsetsListener(bottom = true)
        return recyclerView
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
                    Notifications.showNewVersion(requireContext(), result.release)
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
            println("#### User model changed $user")
            //---- Title Language Preference
            findPreference<ListPreference>(R.string.preference_key_titles)?.apply {
                entryValues = TitlesPref.values().map { it.name }.toTypedArray()
                setDefaultValue(KitsunePref.titles.name)
                value = KitsunePref.titles.name
                setOnPreferenceChangeListener { _, newValue ->
                    val titlesPref = TitlesPref.valueOf(newValue.toString())
                    KitsunePref.titles = titlesPref

                    // Title preference can be also changed without being logged in.
                    // Do only try to update the user model if logged in.
                    if (user != null) {
                        updateUserIfChanged(
                            value,
                            newValue,
                            User(user.id, titleLanguagePreference = titlesPref)
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
                    updateUserIfChanged(
                        value,
                        newValue,
                        User(user?.id, country = newValue as String)
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
                entryValues = SfwFilterPreference.values().map { it.name }.toTypedArray()
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(
                        value,
                        newValue,
                        User(
                            user?.id,
                            sfwFilterPreference = SfwFilterPreference.valueOf(newValue as String)
                        )
                    )
                    true
                }
                requireUserLoggedIn(user) {
                    val filterPreference =
                        it.value?.let { filter -> SfwFilterPreference.valueOf(filter) }
                    getString(
                        when (filterPreference) {
                            SfwFilterPreference.SFW -> R.string.preference_adult_content_description_sfw
                            SfwFilterPreference.NSFW_SOMETIMES -> R.string.preference_adult_content_description_sometimes
                            SfwFilterPreference.NSFW_EVERYWHERE -> R.string.preference_adult_content_description_everywhere
                            else -> R.string.no_information
                        }
                    )
                }
            }

            //---- Display Name
            findPreference<EditTextPreference>(R.string.preference_key_display_name)?.apply {
                text = user?.name
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(text, newValue, User(user?.id, name = newValue as String))
                    true
                }
                requireUserLoggedIn(user) { it.text }
            }

            //---- Profile URL
            findPreference<EditTextPreference>(R.string.preference_key_profile_url)?.apply {
                text = user?.slug
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(text, newValue, User(user?.id, slug = newValue as String))
                    true
                }
                requireUserLoggedIn(user) { Kitsu.USER_URL_PREFIX + it.text }
            }

            //---- Offline Library Updates
            findPreference<SwitchPreferenceCompat>(R.string.preference_key_offline_library_updates)?.apply {
                requireUserLoggedIn(user)
            }
        }
    }

    private fun updateUserIfChanged(oldValue: Any?, newValue: Any?, user: User) {
        if (oldValue != newValue) {
            viewModel.updateUser(user)
        }
    }

    private inline fun <reified T : Preference> T.requireUserLoggedIn(
        user: User?,
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

    private fun <T : Preference> findPreference(@StringRes preferenceKey: Int): T? {
        return findPreference(getString(preferenceKey))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.errorMessageListener = null
    }

}