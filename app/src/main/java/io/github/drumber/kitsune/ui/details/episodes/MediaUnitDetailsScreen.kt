package io.github.drumber.kitsune.ui.details.episodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaUnitDetailsScreen(mediaUnit: MediaUnit?, thumbnailUrl: String?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            GlideImage(
                model = mediaUnit?.thumbnail?.smallOrHigher() ?: thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                it.placeholder(R.drawable.ic_insert_photo_48).error(R.drawable.ic_insert_photo_48)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .drawWithContent {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                        drawContent()
                    }
                    .padding(10.dp)
            ) {
                Text(
                    text = mediaUnit?.title(context).orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (mediaUnit != null) {
            MediaUnitDetailsContent(mediaUnit = mediaUnit, context = context)
        }
    }
}

@Composable
private fun MediaUnitDetailsContent(mediaUnit: MediaUnit, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        if (mediaUnit.hasValidTitle()) {
            Text(
                text = mediaUnit.numberText(context).orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        mediaUnit.formatDate()?.let { date ->
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        mediaUnit.length(context)?.let { len ->
            Text(
                text = len,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
    if (!mediaUnit.description.isNullOrBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = mediaUnit.description.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(10.dp)
        )
    }
}
