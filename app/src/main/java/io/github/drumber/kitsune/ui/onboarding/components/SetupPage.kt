package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun SetupPageAdapter(
    modifier: Modifier = Modifier,
    onFinishClicked: () -> Unit = {}
) {
    val checkForUpdatesPreference by KitsunePref
        .asLiveData(KitsunePref::checkForUpdatesOnStart)
        .asFlow()
        .collectAsState(false)
    val updateCheckForUpdatesPreference = { value: Boolean ->
        KitsunePref.checkForUpdatesOnStart = value
    }

    SetupPage(
        modifier = modifier,
        onFinishClicked = onFinishClicked,
        checkForUpdatesPreference = checkForUpdatesPreference,
        onCheckForUpdatesPreferenceChanged = updateCheckForUpdatesPreference
    )
}

@Composable
fun SetupPage(
    modifier: Modifier = Modifier,
    onFinishClicked: () -> Unit = {},
    checkForUpdatesPreference: Boolean = false,
    onCheckForUpdatesPreferenceChanged: (Boolean) -> Unit = {},
    titleLanguages: List<String> = emptyList(),
    selectedTitleLanguage: String = "",
    onTitleLanguageSelected: (String) -> Unit = {}
) {
    val backgroundGradient = listOf(
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surface
    )

    var openSelectTitleLanguageDialog by remember { mutableStateOf(false) }

    if (openSelectTitleLanguageDialog) {
        SelectTitleLanguageDialog(
            titleLanguages = titleLanguages,
            selectedTitleLanguage = selectedTitleLanguage,
            onTitleLanguageSelected = {
                onTitleLanguageSelected(it)
                openSelectTitleLanguageDialog = false
            },
            onDismiss = { openSelectTitleLanguageDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundGradient))
            .verticalScroll(state = rememberScrollState())
            .then(modifier)
            .padding(16.dp)
    ) {
        HeaderSection()
        Spacer(modifier = Modifier.height(32.dp))
        PreferenceCard(
            title = { Text("Check for Updates") },
            description = { Text("Get notified when a new release is available on GitHub.") },
            action = {
                Switch(
                    checked = checkForUpdatesPreference,
                    onCheckedChange = onCheckForUpdatesPreferenceChanged
                )
            },
            onClick = {
                onCheckForUpdatesPreferenceChanged(!checkForUpdatesPreference)
            }
        )
        Spacer(Modifier.height(12.dp))
        PreferenceCard(
            title = { Text("Title Language") },
            description = { Text("Select your preferred language for titles.") },
            onClick = {}
        )
        Spacer(
            Modifier
                .height(12.dp)
                .weight(1f)
        )
        Button(
            onClick = onFinishClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Finish Setup")
        }
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
            text = "First Setup",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Set your preferred settings to get started. You can change them anytime later in the settings.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SelectTitleLanguageDialog(
    titleLanguages: List<String>,
    selectedTitleLanguage: String,
    onTitleLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: Implement dialog
}

@Preview(showBackground = true)
@Composable
private fun SetupPagePreview() {
    KitsuneTheme {
        SetupPage()
    }
}