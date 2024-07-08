package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.databinding.ItemEpisodeBinding
import kotlin.math.max

class MediaUnitPagingAdapter(
    private val glide: RequestManager,
    private val posterUrl: String?,
    private var enableWatchedCheckbox: Boolean,
    private val listener: MediaUnitActionListener
) : PagingDataAdapter<MediaUnit, MediaUnitPagingAdapter.MediaUnitViewHolder>(MediaUnitComparator) {

    private var numberWatched = 0

    fun updateLibraryWatchCount(numberWatched: Int) {
        val oldValue = this.numberWatched
        this.numberWatched = numberWatched
        notifyItemRangeChanged(0, max(oldValue, numberWatched))
    }

    fun setIsWatchedCheckboxEnabled(isEnabled: Boolean) {
        if (isEnabled == enableWatchedCheckbox) return
        enableWatchedCheckbox = isEnabled
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaUnitViewHolder {
        return MediaUnitViewHolder(
            ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MediaUnitViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    inner class MediaUnitViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let { listener.onMediaUnitClicked(it) }
                }
            }
            binding.checkboxWatched.setOnClickListener {
                val isChecked = binding.checkboxWatched.isChecked
                getItem(bindingAdapterPosition)?.let { listener.onWatchStateChanged(it, isChecked) }
            }
        }

        fun bind(unit: MediaUnit) {
            glide.load(unit.thumbnail?.originalOrDown() ?: posterUrl)
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

            binding.apply {
                tvEpisodeTitle.text = unit.title(root.context)
                tvEpisodeNumber.text = if (unit.hasValidTitle()) {
                    unit.numberText(root.context)
                } else {
                    null
                }
                checkboxWatched.isVisible = enableWatchedCheckbox
                unit.number?.let { checkboxWatched.isChecked = it <= numberWatched }
            }
        }

    }

    interface MediaUnitActionListener {
        fun onMediaUnitClicked(mediaUnit: MediaUnit)
        fun onWatchStateChanged(mediaUnit: MediaUnit, isWatched: Boolean)
    }

    object MediaUnitComparator : DiffUtil.ItemCallback<MediaUnit>() {
        override fun areItemsTheSame(oldItem: MediaUnit, newItem: MediaUnit) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaUnit, newItem: MediaUnit) = oldItem == newItem
    }

}