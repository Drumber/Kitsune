package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPage(modifier: Modifier = Modifier) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = { Text("First Setup") },
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .then(modifier)
                .padding(16.dp)
        ) {
            PreferenceCard(
                title = { Text("Check for Updates") },
                description = { Text("Check for new app releases on GitHub.") },
                action = {
                    Switch(
                        checked = false,
                        onCheckedChange = {}
                    )
                },
                onClick = {}
            )
            Spacer(Modifier.height(12.dp))
            PreferenceCard(
                title = { Text("Title Language") },
                description = { Text("Select your preferred language for titles.") },
                onClick = {}
            )
            Spacer(Modifier.height(12.dp).weight(1f))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Finish Setup")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupPagePreview() {
    KitsuneTheme {
        SetupPage()
    }
}