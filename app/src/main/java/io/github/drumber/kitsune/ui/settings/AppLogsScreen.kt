package io.github.drumber.kitsune.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLogsScreen(
    logs: String?,
    onNavigateUp: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()
    var hasAutoScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(logs) {
        if (!hasAutoScrolled && logs != null && logs.isNotBlank()) {
            // Wait for the text to be laid out before scrolling to the bottom
            snapshotFlow { scrollState.maxValue }.first { it > 0 }
            scrollState.scrollTo(scrollState.maxValue)
            hasAutoScrolled = true
        }
    }

    Scaffold(
        topBar = {
            KitsuneTopAppBar(
                title = { Text(stringResource(R.string.nav_app_logs)) },
                navigationIcon = { KitsuneBackButton(onNavigateUp) },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.action_share_app_logs)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                logs == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                logs.isBlank() -> {
                    Text(
                        text = stringResource(R.string.app_logs_no_data),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Text(
                        text = logs,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// region Previews

@Preview(showBackground = true, name = "AppLogs — Loading")
@Composable
private fun AppLogsLoadingPreview() {
    KitsuneTheme {
        AppLogsScreen(
            logs = null,
            onNavigateUp = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppLogs — Empty")
@Composable
private fun AppLogsEmptyPreview() {
    KitsuneTheme {
        AppLogsScreen(
            logs = "",
            onNavigateUp = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppLogs — With logs")
@Composable
private fun AppLogsWithLogsPreview() {
    KitsuneTheme {
        val sampleLogs = "2024-01-01 12:00:00 D/Kitsune: Application started\n" +
            "2024-01-01 12:00:01 I/Kitsune: Loading user data\n" +
            "2024-01-01 12:00:02 D/Kitsune: User data loaded successfully"
        AppLogsScreen(
            logs = sampleLogs,
            onNavigateUp = {},
            onShareClick = {}
        )
    }
}

// endregion
