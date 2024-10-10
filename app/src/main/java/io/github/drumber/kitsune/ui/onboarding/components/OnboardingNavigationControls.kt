package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun OnboardingNavigationControls(
    modifier: Modifier = Modifier,
    hideNextButton: Boolean = false,
    onBackClicked: () -> Unit = {},
    onNextClicked: () -> Unit = {},
    backText: String = stringResource(R.string.action_back),
    nextText: String = stringResource(R.string.action_next)
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        TextButton(onClick = onBackClicked) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(backText)
        }
        if (!hideNextButton) {
            TextButton(onClick = onNextClicked) {
                Text(nextText)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingNavigationControlsPreview() {
    KitsuneTheme {
        OnboardingNavigationControls()
    }
}