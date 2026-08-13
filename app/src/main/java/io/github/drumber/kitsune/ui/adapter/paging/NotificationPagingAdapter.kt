package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.presentation.model.feed.NotificationVerb
import io.github.drumber.kitsune.databinding.ItemNotificationBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.util.parseUtcDate

class NotificationPagingAdapter(
    private val glide: RequestManager,
    private val listener: OnItemClickListener<Notification>? = null
) : PagingDataAdapter<Notification, NotificationPagingAdapter.NotificationViewHolder>(
    NotificationComparator
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            val context = binding.root.context

            glide.load(notification.actorAvatarUrl)
                .placeholder(R.drawable.ic_outline_person_24)
                .into(binding.ivAvatar)

            binding.unreadIndicator.isVisible = !notification.isRead

            val actorDisplay = buildActorDisplay(context, notification)
            binding.tvSummary.text = summaryFor(context, notification.verb, actorDisplay)

            binding.tvExcerpt.apply {
                isVisible = !notification.excerpt.isNullOrBlank()
                text = notification.excerpt
            }

            binding.tvTimestamp.apply {
                val date = notification.time?.parseUtcDate()
                isVisible = date != null
                text = date?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                }
            }

            binding.root.setOnClickListener {
                listener?.onItemClick(binding.root, notification)
            }
        }

        private fun buildActorDisplay(
            context: android.content.Context,
            notification: Notification
        ): String {
            val name = notification.actorName
                ?: context.getString(R.string.feed_unknown_user)
            val others = notification.actorCount - 1
            return if (others > 0) {
                context.resources.getQuantityString(
                    R.plurals.notification_actor_and_others, others, name, others
                )
            } else {
                name
            }
        }

        private fun summaryFor(
            context: android.content.Context,
            verb: NotificationVerb,
            actorDisplay: String
        ): String {
            val resId = when (verb) {
                NotificationVerb.FOLLOWED -> R.string.notification_followed
                NotificationVerb.LIKED_POST -> R.string.notification_liked_post
                NotificationVerb.COMMENTED -> R.string.notification_commented
                NotificationVerb.REPLIED -> R.string.notification_replied
                NotificationVerb.LIKED_COMMENT -> R.string.notification_liked_comment
                NotificationVerb.LIKED_REACTION -> R.string.notification_liked_reaction
                NotificationVerb.MENTIONED -> R.string.notification_mentioned
                NotificationVerb.POSTED -> R.string.notification_posted
                NotificationVerb.AIRED -> R.string.notification_aired
                NotificationVerb.OTHER -> R.string.notification_generic
            }
            return context.getString(resId, actorDisplay)
        }
    }

    object NotificationComparator : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem == newItem
    }

}
