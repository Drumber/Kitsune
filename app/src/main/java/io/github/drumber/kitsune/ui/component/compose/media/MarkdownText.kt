package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * Renders Kitsu post/comment content as Compose-native Markdown.
 *
 * Backed by `multiplatform-markdown-renderer`, so it carries no Android `View` dependency and is
 * reusable from Compose Multiplatform. Inline images load through the app's Coil `ImageLoader`.
 *
 * Kitsu stores the author's original CommonMark in `content` and a server-rendered HTML copy in
 * `contentFormatted`; this composable always renders the Markdown source, which the previous
 * Markwon-based renderer only did in preview mode.
 *
 * @param content The Markdown to render. Null or blank renders nothing.
 */
@Composable
fun MarkdownText(
    modifier: Modifier = Modifier,
    content: String?
) {
    val source = content?.takeIf { it.isNotBlank() } ?: return

    Markdown(
        content = source,
        modifier = modifier,
        colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
        typography = markdownTypography(text = MaterialTheme.typography.bodyMedium),
        imageTransformer = Coil3ImageTransformerImpl
    )
}

@Preview(showBackground = true)
@Composable
private fun MarkdownTextPreview() {
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
