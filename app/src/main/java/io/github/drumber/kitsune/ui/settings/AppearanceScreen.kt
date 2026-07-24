package io.github.drumber.kitsune.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    uiState: AppearanceUiState,
    onNavigateUp: () -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onDarkModeSelected: (String) -> Unit,
    onOledBlackToggle: (Boolean) -> Unit,
    onMediaItemSizeSelected: (MediaItemSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showMediaSizeDialog by remember { mutableStateOf(false) }

    if (showDarkModeDialog) {
        DarkModeDialog(
            selectedValue = uiState.darkMode,
            onSelect = { value ->
                onDarkModeSelected(value)
                showDarkModeDialog = false
            },
            onDismiss = { showDarkModeDialog = false }
        )
    }

    if (showMediaSizeDialog) {
        MediaItemSizeDialog(
            selectedSize = uiState.mediaItemSize,
            onSelect = { size ->
                onMediaItemSizeSelected(size)
                showMediaSizeDialog = false
            },
            onDismiss = { showMediaSizeDialog = false }
        )
    }

    Scaffold(
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(stringResource(R.string.nav_appearance)) },
                navigationIcon = { KitsuneBackButton(onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        AppearancePreferenceList(
            uiState = uiState,
            paddingValues = paddingValues,
            onDynamicColorToggle = onDynamicColorToggle,
            onThemeSelected = onThemeSelected,
            onDarkModeClick = { showDarkModeDialog = true },
            onOledBlackToggle = onOledBlackToggle,
            onMediaItemSizeClick = { showMediaSizeDialog = true }
        )
    }
}

@Composable
private fun AppearancePreferenceList(
    uiState: AppearanceUiState,
    paddingValues: PaddingValues,
    onDynamicColorToggle: (Boolean) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onDarkModeClick: () -> Unit,
    onOledBlackToggle: (Boolean) -> Unit,
    onMediaItemSizeClick: () -> Unit
) {
    LazyColumn(
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isDynamicColorAvailable) {
            item {
                PreferenceSwitchRow(
                    title = stringResource(R.string.preference_dynamic_color_theme),
                    summary = stringResource(R.string.preference_dynamic_color_theme_description),
                    checked = uiState.useDynamicColorTheme,
                    onCheckedChange = onDynamicColorToggle
                )
            }
        }

        item {
            AppearanceThemePickerRow(
                title = stringResource(R.string.preference_app_theme),
                selectedTheme = uiState.appTheme,
                enabled = !uiState.useDynamicColorTheme,
                onThemeSelected = onThemeSelected
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

        item {
            PreferenceClickRow(
                title = stringResource(R.string.preference_dark_mode),
                summary = stringResource(darkModeNameResId(uiState.darkMode)),
                onClick = onDarkModeClick
            )
        }

        item {
            PreferenceSwitchRow(
                title = stringResource(R.string.preference_oled_black_mode),
                summary = stringResource(R.string.preference_oled_black_mode_description),
                checked = uiState.oledBlackMode,
                enabled = !uiState.useDynamicColorTheme,
                onCheckedChange = onOledBlackToggle
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

        item {
            PreferenceClickRow(
                title = stringResource(R.string.preference_media_item_size),
                summary = stringResource(uiState.mediaItemSize.toNameResId()),
                onClick = onMediaItemSizeClick
            )
        }
    }
}

@Composable
private fun AppearanceThemePickerRow(
    title: String,
    selectedTheme: AppTheme,
    enabled: Boolean,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            AppTheme.entries.forEach { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = theme == selectedTheme,
                    enabled = enabled,
                    onClick = { onThemeSelected(theme) }
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (primaryRes, secondaryRes, surfaceRes) = theme.toColorResIds()
    val primaryColor = colorResource(primaryRes)
    val secondaryColor = colorResource(secondaryRes)
    val surfaceColor = colorResource(surfaceRes)
    val selectedBorderColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable(enabled = enabled, onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(primaryColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(secondaryColor)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(surfaceColor)
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, selectedBorderColor, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(theme.toNameResId()),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun DarkModeDialog(
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "1" to stringResource(R.string.preference_dark_mode_light),
        "2" to stringResource(R.string.preference_dark_mode_dark),
        "-1" to stringResource(R.string.preference_dark_mode_follow_system),
        "3" to stringResource(R.string.preference_dark_mode_battery_saver)
    )
    SingleChoiceDialog(
        title = stringResource(R.string.preference_dark_mode),
        options = options,
        selected = selectedValue,
        onSelect = onSelect,
        onDismiss = onDismiss
    )
}

@Composable
private fun MediaItemSizeDialog(
    selectedSize: MediaItemSize,
    onSelect: (MediaItemSize) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        MediaItemSize.SMALL to stringResource(R.string.preference_media_item_size_small),
        MediaItemSize.MEDIUM to stringResource(R.string.preference_media_item_size_medium),
        MediaItemSize.LARGE to stringResource(R.string.preference_media_item_size_large)
    )
    SingleChoiceDialog(
        title = stringResource(R.string.preference_media_item_size),
        options = options,
        selected = selectedSize,
        onSelect = onSelect,
        onDismiss = onDismiss
    )
}

// region Helpers

private fun AppTheme.toColorResIds(): Triple<Int, Int, Int> = when (this) {
    AppTheme.DEFAULT -> Triple(
        R.color.md_theme_primary,
        R.color.md_theme_secondary,
        R.color.md_theme_surface
    )
    AppTheme.PURPLE -> Triple(
        R.color.md_purple_theme_primary,
        R.color.md_purple_theme_secondary,
        R.color.md_purple_theme_surface
    )
    AppTheme.BLUE -> Triple(
        R.color.md_blue_theme_primary,
        R.color.md_blue_theme_secondary,
        R.color.md_blue_theme_surface
    )
    AppTheme.GREEN -> Triple(
        R.color.md_green_theme_primary,
        R.color.md_green_theme_secondary,
        R.color.md_green_theme_surface
    )
}

private fun AppTheme.toNameResId(): Int = when (this) {
    AppTheme.DEFAULT -> R.string.preference_app_theme_default
    AppTheme.PURPLE -> R.string.preference_app_theme_purple
    AppTheme.BLUE -> R.string.preference_app_theme_blue
    AppTheme.GREEN -> R.string.preference_app_theme_green
}

private fun darkModeNameResId(value: String): Int = when (value) {
    "1" -> R.string.preference_dark_mode_light
    "2" -> R.string.preference_dark_mode_dark
    "-1" -> R.string.preference_dark_mode_follow_system
    "3" -> R.string.preference_dark_mode_battery_saver
    else -> R.string.preference_dark_mode_follow_system
}

private fun MediaItemSize.toNameResId(): Int = when (this) {
    MediaItemSize.SMALL -> R.string.preference_media_item_size_small
    MediaItemSize.MEDIUM -> R.string.preference_media_item_size_medium
    MediaItemSize.LARGE -> R.string.preference_media_item_size_large
}

// endregion

// region Previews

@Preview(showBackground = true, name = "Appearance — normal state")
@Composable
private fun AppearanceScreenPreview() {
    KitsuneTheme(dynamicColor = false, variant = AppTheme.DEFAULT) {
        AppearanceScreen(
            uiState = AppearanceUiState(
                isDynamicColorAvailable = true,
                useDynamicColorTheme = false,
                appTheme = AppTheme.DEFAULT,
                darkMode = "-1",
                oledBlackMode = false,
                mediaItemSize = MediaItemSize.MEDIUM
            ),
            onNavigateUp = {},
            onDynamicColorToggle = {},
            onThemeSelected = {},
            onDarkModeSelected = {},
            onOledBlackToggle = {},
            onMediaItemSizeSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Appearance — dynamic color ON (picker + OLED disabled)")
@Composable
private fun AppearanceScreenDynamicColorPreview() {
    KitsuneTheme(dynamicColor = false, variant = AppTheme.DEFAULT) {
        AppearanceScreen(
            uiState = AppearanceUiState(
                isDynamicColorAvailable = true,
                useDynamicColorTheme = true,
                appTheme = AppTheme.GREEN,
                darkMode = "2",
                oledBlackMode = false,
                mediaItemSize = MediaItemSize.LARGE
            ),
            onNavigateUp = {},
            onDynamicColorToggle = {},
            onThemeSelected = {},
            onDarkModeSelected = {},
            onOledBlackToggle = {},
            onMediaItemSizeSelected = {}
        )
    }
}

// endregion
