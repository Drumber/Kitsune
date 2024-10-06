package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    onLoginClicked: () -> Unit = {},
    onCreateAccountClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onSkipClicked: () -> Unit = {}
) {
    val backgroundGradient = listOf(
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surface
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundGradient))
            .then(modifier)
            .padding(16.dp)
    ) {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                HeaderSection()
                Spacer(modifier = Modifier.height(32.dp))
                ActionSection(
                    onLoginClicked = onLoginClicked,
                    onCreateAccountClicked = onCreateAccountClicked
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.weight(1f)
            ) {
                HeaderSection(modifier = Modifier.fillMaxWidth(0.5f))
                Spacer(modifier = Modifier.width(32.dp))
                ActionSection(
                    onLoginClicked = onLoginClicked,
                    onCreateAccountClicked = onCreateAccountClicked
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BottomSection(onBackClicked = onBackClicked, onSkipClicked = onSkipClicked)
    }
}

@Composable
private fun HeaderSection(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Login to Kitsu",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Login to your Kitsu account or create a new account to get access to all features.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionSection(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit = {},
    onCreateAccountClicked: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            onClick = onLoginClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCreateAccountClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Create account")
        }
    }
}

@Composable
private fun BottomSection(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onSkipClicked: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        TextButton(onClick = onBackClicked) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Back")
        }
        TextButton(onClick = onSkipClicked) {
            Text("Skip")
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPagePreview() {
    KitsuneTheme {
        LoginPage()
    }
}
