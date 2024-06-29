package io.github.drumber.kitsune.ui.profile.editprofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.core.hits.HitsView
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ItemCharacterSearchResultBinding
import io.github.drumber.kitsune.domain_old.mapper.toImage
import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.CharacterSearchResult
import io.github.drumber.kitsune.domain_old.model.ui.media.originalOrDown

class CharacterSearchResultAdapter(private val onCharacterClicked: (CharacterSearchResult) -> Unit) :
    RecyclerView.Adapter<CharacterSearchResultAdapter.CharacterSearchResultViewHolder>(),
    HitsView<CharacterSearchResult> {

    private val characters = mutableListOf<CharacterSearchResult>()

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
        holder.bind(characters[position])
    }

    override fun getItemCount(): Int = characters.size

    override fun setHits(hits: List<CharacterSearchResult>) {
        characters.clear()
        characters.addAll(hits)
        notifyDataSetChanged()
    }

    inner class CharacterSearchResultViewHolder(private val binding: ItemCharacterSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: CharacterSearchResult) {
            binding.apply {
                tvName.text = character.canonicalName
                tvMedia.text = character.primaryMedia
                tvMedia.isVisible = !character.primaryMedia.isNullOrBlank()

                Glide.with(root)
                    .load(character.image?.toImage()?.originalOrDown())
                    .placeholder(R.drawable.character_placeholder)
                    .into(ivCharacter)

                root.setOnClickListener {
                    onCharacterClicked(character)
                }
            }
        }

    }

}