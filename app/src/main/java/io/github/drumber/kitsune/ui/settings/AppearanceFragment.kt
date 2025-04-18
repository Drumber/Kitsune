package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.domain.work.UpdateLibraryWidgetUseCase
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BasePreferenceFragment
import org.koin.android.ext.android.inject

class AppearanceFragment : BasePreferenceFragment(R.string.nav_appearance) {

    private val updateLibraryWidget: UpdateLibraryWidgetUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.preference_file_key)
        setPreferencesFromResource(R.xml.appearance_preferences, rootKey)

        //---- Dynamic Color Theme
        findPreference<SwitchPreferenceCompat>(R.string.preference_key_dynamic_color_theme)?.apply {
            isVisible = DynamicColors.isDynamicColorAvailable()
            setOnPreferenceChangeListener { _, newValue ->
                KitsunePref.useDynamicColorTheme = newValue as Boolean
                updateLibraryWidget(context)
                true
            }
        }

        //---- App Theme
        findPreference<ThemePickerPreference>(R.string.preference_key_app_theme)?.apply {
            isEnabled = !KitsunePref.useDynamicColorTheme
            val themeEntries = getThemePreferenceEntries()
            setThemeEntries(themeEntries)
            setSelectedTheme(KitsunePref.appTheme.toThemeEntry())
            setOnPreferenceChangeListener { _, newValue ->
                val themeIndex = themeEntries.indexOf(newValue as ThemePickerPreference.ThemeEntry)
                KitsunePref.appTheme = AppTheme.entries[themeIndex]
                updateLibraryWidget(context)
                true
            }
        }

        //---- Dark Mode
        findPreference<ListPreference>(R.string.preference_key_dark_mode)?.apply {
            value = KitsunePref.darkMode
            setOnPreferenceChangeListener { _, newValue ->
                if (KitsunePref.darkMode != newValue) {
                    KitsunePref.darkMode = newValue as String
                    AppCompatDelegate.setDefaultNightMode(newValue.toInt())
                }
                true
            }
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

    private fun getThemePreferenceEntries(): List<ThemePickerPreference.ThemeEntry> {
        return AppTheme.entries.map { it.toThemeEntry() }
    }

    private fun AppTheme.toThemeEntry() = when (this) {
        AppTheme.DEFAULT -> ThemePickerPreference.ThemeEntry(
            name = R.string.preference_app_theme_default,
            primaryColor = R.color.md_theme_primary,
            secondaryColor = R.color.md_theme_secondary,
            surfaceColor = R.color.md_theme_surface
        )

        AppTheme.PURPLE -> ThemePickerPreference.ThemeEntry(
            name = R.string.preference_app_theme_purple,
            primaryColor = R.color.md_purple_theme_primary,
            secondaryColor = R.color.md_purple_theme_secondary,
            surfaceColor = R.color.md_purple_theme_surface
        )
        
        AppTheme.BLUE -> ThemePickerPreference.ThemeEntry(
            name = R.string.preference_app_theme_blue,
            primaryColor = R.color.md_blue_theme_primary,
            secondaryColor = R.color.md_blue_theme_secondary,
            surfaceColor = R.color.md_blue_theme_surface
        )

        AppTheme.GREEN -> ThemePickerPreference.ThemeEntry(
            name = R.string.preference_app_theme_green,
            primaryColor = R.color.md_green_theme_primary,
            secondaryColor = R.color.md_green_theme_secondary,
            surfaceColor = R.color.md_green_theme_surface
        )
    }

}