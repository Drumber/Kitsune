package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentPreferenceBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.preference.TitlesPref
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        val titlesPref = findPreference<ListPreference>(getString(R.string.preference_key_titles))
        titlesPref?.apply {
            entryValues = TitlesPref.values().map { it.name }.toTypedArray()
            setDefaultValue(KitsunePref.titles.name)
            value = KitsunePref.titles.name
            setOnPreferenceChangeListener { preference, newValue ->
                KitsunePref.titles = TitlesPref.valueOf(newValue.toString())
                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.initPaddingWindowInsetsListener(left = true, top = true, right = true, bottom = false)
        val binding = FragmentPreferenceBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

}