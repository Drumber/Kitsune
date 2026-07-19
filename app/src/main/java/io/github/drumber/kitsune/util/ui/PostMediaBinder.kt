package io.github.drumber.kitsune.util.ui

import androidx.core.view.isVisible
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ViewPostMediaBinding

/**
 * Binds the "about <media>" card shown below the content of a feed post. Displays the media poster,
 * the media title combined with the tagged episode/chapter (number and name) when present, and the
 * beginning of the media synopsis. Tapping it opens the media detail screen.
 */
object PostMediaBinder {

    fun bind(
        binding: ViewPostMediaBinding,
        glide: RequestManager,
        post: Post,
        visible: Boolean,
        onClick: (() -> Unit)? = null
    ) {
        val show = visible && !post.mediaTitle.isNullOrBlank()
        binding.root.isVisible = show

        if (!show) {
            glide.clear(binding.ivPostMediaPoster)
            binding.root.setOnClickListener(null)
            return
        }

        val canOpen = onClick != null && !post.mediaSlug.isNullOrBlank() && post.mediaIsAnime != null
        binding.root.isClickable = canOpen
        binding.root.setOnClickListener(if (canOpen) {
            { onClick?.invoke() }
        } else null)

        val context = binding.root.context

        glide.load(post.mediaPosterUrl)
            .placeholder(R.drawable.ic_insert_photo_48)
            .centerCrop()
            .into(binding.ivPostMediaPoster)

        val unitLabel = post.spoiledUnitNumber?.let { number ->
            val label = context.getString(
                if (post.spoiledUnitIsEpisode) R.string.feed_post_spoiled_episode
                else R.string.feed_post_spoiled_chapter,
                number
            )
            if (!post.spoiledUnitTitle.isNullOrBlank()) {
                context.getString(R.string.feed_post_unit_with_title, label, post.spoiledUnitTitle)
            } else {
                label
            }
        }

        binding.tvPostMediaTitle.text = if (unitLabel != null) {
            context.getString(R.string.feed_post_media_unit_separator, post.mediaTitle, unitLabel)
        } else {
            post.mediaTitle
        }

        binding.tvPostMediaSynopsis.apply {
            isVisible = !post.mediaSynopsis.isNullOrBlank()
            text = post.mediaSynopsis
        }
    }

}
