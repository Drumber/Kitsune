package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.databinding.OnRebindCallback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder.TagData
import java.util.concurrent.CopyOnWriteArrayList

class MediaRecyclerViewAdapter(
    val dataSet: CopyOnWriteArrayList<MediaAdapter>,
    private val glide: RequestManager,
    private val tagData: TagData = TagData.None,
    private val transitionNameSuffix: String? = null,
    private val listener: OnItemClickListener<MediaAdapter>? = null
) : RecyclerView.Adapter<MediaViewHolder>() {

    var overrideItemSize: MediaItemSize? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.apply {
            contentWrapper.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            overrideItemSize?.let { cardMedia.setCustomItemSize(it) }
            cardMedia.isInGridLayout = false
        }

        binding.addOnRebindCallback(object : OnRebindCallback<ItemMediaBinding>() {
            override fun onBound(binding: ItemMediaBinding) {
                // If the same media (with the same ID) is shown in more than one recyclerview,
                // the shared-element-transition won't work. To make the transition name unique again,
                // we may add a suffix (e.g. the section title on the home page).
                transitionNameSuffix?.let { binding.cardMedia.fixUniqueTransitionName(it) }
            }
        })

        return MediaViewHolder(
            binding,
            glide,
            tagData
        ) { position ->
            if (position < dataSet.size) {
                listener?.onItemClick(binding.cardMedia, dataSet[position])
            }
        }
    }

    /** Add a suffix to the transitionName if not already present. */
    private fun View.fixUniqueTransitionName(suffix: String) {
        ViewCompat.getTransitionName(this)?.let { transitionName ->
            if (!transitionName.endsWith(suffix))
                ViewCompat.setTransitionName(this, transitionName + suffix)
        }
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

}