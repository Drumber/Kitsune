package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.databinding.ItemSingleCharacterBinding

class CharacterAdapter(
    private val listener: OnItemClickListener<Character>? = null
) : ListAdapter<Character, CharacterAdapter.SingleCharacterViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleCharacterViewHolder {
        return SingleCharacterViewHolder(
            ItemSingleCharacterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SingleCharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SingleCharacterViewHolder(private val binding: ItemSingleCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Character) {
            binding.cardCharacter.setOnClickListener {
                listener?.onItemClick(binding.cardCharacter, character)
            }

            binding.ivCharacter.load(character.image?.originalOrDown())

            binding.tvName.text = character.name
        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Character>() {
            override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean =
                oldItem == newItem
        }
    }

}