package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.databinding.ItemGroupBinding

class GroupsPagingAdapter(
    private val glide: RequestManager,
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
            glide.load(group.avatarUrl)
                .placeholder(R.drawable.ic_group_24)
                .into(binding.ivAvatar)

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
                    group.membersCount
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
