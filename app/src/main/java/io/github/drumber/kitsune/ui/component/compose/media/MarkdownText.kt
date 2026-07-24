package io.github.drumber.kitsune.ui.component.compose.media

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/**
 * Renders Kitsu post/comment content inside an `AndroidView`-hosted [TextView] using Markwon.
 *
 * Per the migration plan, this is a deliberate `AndroidView`-wrapper for now (low risk, Compose-
 * native is a follow-up); it mirrors [PostContentRenderer] and [MarkdownPreviewRenderer] exactly,
 * including HTML rendering, Glide-backed inline images, and URL autolinking.
 *
 * Two rendering modes:
 * - **HTML** (default, [isHtml] = `true`): parses `contentFormatted` — the pre-rendered
 *   server-side HTML produced by Kramdown. Matches [PostContentRenderer].
 * - **Markdown** ([isHtml] = `false`): parses raw CommonMark Markdown (preview mode).
 *   Matches [MarkdownPreviewRenderer].
 *
 * @param content  The text to render. Null or blank renders nothing (empty TextView).
 * @param isHtml   `true` to parse as sanitized HTML; `false` for raw Markdown.
 */
@Composable
fun MarkdownText(
    modifier: Modifier = Modifier,
    content: String?,
    isHtml: Boolean = true
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val markwon = remember(isHtml) {
        val builder = Markwon.builder(context)
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(LinkifyPlugin.create())
        if (isHtml) {
            builder.usePlugin(HtmlPlugin.create())
        }
        builder.build()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(textColor)
                setTextAppearance(
                    com.google.android.material.R.style.TextAppearance_Material3_BodyMedium
                )
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            val source = content?.takeIf { it.isNotBlank() }
            if (source == null) {
                textView.text = ""
            } else if (isHtml) {
                val rendered = markwon.toMarkdown(source).trimTrailingWhitespace()
                markwon.setParsedMarkdown(textView, rendered)
            } else {
                markwon.setMarkdown(textView, source)
            }
        }
    )
}

private fun android.text.Spanned.trimTrailingWhitespace(): android.text.Spanned {
    var end = length
    while (end > 0 && this[end - 1].isWhitespace()) end--
    if (end == length) return this
    return android.text.SpannableStringBuilder(this).delete(end, length)
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextHtmlPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = "<p>This is <strong>bold</strong> and <em>italic</em> HTML rendered via Markwon.</p>",
            isHtml = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextRawMarkdownPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = "This is **bold** and _italic_ Markdown preview.",
            isHtml = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextNullPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextEmptyPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = ""
        )
    }
}
