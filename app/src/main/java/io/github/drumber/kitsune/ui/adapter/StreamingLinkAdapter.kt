package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.StreamingLogo
import io.github.drumber.kitsune.data.presentation.model.media.streamer.StreamingLink
import io.github.drumber.kitsune.databinding.ItemStreamerBinding
import java.util.concurrent.CopyOnWriteArrayList

class StreamingLinkAdapter(
    val dataSet: CopyOnWriteArrayList<StreamingLink> = CopyOnWriteArrayList(),
    private val glide: RequestManager,
    private val listener: OnItemClickListener<StreamingLink>? = null
) : RecyclerView.Adapter<StreamingLinkAdapter.StreamingLinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamingLinkViewHolder {
        val binding = ItemStreamerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StreamingLinkViewHolder(binding) { position ->
            if (position < dataSet.size) {
                listener?.onItemClick(binding.root, dataSet[position])
            }
        }
    }

    override fun onBindViewHolder(holder: StreamingLinkViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    inner class StreamingLinkViewHolder(
        private val binding: ItemStreamerBinding,
        private val listener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener(position)
                }
            }
        }

        fun bind(streamingLink: StreamingLink) {
            val logo = streamingLink.streamer?.siteName?.let { siteName ->
                StreamingLogo.entries.find { it.name.equals(siteName, true) }?.drawable
            }
            glide.load(logo)
                .centerInside()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivLogo)

            TooltipCompat.setTooltipText(binding.root, streamingLink.streamer?.siteName)
        }

    }

}