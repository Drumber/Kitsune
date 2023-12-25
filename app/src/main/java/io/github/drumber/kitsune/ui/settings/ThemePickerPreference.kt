package io.github.drumber.kitsune.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ItemThemeOptionBinding
import io.github.drumber.kitsune.databinding.LayoutThemePickerPreferenceBinding
import io.github.drumber.kitsune.util.extensions.getColor

class ThemePickerPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private lateinit var binding: LayoutThemePickerPreferenceBinding
    private val rvAdapter = ThemeAdapter()
    private var selectedTheme: ThemeEntry? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = LayoutThemePickerPreferenceBinding.bind(holder.itemView)
        binding.tvTitle.text = title
        binding.rvThemes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = rvAdapter
        }
    }

    fun setThemeEntries(entries: List<ThemeEntry>) {
        rvAdapter.apply {
            themes.clear()
            themes.addAll(entries)
            notifyDataSetChanged()
        }
    }

    fun setSelectedTheme(theme: ThemeEntry) {
        val prevSelectedIndex = rvAdapter.themes.indexOf(selectedTheme)
        val newSelectedIndex = rvAdapter.themes.indexOf(theme)
        selectedTheme = theme
        if (prevSelectedIndex != -1)
            rvAdapter.notifyItemChanged(prevSelectedIndex)
        if (newSelectedIndex != -1)
            rvAdapter.notifyItemChanged(newSelectedIndex)
    }

    private fun onThemeCardClicked(theme: ThemeEntry) {
        if (callChangeListener(theme)) {
            setSelectedTheme(theme)
        }
    }

    data class ThemeEntry(
        @StringRes val name: Int,
        @ColorRes val primaryColor: Int,
        @ColorRes val secondaryColor: Int,
        @ColorRes val surfaceColor: Int
    )

    private inner class ThemeAdapter(val themes: MutableList<ThemeEntry> = mutableListOf()) :
        RecyclerView.Adapter<ThemeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
            return ThemeViewHolder(
                ItemThemeOptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int = themes.size

        override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
            holder.bind(themes[position])
        }
    }

    private inner class ThemeViewHolder(private val binding: ItemThemeOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(theme: ThemeEntry) {
            binding.apply {
                cardTheme.isEnabled = isEnabled
                cardTheme.isChecked = theme == selectedTheme
                cardTheme.setOnClickListener { onThemeCardClicked(theme) }
                viewPrimaryColor.setBackgroundResource(theme.primaryColor)
                viewSecondaryColor.setBackgroundResource(theme.secondaryColor)
                viewSurfaceColor.setBackgroundResource(theme.surfaceColor)
                tvName.isEnabled = isEnabled
                tvName.setText(theme.name)
                if (isEnabled) {
                    tvName.setTextColor(
                        context.theme.getColor(
                            if (theme == selectedTheme) R.attr.colorPrimary
                            else R.attr.colorOnSurface
                        )
                    )
                }
            }
        }
    }

}