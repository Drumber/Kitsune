package io.github.drumber.kitsune.util.ui

import android.content.Intent
import android.net.Uri
import androidx.core.view.isVisible
import coil3.ImageLoader
import coil3.dispose
import coil3.load
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Embed
import io.github.drumber.kitsune.databinding.ViewEmbedBinding
import io.github.drumber.kitsune.util.logE

/**
 * Binds a server-resolved [Embed] (Tenor GIF, YouTube video, Twitter card, generic link preview)
 * into a reusable [ViewEmbedBinding]. Animated GIFs play inline via Glide; video embeds show a play
 * overlay; link/card embeds show title, description and site. Tapping the card opens the embed url.
 */
object EmbedBinder {

    fun bind(binding: ViewEmbedBinding, imageLoader: ImageLoader, embed: Embed?, visible: Boolean) {
        val hasContent = embed != null &&
                (!embed.imageUrl.isNullOrBlank() || !embed.title.isNullOrBlank())
        val show = visible && hasContent
        binding.root.isVisible = show

        if (!show || embed == null) {
            binding.root.setOnClickListener(null)
            binding.ivEmbedImage.dispose()
            binding.ivEmbedImage.setImageResource(R.drawable.default_placeholder)
            return
        }

        val hasImage = !embed.imageUrl.isNullOrBlank()
        binding.layoutEmbedImage.isVisible = hasImage
        if (hasImage) {
            binding.ivEmbedImage.load(embed.imageUrl, imageLoader = imageLoader)
        } else {
            binding.ivEmbedImage.dispose()
            binding.ivEmbedImage.setImageResource(R.drawable.default_placeholder)
        }
        binding.ivEmbedPlay.isVisible = embed.isVideo && !embed.isGif

        val showText = !embed.isGif && !embed.title.isNullOrBlank()
        binding.layoutEmbedText.isVisible = showText
        if (showText) {
            binding.tvEmbedSite.apply {
                isVisible = !embed.siteName.isNullOrBlank()
                text = embed.siteName
            }
            binding.tvEmbedTitle.text = embed.title
            binding.tvEmbedDescription.apply {
                isVisible = !embed.description.isNullOrBlank()
                text = embed.description
            }
        }

        val targetUrl = embed.url ?: embed.videoUrl
        binding.root.setOnClickListener { view ->
            if (targetUrl.isNullOrBlank()) return@setOnClickListener
            try {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
            } catch (e: Exception) {
                logE("Failed to open embed url: $targetUrl", e)
            }
        }
    }

}
