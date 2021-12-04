package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import java.util.concurrent.CopyOnWriteArrayList

class MediaRecyclerViewAdapter(
    val dataSet: CopyOnWriteArrayList<MediaAdapter>,
    private val glide: GlideRequests,
    private val listener: OnItemClickListener<MediaAdapter>? = null
) : RecyclerView.Adapter<MediaViewHolder>() {

    var overrideWidth: Int? = null
    var overrideHeight: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.apply {
            contentWrapper.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            cardMedia.layoutParams.apply {
                overrideWidth?.let { width = it }
                overrideHeight?.let { height = it }
            }
        }
        return MediaViewHolder(
            binding,
            glide
        ) { position ->
            if (position < dataSet.size) {
                listener?.onItemClick(dataSet[position])
            }
        }
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

}