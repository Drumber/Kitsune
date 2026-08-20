package io.github.drumber.kitsune.util.markwon

import android.content.Context
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri
import androidx.navigation.findNavController
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.ui.EmojiShortcodeConverter
import io.github.drumber.kitsune.util.ui.NonScrollingLinkMovementMethod
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin

/**
 * Renders Kitsu post/comment content into a [TextView].
 *
 * Kitsu serves a pre-rendered, sanitized HTML representation of the content (`content_formatted`)
 * produced server-side by Kramdown + HTML::Pipeline. We render that HTML with Markwon's
 * [HtmlPlugin], loading inline images through Coil and autolinking bare URLs, which mirrors the
 * formatting shown on the website without re-parsing Markdown on the client.
 */
class PostContentRenderer(context: Context) {

    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(Coil3ImagesPlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(MovementMethodPlugin.create(NonScrollingLinkMovementMethod.instance))
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.linkResolver(CustomLinkResolver())
            }

            override fun afterSetText(textView: TextView) {
                // The MovementMethod will set the TextView to focusable.
                // Revert to non-focusable to allow parents to consume touch events for e.g. ripple effect.
                textView.isFocusable = false
                textView.isClickable = false
                textView.isLongClickable = false
            }
        })
        .build()

    /**
     * Renders [formatted] HTML into [textView]. Falls back to [plain] text when no formatted
     * content is available.
     */
    fun render(textView: TextView, formatted: String?, plain: String?) {
        val source = formatted?.takeIf { it.isNotBlank() }
        if (source != null) {
            val rendered = markwon.toMarkdown(EmojiShortcodeConverter.convert(source))
            markwon.setParsedMarkdown(textView, rendered.trimTrailingWhitespace())
        } else {
            textView.text = EmojiShortcodeConverter.convert(plain).trim()
        }
    }

    /**
     * Block HTML elements (e.g. `<p>`) cause Markwon to append trailing newlines, which render as
     * an empty line below the content. Strip any trailing whitespace while preserving the spans.
     */
    private fun Spanned.trimTrailingWhitespace(): Spanned {
        var end = length
        while (end > 0 && this[end - 1].isWhitespace()) {
            end--
        }
        if (end == length) return this
        return SpannableStringBuilder(this).delete(end, length)
    }

    private class CustomLinkResolver : LinkResolverDef() {
        override fun resolve(view: View, link: String) {
            val uri = try {
                getAbsoluteUri(link)
            } catch (e: Exception) {
                logE("Failed to resolve absolute URL for '$link'.", e)
                return super.resolve(view, link)
            }

            // navigate to details or user fragment with deeplink
            if (uri.host == Kitsu.API_HOST && uri.path?.matches(Regex("^/(users|anime|manga)/[\\w._-]+")) == true) {
                try {
                    return view.findNavController().navigate(uri)
                } catch (e: Exception) {
                    logE("Failed to navigate with deeplink '$uri'.", e)
                }
            }
            super.resolve(view, link)
        }

        private fun getAbsoluteUri(link: String): Uri {
            if (link.startsWith("https://", true) || link.startsWith("http://", true)) {
                return link.toUri()
            }
            return "${Kitsu.BASE_URL}/${link.trimStart('/')}".toUri()
        }
    }
}
