package io.github.drumber.kitsune.ui.profile.editprofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.core.hits.HitsView
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.CharacterSearchResult
import io.github.drumber.kitsune.databinding.ItemCharacterSearchResultBinding
import io.github.drumber.kitsune.util.fixImageUrl

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
                tvName.text = character.name
                tvMedia.text = character.primaryMediaTitle
                tvMedia.isVisible = !character.primaryMediaTitle.isNullOrBlank()

                Glide.with(root)
                    .load(character.image?.originalOrDown()?.fixImageUrl())
                    .placeholder(R.drawable.character_placeholder)
                    .into(ivCharacter)

                root.setOnClickListener {
                    onCharacterClicked(character)
                }
            }
        }

    }

}