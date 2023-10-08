package io.github.drumber.kitsune.ui.details.characters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.drumber.kitsune.databinding.ItemCharacterFilterBinding

class CharacterFilterAdapter(
    isViewHolderVisible: Boolean,
    var languages: List<String> = emptyList(),
    var selectedLanguage: String? = null,
    private val onItemClicked: (language: String) -> Unit
) : RecyclerView.Adapter<CharacterFilterAdapter.CharacterFilterViewHolder>() {

    var isViewHolderVisible = isViewHolderVisible
        set(value) {
            val previous = field
            field = value
            when {
                previous != value && value -> notifyItemInserted(0)
                previous != value && !value -> notifyItemRemoved(0)
            }
        }

    fun notifyItemChanged() = notifyItemChanged(0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterFilterViewHolder {
        return CharacterFilterViewHolder(
            ItemCharacterFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CharacterFilterViewHolder, position: Int) {
        holder.setLanguages(languages)
        holder.setSelectedLanguage(selectedLanguage)
    }

    override fun getItemCount() = if (isViewHolderVisible) 1 else 0

    inner class CharacterFilterViewHolder(val binding: ItemCharacterFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.chipLanguage.setOnClickListener {
                val languages = languages
                val checkedItemIndex = languages.indexOf(selectedLanguage)
                MaterialAlertDialogBuilder(binding.root.context)
                    .setSingleChoiceItems(languages.toTypedArray(), checkedItemIndex) { dialog, i ->
                        if (i == checkedItemIndex) {
                            dialog.dismiss()
                            return@setSingleChoiceItems
                        }
                        val checkedLanguage = languages[i]
                        setSelectedLanguage(checkedLanguage)
                        selectedLanguage = checkedLanguage
                        onItemClicked(checkedLanguage)
                        dialog.dismiss()
                    }
                    .show()
            }
            setLanguages(languages)
            setSelectedLanguage(selectedLanguage)
        }

        fun setLanguages(languages: List<String>) {
            binding.chipLanguage.apply {
                if (!languages.contains(text)) {
                    text = languages.firstOrNull()
                }
            }
        }

        fun setSelectedLanguage(language: String?) {
            binding.chipLanguage.text = language
        }
    }

}