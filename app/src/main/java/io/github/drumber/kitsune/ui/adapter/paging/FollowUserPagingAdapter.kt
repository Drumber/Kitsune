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
import io.github.drumber.kitsune.data.presentation.model.user.FollowUser
import io.github.drumber.kitsune.databinding.ItemFollowUserBinding
import io.github.drumber.kitsune.ui.profile.follow.FollowButtonState

class FollowUserPagingAdapter(
    private val onUserClick: ((String) -> Unit)? = null,
    private val onFollowClick: ((String) -> Unit)? = null,
    private val onBindUser: ((String) -> Unit)? = null,
    private val showButtonFor: ((String) -> Boolean)? = null
) : PagingDataAdapter<FollowUser, FollowUserPagingAdapter.FollowUserViewHolder>(FollowUserComparator) {

    private val followStates = mutableMapOf<String, FollowButtonState>()

    /** Updates the follow button state for the given user and refreshes its row. */
    fun setFollowState(userId: String, state: FollowButtonState) {
        if (followStates[userId] == state) return
        followStates[userId] = state
        val index = snapshot().items.indexOfFirst { it.userId == userId }
        if (index != -1) {
            notifyItemChanged(index, PAYLOAD_FOLLOW_STATE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUserViewHolder {
        val binding = ItemFollowUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FollowUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowUserViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onBindViewHolder(
        holder: FollowUserViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_FOLLOW_STATE)) {
            getItem(position)?.let { holder.bindFollowButtonOnly(it) }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class FollowUserViewHolder(
        private val binding: ItemFollowUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(followUser: FollowUser) {
            val context = binding.root.context

            binding.ivAvatar.load(followUser.avatarUrl) {
                placeholder(R.drawable.ic_outline_person_24)
                error(R.drawable.ic_outline_person_24)
                fallback(R.drawable.ic_outline_person_24)
            }

            binding.tvName.text = followUser.name
                ?: context.getString(R.string.feed_unknown_user)

            binding.tvTitle.apply {
                isVisible = !followUser.title.isNullOrBlank()
                text = followUser.title
            }

            binding.root.setOnClickListener {
                onUserClick?.invoke(followUser.userId)
            }

            val showButton = showButtonFor?.invoke(followUser.userId) ?: false
            binding.btnFollow.isVisible = showButton

            if (showButton) {
                onBindUser?.invoke(followUser.userId)
                bindFollowButton(followUser.userId)
            }
        }

        /** Rebinds only the follow button without touching the avatar or text. */
        fun bindFollowButtonOnly(followUser: FollowUser) {
            val showButton = showButtonFor?.invoke(followUser.userId) ?: false
            binding.btnFollow.isVisible = showButton
            if (showButton) {
                bindFollowButton(followUser.userId)
            }
        }

        private fun bindFollowButton(userId: String) {
            val state = followStates[userId] ?: FollowButtonState()
            val context = binding.root.context

            binding.btnFollow.isEnabled = !state.isProcessing && state.isResolved
            binding.btnFollow.text = if (state.isFollowing) {
                context.getString(R.string.action_unfollow)
            } else {
                context.getString(R.string.action_follow)
            }
            binding.btnFollow.setOnClickListener {
                onFollowClick?.invoke(userId)
            }
        }
    }

    object FollowUserComparator : DiffUtil.ItemCallback<FollowUser>() {
        override fun areItemsTheSame(oldItem: FollowUser, newItem: FollowUser) =
            oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: FollowUser, newItem: FollowUser) =
            oldItem == newItem
    }

    companion object {
        private const val PAYLOAD_FOLLOW_STATE = "payload_follow_state"
    }
}
