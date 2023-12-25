package io.github.drumber.kitsune.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.databinding.LayoutThemePickerPreferenceBinding

class ThemePickerPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private lateinit var binding: LayoutThemePickerPreferenceBinding

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = LayoutThemePickerPreferenceBinding.bind(holder.itemView)
        binding.rvThemes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ThemeAdapter(emptyArray())
        }
    }

    data class ThemeEntry(
        @StringRes val name: Int
    )
    
    class ThemeAdapter(private val themes: Array<ThemeEntry>) :
        RecyclerView.Adapter<ThemeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

    }

    class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

}