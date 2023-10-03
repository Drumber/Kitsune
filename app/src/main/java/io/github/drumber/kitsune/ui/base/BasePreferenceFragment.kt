package io.github.drumber.kitsune.ui.base

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.drumber.kitsune.databinding.CustomEditTextPreferenceBinding

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        // replace old AlertDialog from PreferenceFragmentCompat with new MaterialAlertDialog
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setIcon(preference.icon)
            .setNegativeButton(android.R.string.cancel, null)

        when (preference) {
            is EditTextPreference -> {
                val binding = CustomEditTextPreferenceBinding.inflate(layoutInflater)
                binding.textInputLayout.editText?.apply {
                    setText(preference.text)
                    requestFocus()
                    post {
                        val imm = requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                    }
                }

                builder
                    .setView(binding.root)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        val inputText = binding.textInputLayout.editText?.text?.toString()
                        if (preference.callChangeListener(inputText)) {
                            preference.text = inputText
                        }
                        dialog.dismiss()
                    }
            }

            is ListPreference -> {
                val selectedIndex = preference.findIndexOfValue(preference.value)

                builder.setSingleChoiceItems(preference.entries, selectedIndex) { dialog, index ->
                    val value = preference.entryValues[index].toString()
                    if (preference.callChangeListener(value)) {
                        preference.value = value
                    }
                    dialog.dismiss()
                }
            }

            is MultiSelectListPreference -> {
                val newValues = preference.values
                val checkedItems = preference.entryValues.map { entryValue ->
                    newValues.contains(entryValue)
                }.toBooleanArray()

                builder
                    .setMultiChoiceItems(
                        preference.entries,
                        checkedItems
                    ) { _, index, isChecked ->
                        val value = preference.entryValues[index].toString()
                        if (isChecked) {
                            newValues.add(value)
                        } else {
                            newValues.remove(value)
                        }
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        if (preference.callChangeListener(newValues)) {
                            preference.values = newValues
                        }
                    }
            }
        }

        builder.show()
    }

}