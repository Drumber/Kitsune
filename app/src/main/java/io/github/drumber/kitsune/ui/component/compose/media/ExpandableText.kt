package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A text block that collapses to [collapsedMaxLines] lines and reveals a "Read more" / "Read less"
 * toggle, matching the behaviour of `at.blogc:expandabletextview` (`ExpandableTextView`) used in
 * `section_details_description.xml` and `sheet_character_details.xml`.
 *
 * The XML equivalent set `android:maxLines="5"` and an animation of 200 ms — this component
 * mirrors both defaults.
 *
 * @param text             The text to display.
 * @param collapsedMaxLines Number of lines shown when collapsed (defaults to 5, matching XML).
 * @param modifier         Layout modifier for the outer [Column].
 */
@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String?,
    collapsedMaxLines: Int = 5
) {
    if (text.isNullOrBlank()) return

    var expanded by rememberSaveable { mutableStateOf(false) }
    var isOverflow by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) {
                    isOverflow = result.hasVisualOverflow
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 200))
        )

        if (isOverflow || expanded) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(
                        if (expanded) R.string.action_read_less else R.string.action_read_more
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandableTextCollapsedPreview() {
    KitsuneTheme {
        ExpandableText(
            modifier = Modifier.fillMaxWidth(),
            text = "In the year 2071, humanity has colonized several of the planets and moons " +
                "of the solar system leaving the now uninhabitable surface of planet Earth behind. " +
                "The Inter Solar System Police attempts to keep peace in the galaxy, aided in part by " +
                "outlaw bounty hunters, referred to as 'Cowboys'. The ragtag team aboard the spaceship " +
                "Bebop are two such individuals. Mellow and carefree Spike Spiegel is balanced by his " +
                "serious, no-nonsense partner Jet Black as the pair makes a living chasing bounties and " +
                "getting into trouble. And much more lorem ipsum text that will not fit in five lines."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandableTextShortPreview() {
    KitsuneTheme {
        ExpandableText(
            modifier = Modifier.fillMaxWidth(),
            text = "Short description that fits on a few lines."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandableTextNullPreview() {
    KitsuneTheme {
        Column {
            Text(text = "No expandable text below (null input):")
            Spacer(Modifier.height(8.dp))
            ExpandableText(modifier = Modifier.fillMaxWidth(), text = null)
            Text(text = "(nothing rendered)")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandableTextCustomMaxLinesPreview() {
    KitsuneTheme {
        ExpandableText(
            modifier = Modifier.fillMaxWidth(),
            text = "Line 1\nLine 2\nLine 3\nLine 4\nLine 5\nLine 6\nLine 7\nLine 8",
            collapsedMaxLines = 3
        )
    }
}
