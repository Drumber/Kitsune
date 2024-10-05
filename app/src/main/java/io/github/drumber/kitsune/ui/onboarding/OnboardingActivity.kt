package io.github.drumber.kitsune.ui.onboarding

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.ui.onboarding.components.ImageSlideshow
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingActivity : BaseActivity(0) {

    private val viewModel: OnboardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val useDynamicColorTheme by KitsunePref.asLiveData(KitsunePref::useDynamicColorTheme)
                .asFlow()
                .collectAsState(initial = KitsunePref.useDynamicColorTheme)
            val darkModePreference by KitsunePref.asLiveData(KitsunePref::darkMode)
                .asFlow()
                .collectAsState(initial = KitsunePref.darkMode)

            val isDarkModeEnabled = when (darkModePreference.toInt()) {
                AppCompatDelegate.MODE_NIGHT_NO -> false
                AppCompatDelegate.MODE_NIGHT_YES -> true
                else -> isSystemInDarkTheme()
            }

            val uiState by viewModel.uiSate.collectAsState()

            KitsuneTheme(dynamicColor = useDynamicColorTheme, darkTheme = isDarkModeEnabled) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    OnboardingTour(uiState = uiState, contentPadding = innerPadding)
                }
            }
        }
    }
}

@Composable
fun OnboardingTour(
    uiState: OnboardingUiState = OnboardingUiState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val contentPaddingWithoutBottom = PaddingValues(
        start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
        top = contentPadding.calculateTopPadding(),
        end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
        bottom = 0.dp
    )
    val contentPaddingWithoutTop = PaddingValues(
        start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
        top = 0.dp,
        end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
        bottom = contentPadding.calculateBottomPadding()
    )

    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(Modifier.padding(contentPaddingWithoutBottom), uiState)
                    1 -> LoginPage(Modifier.padding(contentPaddingWithoutBottom))
                    2 -> SetupPage(Modifier.padding(contentPaddingWithoutBottom))
                }
            }
            PageIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPaddingWithoutTop),
                pagerState = pagerState
            )
        }
    }
}

@Composable
fun PageIndicator(modifier: Modifier, pagerState: PagerState) {
    Row(
        modifier
            .wrapContentHeight()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(pagerState.pageCount) { page ->
            val color = if (pagerState.currentPage == page) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
            Box(
                Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp)
            )
        }
    }
}

@Composable
fun WelcomePage(modifier: Modifier, uiState: OnboardingUiState) {
    val overlayGradient = listOf(
        MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surface
    )

    val imagePresenter = remember(uiState.backgroundImages) {
        RandomImagePresenter(uiState.backgroundImages.shuffled())
    }

    Box {
        if (imagePresenter.hasNextImage()) {
            ImageSlideshow(modifier = Modifier.fillMaxSize(), imagePresenter = imagePresenter)
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(overlayGradient))
                .then(modifier)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to Kitsune!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Discover the world of anime and manga.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column {
                FeatureItem(
                    icon = painterResource(R.drawable.ic_search_24),
                    title = "Search",
                    description = "Find your favorite anime and manga."
                )
                FeatureItem(
                    icon = painterResource(R.drawable.ic_outline_explore_24),
                    title = "Explore",
                    description = "Discover new and trending titles."
                )
                FeatureItem(
                    icon = painterResource(R.drawable.ic_add_24),
                    title = "Track",
                    description = "Keep track of what you've watched and read."
                )
            }
        }
    }
}

@Composable
fun FeatureItem(icon: Painter, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LoginPage(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Login!")
        Button(onClick = {}) {
            Text("Login")
        }
    }
}

@Composable
fun SetupPage(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Setup!")
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingTourPreview() {
    KitsuneTheme {
        OnboardingTour()
    }
}
