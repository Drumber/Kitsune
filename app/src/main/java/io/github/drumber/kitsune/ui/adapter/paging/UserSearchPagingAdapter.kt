package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.UserSearchResult
import io.github.drumber.kitsune.databinding.ItemUserSearchBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class UserSearchPagingAdapter(
    private val listener: OnItemClickListener<UserSearchResult>? = null
) : PagingDataAdapter<UserSearchResult, UserSearchPagingAdapter.UserViewHolder>(UserComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    inner class UserViewHolder(private val binding: ItemUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserSearchResult) {
            binding.root.setOnClickListener {
                listener?.onItemClick(binding.root, user)
            }

            binding.ivAvatar.load(user.avatar?.originalOrDown()) {
                placeholder(R.drawable.ic_outline_person_24)
                error(R.drawable.ic_outline_person_24)
                fallback(R.drawable.ic_outline_person_24)
            }

            binding.tvName.text = user.name
                ?: binding.root.context.getString(R.string.feed_unknown_user)

            binding.tvTitle.apply {
                isVisible = !user.title.isNullOrBlank()
                text = user.title
            }

            binding.tvFollowers.apply {
                val count = user.followersCount
                isVisible = count != null
                text = count?.let {
                    context.getString(R.string.profile_data_followers, it)
                }
            }
        }
    }

    object UserComparator : DiffUtil.ItemCallback<UserSearchResult>() {
        override fun areItemsTheSame(oldItem: UserSearchResult, newItem: UserSearchResult) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UserSearchResult, newItem: UserSearchResult) =
            oldItem == newItem
    }
}
