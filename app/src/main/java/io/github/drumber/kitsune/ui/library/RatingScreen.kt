package io.github.drumber.kitsune.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.ui.component.compose.media.RatingBar
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.convertFrom
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.convertToRatingTwenty
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.fromRatingTwentyTo
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.stepSize

@Composable
fun RatingScreen(
    title: String,
    ratingTwenty: Int?,
    ratingSystem: LocalRatingSystemPreference,
    onRate: (Int) -> Unit,
    onRemoveRating: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasNoRating = ratingTwenty == null || ratingTwenty == 0
    val initialRating = if (hasNoRating) 0f else ratingTwenty!!.fromRatingTwentyTo(ratingSystem)
    var rating by rememberSaveable { mutableFloatStateOf(initialRating) }
    val numStars = ratingSystem.convertFrom(20).toInt()
    val ratingTwentyValue = ratingSystem.convertToRatingTwenty(rating)
    val isInRange = ratingTwentyValue in 1..20

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        RatingTitleRow(title = title, hasRating = !hasNoRating, onRemoveRating = onRemoveRating)
        RatingBar(
            rating = rating,
            numStars = numStars,
            stepSize = ratingSystem.stepSize(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onRatingChange = { rating = it.coerceIn(0f, numStars.toFloat()) }
        )
        RatingValueLabel(rating = rating, ratingSystem = ratingSystem)
        Spacer(Modifier.height(8.dp))
        RatingButtonRow(
            hasNoRating = hasNoRating,
            isEnabled = isInRange,
            onDismiss = onDismiss,
            onRate = { onRate(ratingTwentyValue) }
        )
    }
}

@Composable
private fun RatingTitleRow(
    title: String,
    hasRating: Boolean,
    onRemoveRating: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        if (hasRating) {
            IconButton(onClick = onRemoveRating) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_remove_rating)
                )
            }
        }
    }
}

@Composable
private fun RatingValueLabel(
    rating: Float,
    ratingSystem: LocalRatingSystemPreference
) {
    val text = if (rating == 0f) {
        stringResource(R.string.library_not_rated)
    } else {
        "$rating / ${RatingSystemUtil.formatRating(20, ratingSystem)}"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

@Composable
private fun RatingButtonRow(
    hasNoRating: Boolean,
    isEnabled: Boolean,
    onDismiss: () -> Unit,
    onRate: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text(stringResource(android.R.string.cancel))
        }
        Spacer(Modifier.width(8.dp))
        TextButton(
            onClick = onRate,
            enabled = isEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                if (hasNoRating) {
                    stringResource(R.string.action_rate)
                } else {
                    stringResource(R.string.action_update_rating)
                }
            )
        }
    }
}
