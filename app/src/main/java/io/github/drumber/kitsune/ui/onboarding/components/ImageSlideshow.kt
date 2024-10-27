package io.github.drumber.kitsune.ui.onboarding.components

import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val ANIMATION_TRANSFORM_DURATION = 20000

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageSlideshow(
    modifier: Modifier = Modifier,
    imagePresenter: ImagePresenter
) {
    var currentImage by rememberSaveable(imagePresenter) { mutableStateOf(imagePresenter.getNextImage()) }
    var nextImage by rememberSaveable(imagePresenter) { mutableStateOf(imagePresenter.getNextImage()) }

    val context = LocalContext.current

    val scaleAnimation = remember { Animatable(1.1f) }
    val xAnimation = remember { Animatable(0f) }
    val yAnimation = remember { Animatable(0f) }

    var animationKey by remember { mutableIntStateOf(0) }
    if (isAnimationsEnabled()) {
        LaunchedEffect(animationKey) {
            awaitAll(
                async {
                    scaleAnimation.animateTo(
                        targetValue = 1.3f - scaleAnimation.value + 1.1f,
                        animationSpec = tween(ANIMATION_TRANSFORM_DURATION, easing = Ease)
                    )
                },
                async {
                    xAnimation.animateTo(
                        targetValue = (10f - xAnimation.value) * (if (Random.nextInt() % 2 == 0) 1 else -1),
                        animationSpec = tween(ANIMATION_TRANSFORM_DURATION, easing = LinearEasing)
                    )
                },
                async {
                    yAnimation.animateTo(
                        targetValue = (15f - yAnimation.value) * (if (Random.nextInt() % 2 == 0) 1 else -1),
                        animationSpec = tween(ANIMATION_TRANSFORM_DURATION, easing = LinearEasing)
                    )
                }
            )

            animationKey = (++animationKey) % 2
        }
    }

    LaunchedEffect(currentImage) {
        delay(8000)
        currentImage = nextImage
        nextImage = imagePresenter.getNextImage()
        Glide.with(context)
            .load(nextImage)
            .preload()
    }

    Crossfade(
        targetState = currentImage,
        label = "slide show",
        modifier = modifier.clipToBounds(),
        animationSpec = tween(3000)
    ) { image ->
        if (image != null) {
            GlideImage(
                model = image,
                contentDescription = null,
                transition = CrossFade,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scaleAnimation.value
                        scaleY = scaleX
                        translationX = xAnimation.value
                        translationY = yAnimation.value
                        transformOrigin = TransformOrigin.Center
                    }
            )
        }
    }
}

@Composable
private fun isAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        val windowAnimationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            1f
        )
        val transitionAnimationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        val animatorDurationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )

        windowAnimationScale != 0f && transitionAnimationScale != 0f && animatorDurationScale != 0f
    }
}

@Preview(showBackground = true)
@Composable
fun ImageSlideshowPreview() {
    KitsuneTheme {
        ImageSlideshow(imagePresenter = EmptyImagePresenter)
    }
}