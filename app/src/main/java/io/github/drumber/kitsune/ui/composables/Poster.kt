package io.github.drumber.kitsune.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Poster(
    imageUrl: String?,
    modifier: Modifier,
    elevated: Boolean = false,
    contentDescription: String? = null
) {
    Card(
        modifier,
        elevation = if (elevated) CardDefaults.elevatedCardElevation() else CardDefaults.cardElevation()
    ) {
        GlideImage(imageUrl, contentDescription)
    }
}

@Composable
fun SmallPoster(
    imageUrl: String?,
    elevated: Boolean = false,
    contentDescription: String? = null
) {
    Poster(imageUrl, Modifier.size(106.dp, 150.dp), elevated, contentDescription)
}