package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.DynamicColors
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BasePreferenceFragment

class AppearanceFragment : BasePreferenceFragment(R.string.nav_appearance) {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.preference_file_key)
        setPreferencesFromResource(R.xml.appearance_preferences, rootKey)

        //---- Dynamic Color Theme
        findPreference<SwitchPreferenceCompat>(R.string.preference_key_dynamic_color_theme)?.apply {
            isVisible = DynamicColors.isDynamicColorAvailable()
            setOnPreferenceChangeListener { _, newValue ->
                KitsunePref.useDynamicColorTheme = newValue as Boolean
                true
            }
        }

        //---- Dark Mode
        findPreference<ListPreference>(R.string.preference_key_dark_mode)
            ?.setOnPreferenceChangeListener { _, newValue ->
                if (KitsunePref.darkMode != newValue) {
                    KitsunePref.darkMode = newValue as String
                    AppCompatDelegate.setDefaultNightMode(newValue.toInt())
                }
                true
            }

        //---- OLED Black Mode
        findPreference<SwitchPreferenceCompat>(R.string.preference_key_oled_black_mode)?.apply {
            isEnabled = !KitsunePref.useDynamicColorTheme
            setOnPreferenceChangeListener { _, newValue ->
                KitsunePref.oledBlackMode = newValue as Boolean
                true
            }
        }

        //---- Media Item Size
        findPreference<ListPreference>(R.string.preference_key_media_item_size)?.apply {
            entryValues = MediaItemSize.entries.map { it.name }.toTypedArray()
            value = KitsunePref.mediaItemSize.name
            setOnPreferenceChangeListener { _, newValue ->
                KitsunePref.mediaItemSize = MediaItemSize.valueOf(newValue as String)
                true
            }
        }
    }

}