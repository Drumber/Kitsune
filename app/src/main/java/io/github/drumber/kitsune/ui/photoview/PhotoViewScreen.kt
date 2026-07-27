package io.github.drumber.kitsune.ui.photoview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

/**
 * Full screen zoomable photo viewer. Replaces the former `PhotoViewActivity`, so it can be
 * hosted by the Compose navigation graph on any platform.
 */
@Composable
fun PhotoViewScreen(
    imageUrl: String,
    title: String?,
    onClose: () -> Unit,
    onSaveImage: () -> Unit,
    onOpenInBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    val zoomableState = rememberZoomableState()
    val imageState = rememberZoomableImageState(zoomableState)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ZoomableAsyncImage(
            model = imageUrl,
            contentDescription = title,
            state = imageState,
            modifier = Modifier.fillMaxSize(),
            onClick = { controlsVisible = !controlsVisible }
        )

        if (!imageState.isImageDisplayed) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = onClose,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.Close, stringResource(R.string.action_close))
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
                IconButton(
                    onClick = onSaveImage,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Download, stringResource(R.string.action_save))
                }
                IconButton(
                    onClick = onOpenInBrowser,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.OpenInBrowser, stringResource(R.string.action_open_in_browser))
                }
            }
        }
    }
}
