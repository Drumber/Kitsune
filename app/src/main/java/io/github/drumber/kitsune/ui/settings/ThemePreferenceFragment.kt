package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.chibatching.kotpref.livedata.asLiveData
import com.google.android.material.color.DynamicColors
import com.google.android.material.radiobutton.MaterialRadioButton
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.databinding.FragmentThemePreferenceBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener

class ThemePreferenceFragment : Fragment(R.layout.fragment_theme_preference) {

    private val binding: FragmentThemePreferenceBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            toolbar.initWindowInsetsListener(consume = false)
            nsvContent.initPaddingWindowInsetsListener(left = true, right = true, bottom = true)
        }

        initDynamicColorSwitch()
        initThemePickerCards()
        initDarkModeRadioGroup()
        initMediaItemSizeRadioGroup()
    }

    private fun initDynamicColorSwitch() {
        binding.switchDynamicColor.apply {
            isVisible = DynamicColors.isDynamicColorAvailable()
            isChecked = KitsunePref.useDynamicColorTheme
            setOnCheckedChangeListener { _, isChecked ->
                KitsunePref.useDynamicColorTheme = isChecked
            }
        }
    }

    private fun initThemePickerCards() {
        KitsunePref.asLiveData(KitsunePref::appTheme).observe(viewLifecycleOwner) { theme ->
            binding.apply {
                cardThemeDefault.isChecked = theme == AppTheme.DEFAULT
                cardThemePurple.isChecked = theme == AppTheme.PURPLE
            }
        }

        binding.apply {
            layoutThemeCardsContainer.isVisible = !KitsunePref.useDynamicColorTheme

            cardThemeDefault.setOnClickListener {
                KitsunePref.appTheme = AppTheme.DEFAULT
            }

            cardThemePurple.setOnClickListener {
                KitsunePref.appTheme = AppTheme.PURPLE
            }
        }
    }

    private fun initDarkModeRadioGroup() {
        val darkModeStrings = resources.getStringArray(R.array.preference_dark_mode_entries)
        val darkModeValues = resources.getStringArray(R.array.preference_dark_mode_values)
        val currentValueIndex = darkModeValues.indexOfFirst { it == KitsunePref.darkMode }

        darkModeStrings.forEachIndexed { index, string ->
            val radioBtn = MaterialRadioButton(requireContext())
            radioBtn.apply {
                text = string

                setOnCheckedChangeListener { _, isChecked ->
                    val mode = darkModeValues[index]
                    if (isChecked && KitsunePref.darkMode != mode) {
                        KitsunePref.darkMode = mode
                        AppCompatDelegate.setDefaultNightMode(mode.toInt())
                    }
                }
            }
            binding.radioGroupDarkMode.addView(radioBtn)
        }
        (binding.radioGroupDarkMode.getChildAt(currentValueIndex) as RadioButton).isChecked = true
    }

    private fun initMediaItemSizeRadioGroup() {
        binding.radioGroupMediaItemSize.apply {
            (getChildAt(KitsunePref.mediaItemSize.ordinal) as RadioButton).isChecked = true
            setOnCheckedChangeListener { _, checkedId ->
                val selectedIndex = indexOfChild(findViewById(checkedId))
                val selectedSize = MediaItemSize.values()[selectedIndex]
                KitsunePref.mediaItemSize = selectedSize
            }
        }
    }

}