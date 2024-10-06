package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.onboarding.OnboardingUiState
import io.github.drumber.kitsune.ui.onboarding.RandomImagePresenter
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun WelcomePage(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    uiState: OnboardingUiState,
    onNextClicked: () -> Unit = {}
) {
    val overlayGradient = listOf(
        MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.8f), MaterialTheme.colorScheme.surface
    )

    val imagePresenter = remember(uiState.backgroundImages) {
        RandomImagePresenter(uiState.backgroundImages.shuffled())
    }

    val isVerticalLayout = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    Box(modifier = Modifier.fillMaxSize()) {
        if (imagePresenter.hasNextImage()) {
            ImageSlideshow(modifier = Modifier.fillMaxSize(), imagePresenter = imagePresenter)
        }
        Column(
            verticalArrangement = if (isVerticalLayout) Arrangement.Bottom else Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(overlayGradient))
                .verticalScroll(state = rememberScrollState())
                .then(modifier)
                .padding(16.dp)
        ) {
            if (isVerticalLayout) {
                HeaderSection(isCompact = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT)
                Spacer(modifier = Modifier.height(32.dp))
                FeatureSection(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(32.dp))
                GetStartedButton(modifier = Modifier.fillMaxWidth(), onClick = onNextClicked)
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HeaderSection(
                        isCompact = windowSizeClass.windowHeightSizeClass != WindowHeightSizeClass.EXPANDED,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                        FeatureSection(modifier = Modifier.width(IntrinsicSize.Max))
                        Spacer(modifier = Modifier.height(32.dp))
                        GetStartedButton(
                            modifier = Modifier.fillMaxWidth(), onClick = onNextClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(modifier: Modifier = Modifier, isCompact: Boolean = false) {
    val maxLogoHeight = if (isCompact) 100.dp else 180.dp

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.wrapContentHeight()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .sizeIn(
                    maxWidth = maxLogoHeight,
                    maxHeight = maxLogoHeight,
                    minWidth = 50.dp,
                    minHeight = 50.dp
                )
                .aspectRatio(1f)
        )
        Spacer(
            modifier = if (isCompact) {
                Modifier.height(0.dp)
            } else {
                Modifier.height(2.dp)
            }
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeatureSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FeatureItem(
            icon = painterResource(R.drawable.ic_search_24),
            title = stringResource(R.string.onboarding_welcome_feature_search),
            description = stringResource(R.string.onboarding_welcome_feature_search_description)
        )
        FeatureItem(
            icon = painterResource(R.drawable.ic_outline_explore_24),
            title = stringResource(R.string.onboarding_welcome_feature_explore),
            description = stringResource(R.string.onboarding_welcome_feature_explore_description)
        )
        FeatureItem(
            icon = painterResource(R.drawable.ic_outline_bookmarks_24),
            title = stringResource(R.string.onboarding_welcome_feature_track),
            description = stringResource(R.string.onboarding_welcome_feature_track_description)
        )
    }
}

@Composable
fun FeatureItem(icon: Painter, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GetStartedButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        modifier = modifier, onClick = onClick
    ) {
        Text(text = stringResource(R.string.onboarding_welcome_action))
    }
}

@PreviewScreenSizes
@Preview(showBackground = true, heightDp = 780, widthDp = 390)
@Composable
fun WelcomePagePreview() {
    KitsuneTheme {
        WelcomePage(uiState = OnboardingUiState())
    }
}
