package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.domain.model.infrastructure.character.MediaCharacter
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.domain.model.ui.media.getString
import java.util.concurrent.CopyOnWriteArrayList

class MediaCharacterAdapter(
    val dataSet: CopyOnWriteArrayList<MediaCharacter>,
    private val glide: RequestManager,
    private val listener: OnItemClickListener<MediaCharacter>? = null
) : RecyclerView.Adapter<MediaCharacterAdapter.MediaCharacterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaCharacterViewHolder {
        return MediaCharacterViewHolder(
            ItemMediaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MediaCharacterViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    inner class MediaCharacterViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                contentWrapper.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
                cardMedia.setCustomItemSize(MediaItemSize.SMALL)
                cardMedia.isInGridLayout = false
            }
        }

        fun bind(data: MediaCharacter) {
            val mediaAdapter = data.media?.let { MediaAdapter.fromMedia(it) }
            binding.data = mediaAdapter
            binding.overlayTagText = data.role?.getString(binding.root.context)

            glide.load(mediaAdapter?.posterImage)
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

            binding.cardMedia.setOnClickListener {
                listener?.onItemClick(binding.cardMedia, data)
            }
        }

    }

}