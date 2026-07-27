package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * Renders Kitsu post/comment content as Compose-native Markdown.
 *
 * Published content uses Kitsu's server-rendered Kramdown HTML so it matches the formatting shown
 * on the website. Raw Markdown is rendered for previews and as a fallback when formatted content
 * is unavailable.
 *
 * @param content The original Markdown source.
 * @param contentFormatted The server-rendered HTML, when available.
 */
@Composable
fun MarkdownText(
    modifier: Modifier = Modifier,
    content: String?,
    contentFormatted: String? = null
) {
    val formatted = contentFormatted?.takeIf { it.isNotBlank() }
    if (formatted != null) {
        val linkColor = MaterialTheme.colorScheme.primary
        val annotatedContent = remember(formatted, linkColor) {
            AnnotatedString.fromHtml(
                htmlString = formatted,
                linkStyles = TextLinkStyles(style = SpanStyle(color = linkColor))
            ).trimTrailingWhitespace()
        }
        Text(
            text = annotatedContent,
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        return
    }

    val source = content?.takeIf { it.isNotBlank() } ?: return

    Markdown(
        content = source,
        modifier = modifier,
        colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
        typography = markdownTypography(text = MaterialTheme.typography.bodyMedium),
        imageTransformer = Coil3ImageTransformerImpl
    )
}

private fun AnnotatedString.trimTrailingWhitespace(): AnnotatedString {
    var end = length
    while (end > 0 && this[end - 1].isWhitespace()) {
        end--
    }
    return if (end == length) this else subSequence(0, end)
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextHtmlPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = "This is **bold** and _italic_ Markdown.",
            contentFormatted = """
                <p>This is <strong>bold</strong> and <em>italic</em> with a
                <a href="https://kitsu.app">link</a>.</p>
            """.trimIndent()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextRawPreview() {
    KitsuneTheme {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            content = "This is **bold** and _italic_ Markdown with a [link](https://kitsu.app)."
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
