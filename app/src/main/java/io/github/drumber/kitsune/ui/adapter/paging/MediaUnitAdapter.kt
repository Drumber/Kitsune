package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.Chapter
import io.github.drumber.kitsune.data.model.resource.Episode
import io.github.drumber.kitsune.data.model.resource.MediaUnit
import io.github.drumber.kitsune.databinding.ItemEpisodeBinding
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.originalOrDown

class MediaUnitAdapter(
    private val glide: GlideRequests,
    private val posterUrl: String?
) : PagingDataAdapter<MediaUnit, MediaUnitAdapter.MediaUnitViewHolder>(MediaUnitComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaUnitViewHolder {
        return MediaUnitViewHolder(
            ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MediaUnitViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class MediaUnitViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(unit: MediaUnit) {
            glide.load(unit.thumbnail?.originalOrDown() ?: posterUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

            val title = DataUtil.getTitle(unit.titles, unit.canonicalTitle)
            // check if title is not 'Episode %d' or 'Chapter %d'
            val hasValidTitle = title != null && !"""(Chapter|Episode)\s*\d+""".toRegex()
                .matches(unit.canonicalTitle ?: "")
            val episodeNumberText = binding.root.context.getString(
                when (unit) {
                    is Chapter -> R.string.unit_chapter
                    else -> R.string.unit_episode
                }, unit.number
            )

            binding.apply {
                tvEpisodeTitle.text = if (hasValidTitle) {
                    title
                } else {
                    episodeNumberText
                }
                tvEpisodeNumber.text = if (hasValidTitle) {
                    episodeNumberText
                } else {
                    null
                }
            }
        }

    }

    object MediaUnitComparator : DiffUtil.ItemCallback<MediaUnit>() {
        override fun areItemsTheSame(oldItem: MediaUnit, newItem: MediaUnit) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaUnit, newItem: MediaUnit) = when {
            oldItem is Episode && newItem is Episode -> oldItem as Episode == newItem as Episode
            oldItem is Chapter && newItem is Chapter -> oldItem as Chapter == newItem as Chapter
            else -> oldItem.id == newItem.id
        }
    }

}