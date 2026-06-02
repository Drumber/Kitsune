package io.github.drumber.kitsune.util.ui

import android.content.Context
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin

/**
 * Renders Kitsu post/comment content into a [TextView].
 *
 * Kitsu serves a pre-rendered, sanitized HTML representation of the content (`content_formatted`)
 * produced server-side by Kramdown + HTML::Pipeline. We render that HTML with Markwon's
 * [HtmlPlugin], loading inline images through Glide and autolinking bare URLs, which mirrors the
 * formatting shown on the website without re-parsing Markdown on the client.
 */
class PostContentRenderer(context: Context) {

    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(GlideImagesPlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(MovementMethodPlugin.link())
        .build()

    /**
     * Renders [formatted] HTML into [textView]. Falls back to [plain] text when no formatted
     * content is available.
     */
    fun render(textView: TextView, formatted: String?, plain: String?) {
        val source = formatted?.takeIf { it.isNotBlank() }
        if (source != null) {
            markwon.setMarkdown(textView, EmojiShortcodeConverter.convert(source))
        } else {
            textView.text = EmojiShortcodeConverter.convert(plain)
        }
    }

}
