package io.github.drumber.kitsune.ui.library_new.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryEntryRatingButton(
    hasRating: Boolean,
    ratingFormatted: String?,
    onClick: () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(stringResource(R.string.hint_rating)) } },
        state = rememberTooltipState()
    ) {
        if (hasRating) {
            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(ratingFormatted ?: "-")
                Spacer(Modifier.width(4.dp))
                Icon(
                    painterResource(R.drawable.ic_star_24),
                    contentDescription = null
                )
            }
        } else {
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    painterResource(R.drawable.ic_star_outline_24),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
private fun LibraryEntryRatingButtonWithRatingPreview() {
    LibraryEntryRatingButton(true, "5", {})
}

@Preview
@Composable
private fun LibraryEntryRatingButtonPreview() {
    LibraryEntryRatingButton(false, null, {})
}