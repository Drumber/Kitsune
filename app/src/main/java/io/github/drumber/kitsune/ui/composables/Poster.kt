package io.github.drumber.kitsune.ui.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import io.github.drumber.kitsune.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Poster(
    imageModel: Any?,
    modifier: Modifier,
    elevated: Boolean = false,
    contentDescription: String? = null
) {
    Card(
        modifier,
        elevation = if (elevated) CardDefaults.elevatedCardElevation() else CardDefaults.cardElevation()
    ) {
        GlideImage(
            model = imageModel,
            contentDescription = contentDescription,
            loading = placeholder(R.drawable.ic_insert_photo_48),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SmallPoster(
    imageModel: Any?,
    elevated: Boolean = false,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    Poster(imageModel, modifier.size(106.dp, 150.dp), elevated, contentDescription)
}

@Preview
@Composable
private fun SmallPosterPreview() {
    SmallPoster(null)
}