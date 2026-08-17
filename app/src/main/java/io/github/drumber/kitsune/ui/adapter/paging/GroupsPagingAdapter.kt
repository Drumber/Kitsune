package io.github.drumber.kitsune.ui.adapter.paging

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.databinding.ItemGroupBinding

class GroupsPagingAdapter(
    private val imageLoader: ImageLoader,
    private val onGroupClick: ((Group) -> Unit)? = null
) : PagingDataAdapter<Group, GroupsPagingAdapter.GroupViewHolder>(GroupComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(
            ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group) {
            binding.ivAvatar.load(group.avatarUrl, imageLoader = imageLoader) {
                placeholder(R.drawable.ic_group_24)
                error(R.drawable.ic_group_24)
                fallback(R.drawable.ic_group_24)
            }

            binding.tvName.text = group.name

            binding.tvTagline.apply {
                val text = group.tagline?.takeUnless { it.isBlank() }
                    ?: group.about?.takeUnless { it.isBlank() }
                isVisible = text != null
                this.text = text
            }

            binding.tvMembersCount.text = binding.root.context.resources
                .getQuantityString(
                    R.plurals.group_members_count,
                    group.membersCount,
                    NumberFormat.getNumberInstance().format(group.membersCount)
                )

            binding.cardGroup.setOnClickListener { onGroupClick?.invoke(group) }
        }

    }

    object GroupComparator : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Group, newItem: Group) =
            oldItem == newItem
    }

}
