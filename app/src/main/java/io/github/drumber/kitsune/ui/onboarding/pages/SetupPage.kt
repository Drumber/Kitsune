package io.github.drumber.kitsune.ui.onboarding.pages

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.onboarding.components.CustomDialog
import io.github.drumber.kitsune.ui.onboarding.components.OnboardingNavigationControls
import io.github.drumber.kitsune.ui.onboarding.components.PreferenceCard
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupPageAdapter(
    modifier: Modifier = Modifier,
    onFinishClicked: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }
    val isNotificationPermissionGranted = notificationPermissionState?.status?.isGranted ?: true
    val shouldShouldNotificationPermissionNotice = notificationPermissionState?.status?.shouldShowRationale ?: false

    val checkForUpdatesPreference by KitsunePref
        .asLiveData(KitsunePref::checkForUpdatesOnStart)
        .asFlow()
        .collectAsState(false)

    val updateCheckForUpdatesPreference: (Boolean) -> Unit = { value: Boolean ->
        if (isNotificationPermissionGranted) {
            KitsunePref.checkForUpdatesOnStart = value
        } else {
            notificationPermissionState?.launchPermissionRequest()
        }
    }

    val titleLanguages = LocalTitleLanguagePreference.entries.map { it.name }
    val selectedTitleLanguageIndex by KitsunePref.getTitleLanguageAsFlow()
        .map { it.ordinal }
        .collectAsState(KitsunePref.titles.ordinal)
    val selectTitleLanguage = { index: Int ->
        KitsunePref.titles = LocalTitleLanguagePreference.entries[index]
    }

    SetupPage(
        modifier = modifier,
        onFinishClicked = onFinishClicked,
        onBackClicked = onBack,
        showNotificationPermissionNotice = shouldShouldNotificationPermissionNotice,
        checkForUpdatesPreference = checkForUpdatesPreference,
        onCheckForUpdatesPreferenceChanged = updateCheckForUpdatesPreference,
        titleLanguages = titleLanguages,
        selectedTitleLanguageIndex = selectedTitleLanguageIndex,
        onTitleLanguageSelected = selectTitleLanguage
    )
}

@Composable
private fun SetupPage(
    modifier: Modifier = Modifier,
    onFinishClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    showNotificationPermissionNotice: Boolean = false,
    checkForUpdatesPreference: Boolean = false,
    onCheckForUpdatesPreferenceChanged: (Boolean) -> Unit = {},
    titleLanguages: List<String> = emptyList(),
    selectedTitleLanguageIndex: Int = 0,
    onTitleLanguageSelected: (Int) -> Unit = {}
) {
    val backgroundGradient = listOf(
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surface
    )

    var openSelectTitleLanguageDialog by rememberSaveable { mutableStateOf(false) }

    if (openSelectTitleLanguageDialog) {
        SelectTitleLanguageDialog(
            titleLanguages = titleLanguages,
            selectedIndex = selectedTitleLanguageIndex,
            onTitleLanguageSelected = {
                onTitleLanguageSelected(it)
                openSelectTitleLanguageDialog = false
            },
            onDismiss = { openSelectTitleLanguageDialog = false }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundGradient))
            .then(modifier)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = rememberScrollState())
                .widthIn(min = 0.dp, max = 500.dp)
                .padding(16.dp)
        ) {
            Spacer(Modifier.weight(1f))
            HeaderSection()
            Spacer(modifier = Modifier.height(32.dp))
            PreferenceCard(
                title = { Text(stringResource(R.string.onboarding_setup_updates)) },
                description = {
                    Text(stringResource(R.string.onboarding_setup_updates_description))
                    if (showNotificationPermissionNotice) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.onboarding_setup_notification_permission_notice),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
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
                title = { Text(stringResource(R.string.onboarding_setup_title_language)) },
                description = {
                    Text(stringResource(R.string.onboarding_setup_title_language_description))
                    if (selectedTitleLanguageIndex in titleLanguages.indices) {
                        Text(
                            stringResource(
                                R.string.onboarding_setup_title_language_selected,
                                titleLanguages[selectedTitleLanguageIndex]
                            )
                        )
                    }
                },
                onClick = { openSelectTitleLanguageDialog = true }
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onFinishClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.onboarding_setup_action))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OnboardingNavigationControls(
            hideNextButton = true,
            onBackClicked = onBackClicked,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
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
            text = stringResource(R.string.onboarding_setup_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SelectTitleLanguageDialog(
    titleLanguages: List<String>,
    selectedIndex: Int,
    onTitleLanguageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tmpSelectedOption by remember { mutableIntStateOf(selectedIndex) }

    CustomDialog(
        title = { Text(stringResource(R.string.onboarding_setup_title_language)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onTitleLanguageSelected(tmpSelectedOption) }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) { contentPadding ->
        Column(modifier = Modifier.selectableGroup()) {
            titleLanguages.forEachIndexed { index, language ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = index == tmpSelectedOption,
                            onClick = { tmpSelectedOption = index },
                            role = Role.RadioButton
                        )
                        .padding(contentPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = index == tmpSelectedOption,
                        onClick = null
                    )
                    Text(
                        text = language,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
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

@Preview(showBackground = true)
@Composable
private fun SelectTitleLanguageDialogPreview() {
    KitsuneTheme {
        SelectTitleLanguageDialog(
            titleLanguages = listOf("Canonical", "Romaji", "English"),
            selectedIndex = 2,
            onTitleLanguageSelected = {},
            onDismiss = {}
        )
    }
}