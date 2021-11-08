package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.aboutlibraries.LibsBuilder
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.TitlesPref
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.databinding.FragmentPreferenceBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        findPreference<ListPreference>(R.string.preference_key_titles)?.apply {
            entryValues = TitlesPref.values().map { it.name }.toTypedArray()
            setDefaultValue(KitsunePref.titles.name)
            value = KitsunePref.titles.name
            setOnPreferenceChangeListener { _, newValue ->
                KitsunePref.titles = TitlesPref.valueOf(newValue.toString())
                true
            }
        }

        val appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        findPreference<Preference>(R.string.preference_key_app_version)?.summary = appVersion

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
        view.initPaddingWindowInsetsListener(left = true, top = true, right = true, bottom = false)
        val binding = FragmentPreferenceBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        viewModel.errorMessageListener = {
            Snackbar.make(view, "Error: ${it.getMessage(requireContext())}", Snackbar.LENGTH_LONG)
                .setAction(R.string.action_dismiss) { /* dismiss */ }
                .show()
        }
    }

    private fun observeUserModel() {
        viewModel.userModel.observe(this) { user ->
            //---- Country
            findPreference<ListPreference>(R.string.preference_key_country)?.apply {
                entryValues = Locale.getISOCountries()
                entries = Locale.getISOCountries().map { Locale("", it).displayCountry }.toTypedArray()
                value = user?.country
                requireUserLoggedIn(user)
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(value, newValue, User(user?.id, country = newValue as String))
                    true
                }
            }

            //---- Adult Content
            findPreference<SwitchPreferenceCompat>(R.string.preference_key_adult_content)?.apply {
                isChecked = user?.sfwFilter?.not() ?: false
                requireUserLoggedIn(user)
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(isChecked, newValue, User(user?.id, sfwFilter = !(newValue as Boolean)))
                    true
                }
            }

            //---- Display Name
            findPreference<EditTextPreference>(R.string.preference_key_display_name)?.apply {
                text = user?.name
                requireUserLoggedIn(user)
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(text, newValue, User(user?.id, name = newValue as String))
                    true
                }
            }

            //---- Profile URL
            findPreference<EditTextPreference>(R.string.preference_key_profile_url)?.apply {
                text = user?.slug
                requireUserLoggedIn(user)
                setOnPreferenceChangeListener { _, newValue ->
                    updateUserIfChanged(text, newValue, User(user?.id, slug = newValue as String))
                    true
                }
            }
        }
    }

    private fun updateUserIfChanged(oldValue: Any, newValue: Any, user: User) {
        if (oldValue != newValue) {
            viewModel.updateUser(user)
        }
    }

    private fun Preference.requireUserLoggedIn(
        user: User?,
        @StringRes messageRes: Int = R.string.preference_not_logged_in
    ) {
        isEnabled = user != null
        if (user == null) {
            summary = getString(messageRes)
        }
    }

    private fun <T : Preference> findPreference(@StringRes preferenceKey: Int): T? {
        return findPreference(getString(preferenceKey))
    }

}