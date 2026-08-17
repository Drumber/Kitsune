package io.github.drumber.kitsune.util.markwon

import android.content.Context
import android.widget.TextView
import io.github.drumber.kitsune.util.ui.EmojiShortcodeConverter
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin

/**
 * Renders client-authored Markdown into a [TextView] for the post composer's live preview.
 *
 * Unlike [PostContentRenderer] (which renders the server's pre-formatted HTML), this parses raw
 * Markdown with Markwon's CommonMark support, loads inline images through Coil, autolinks bare
 * URLs, and converts `:shortcode:` emoji tokens. The result approximates how the post will look
 * once published.
 */
class MarkdownPreviewRenderer(context: Context) {

    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(Coil3ImagesPlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(MovementMethodPlugin.link())
        .build()

    /** Renders [markdown] into [textView], applying emoji shortcode conversion first. */
    fun render(textView: TextView, markdown: String?) {
        markwon.setMarkdown(textView, EmojiShortcodeConverter.convert(markdown))
    }

}
