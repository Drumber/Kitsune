package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.infrastructure.production.Character
import io.github.drumber.kitsune.databinding.ItemSingleCharacterBinding
import io.github.drumber.kitsune.domain.model.ui.media.originalOrDown
import java.util.concurrent.CopyOnWriteArrayList

class CharacterAdapter(
    val dataSet: CopyOnWriteArrayList<Character>,
    private val glide: RequestManager,
    private val listener: OnItemClickListener<Character>? = null
) : RecyclerView.Adapter<CharacterAdapter.SingleCharacterViewHolder>() {

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
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    inner class SingleCharacterViewHolder(val binding: ItemSingleCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Character) {
            binding.cardCharacter.setOnClickListener {
                listener?.onItemClick(binding.cardCharacter, character)
            }

            glide.load(character.image?.originalOrDown())
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivCharacter)

            binding.tvName.text = character.name
        }

    }

}