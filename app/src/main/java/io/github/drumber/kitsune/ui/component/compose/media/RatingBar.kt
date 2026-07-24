package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A star-rating bar that supports full and half stars, matching the behaviour of
 * `me.zhanghai.android.materialratingbar.MaterialRatingBar` used in `sheet_library_rating.xml`.
 *
 * The default configuration mirrors the app's rating sheet:
 *  - 5 stars (full integer range = [0, numStars])
 *  - step size of 0.5 (half-star increments)
 *
 * In read-only mode ([onRatingChange] is null) the component is non-interactive.
 *
 * @param rating        Current rating value in [0, numStars].
 * @param numStars      Total number of stars (default 5).
 * @param stepSize      Minimum increment per tap (0.5 for half stars, 1.0 for full stars).
 * @param starSize      Size of each individual star icon.
 * @param activeColor   Color for filled star icons; defaults to the primary theme colour.
 * @param inactiveColor Color for empty star icons; defaults to onSurface with low alpha.
 * @param onRatingChange Callback invoked when the user selects a new rating. Null = read-only.
 */
@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    numStars: Int = 5,
    stepSize: Float = 0.5f,
    starSize: Dp = 32.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    onRatingChange: ((Float) -> Unit)? = null
) {
    val coercedRating = rating.coerceIn(0f, numStars.toFloat())
    val isInteractive = onRatingChange != null

    Row(
        modifier = modifier
            .then(if (isInteractive) Modifier.selectableGroup() else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (starIndex in 1..numStars) {
            val fullStarValue = starIndex.toFloat()
            val halfStarValue = starIndex - 0.5f

            // Decide which icon to draw for this position
            val icon = when {
                coercedRating >= fullStarValue -> Icons.Default.Star
                coercedRating >= halfStarValue && stepSize <= 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarOutline
            }
            val isFilled = coercedRating >= halfStarValue

            val starModifier = if (isInteractive) {
                Modifier
                    .size(starSize)
                    .selectable(
                        selected = coercedRating == fullStarValue,
                        onClick = {
                            val newRating = if (stepSize <= 0.5f && coercedRating == fullStarValue) {
                                // second tap on fully-filled star → half star
                                halfStarValue
                            } else {
                                fullStarValue
                            }
                            onRatingChange?.invoke(newRating)
                        },
                        role = Role.RadioButton
                    )
            } else {
                Modifier.size(starSize)
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFilled) activeColor else inactiveColor,
                modifier = starModifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarFullPreview() {
    KitsuneTheme {
        Column {
            RatingBar(rating = 5f, onRatingChange = {})
            Spacer(Modifier.height(8.dp))
            RatingBar(rating = 3.5f, onRatingChange = {})
            Spacer(Modifier.height(8.dp))
            RatingBar(rating = 2.5f, onRatingChange = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarZeroPreview() {
    KitsuneTheme {
        RatingBar(rating = 0f, onRatingChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarReadOnlyPreview() {
    KitsuneTheme {
        Column {
            Text(text = "Read-only 4 stars")
            Spacer(Modifier.height(4.dp))
            RatingBar(rating = 4f)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarOutOfRangePreview() {
    KitsuneTheme {
        Column {
            Text(text = "Clamped from 8 → 5")
            Spacer(Modifier.height(4.dp))
            RatingBar(rating = 8f, numStars = 5)
            Spacer(Modifier.height(4.dp))
            Text(text = "Clamped from -1 → 0")
            Spacer(Modifier.height(4.dp))
            RatingBar(rating = -1f, numStars = 5)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarInteractiveStatefulPreview() {
    var rating by remember { mutableFloatStateOf(2.5f) }
    KitsuneTheme {
        Column {
            Text(text = "Rating: $rating / 5")
            Spacer(Modifier.height(8.dp))
            RatingBar(
                rating = rating,
                onRatingChange = { rating = it },
                starSize = 40.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarSmallSizePreview() {
    KitsuneTheme {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingBar(rating = 3.5f, starSize = 16.dp)
            Spacer(Modifier.width(8.dp))
            Text(text = "3.5", style = MaterialTheme.typography.labelSmall)
        }
    }
}
