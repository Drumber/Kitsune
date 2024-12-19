package io.github.drumber.kitsune.ui.library_new.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LibraryEntryProgressBar(
    progress: Int,
    unitCount: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / unitCount.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "animatedLibraryProgress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun LibraryEntryProgressBarPreview() {
    LibraryEntryProgressBar(progress = 3, unitCount = 12)
}