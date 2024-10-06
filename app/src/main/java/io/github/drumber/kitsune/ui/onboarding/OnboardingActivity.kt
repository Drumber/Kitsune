package io.github.drumber.kitsune.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.ui.onboarding.components.LoginPage
import io.github.drumber.kitsune.ui.onboarding.components.SetupPage
import io.github.drumber.kitsune.ui.onboarding.components.WelcomePage
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingActivity : BaseActivity(0) {

    private val viewModel: OnboardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))

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

    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        modifier = Modifier.padding(contentPaddingWithoutBottom),
                        uiState = uiState,
                        onNextClicked = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )

                    1 -> LoginPage(
                        modifier = Modifier.padding(contentPaddingWithoutBottom)
                    )

                    2 -> SetupPage(
                        modifier = Modifier.padding(contentPaddingWithoutBottom)
                    )
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

@Preview(showBackground = true)
@Composable
fun OnboardingTourPreview() {
    KitsuneTheme {
        OnboardingTour()
    }
}
