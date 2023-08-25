package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.production.Casting
import io.github.drumber.kitsune.data.model.production.Character
import io.github.drumber.kitsune.databinding.ItemCharacterBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class CharacterPagingAdapter(
    private val glide: RequestManager,
    private val characterClickListener: OnItemClickListener<Character>? = null
) : PagingDataAdapter<Casting, CharacterPagingAdapter.CharacterViewHolder>(CharacterComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        return CharacterViewHolder(
            ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(casting: Casting) {
            val imgCharacter = casting.character?.image?.original
            val imgActor = casting.person?.image?.original

            glide.load(imgCharacter)
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivCharacter)

            glide.load(imgActor)
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivActor)

            binding.apply {
                tvCharacterName.text = casting.character?.name
                tvActorName.text = casting.person?.name

                ivCharacter.isVisible = imgCharacter != null || !tvCharacterName.text.isNullOrBlank()
                ivActor.isVisible = imgActor != null || !tvActorName.text.isNullOrBlank()

                ivCharacter.setOnClickListener {
                    casting.character?.let { character ->
                        characterClickListener?.onItemClick(ivCharacter, character)
                    }
                }
            }
        }

    }

    object CharacterComparator : DiffUtil.ItemCallback<Casting>() {
        override fun areItemsTheSame(oldItem: Casting, newItem: Casting) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Casting, newItem: Casting) = oldItem == newItem
    }

}