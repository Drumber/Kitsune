package io.github.drumber.kitsune.ui.profile.editprofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.algolia.instantsearch.core.hits.HitsView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.CharacterSearchResult
import io.github.drumber.kitsune.databinding.ItemCharacterSearchResultBinding
import io.github.drumber.kitsune.util.fixImageUrl

class CharacterSearchResultAdapter(private val onCharacterClicked: (CharacterSearchResult) -> Unit) :
    ListAdapter<CharacterSearchResult, CharacterSearchResultAdapter.CharacterSearchResultViewHolder>(
        DIFF_CALLBACK
    ),
    HitsView<CharacterSearchResult> {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CharacterSearchResultViewHolder {
        return CharacterSearchResultViewHolder(
            ItemCharacterSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CharacterSearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun setHits(hits: List<CharacterSearchResult>) {
        submitList(hits)
    }

    inner class CharacterSearchResultViewHolder(private val binding: ItemCharacterSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: CharacterSearchResult) {
            binding.apply {
                tvName.text = character.name
                tvMedia.text = character.primaryMediaTitle
                tvMedia.isVisible = !character.primaryMediaTitle.isNullOrBlank()

                ivCharacter.load(character.image?.originalOrDown()?.fixImageUrl()) {
                    placeholder(R.drawable.character_placeholder)
                    error(R.drawable.character_placeholder)
                    fallback(R.drawable.character_placeholder)
                }

                root.setOnClickListener {
                    onCharacterClicked(character)
                }
            }
        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CharacterSearchResult>() {
            override fun areItemsTheSame(
                oldItem: CharacterSearchResult,
                newItem: CharacterSearchResult
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CharacterSearchResult,
                newItem: CharacterSearchResult
            ): Boolean = oldItem == newItem
        }
    }

}