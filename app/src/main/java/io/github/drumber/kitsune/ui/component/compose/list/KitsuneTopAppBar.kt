package io.github.drumber.kitsune.ui.component.compose.list

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A standard top app bar styled for Kitsune.
 *
 * Wraps Material 3's [TopAppBar] with Kitsune defaults. Connect to a scroll container via
 * [scrollBehavior] to get the standard collapsed/pinned effect.
 *
 * Example with back navigation:
 * ```
 * val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
 * Scaffold(
 *     topBar = {
 *         KitsuneTopAppBar(
 *             title = { Text("Notifications") },
 *             navigationIcon = {
 *                 KitsuneBackButton(onNavigateUp = { navController.navigateUp() })
 *             },
 *             scrollBehavior = scrollBehavior
 *         )
 *     },
 *     modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
 * ) { … }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsuneTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

/**
 * A large (collapsing) top app bar styled for Kitsune. The title collapses into the toolbar
 * line as the user scrolls down.
 *
 * Typically paired with [TopAppBarDefaults.exitUntilCollapsedScrollBehavior].
 *
 * Example:
 * ```
 * val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
 * Scaffold(
 *     topBar = {
 *         KitsuneCollapsingTopAppBar(
 *             title = { Text("Library") },
 *             navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
 *             scrollBehavior = scrollBehavior
 *         )
 *     },
 *     modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
 * ) { … }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsuneCollapsingTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

/**
 * A back-navigation icon button intended for use as the [KitsuneTopAppBar] `navigationIcon`.
 */
@Composable
fun KitsuneBackButton(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onNavigateUp, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.action_back)
        )
    }
}

// region Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopAppBar — with back button")
@Composable
private fun KitsuneTopAppBarPreview() {
    KitsuneTheme {
        KitsuneTopAppBar(
            title = { Text("Notifications") },
            navigationIcon = { KitsuneBackButton(onNavigateUp = {}) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopAppBar — no navigation")
@Composable
private fun KitsuneTopAppBarNoNavPreview() {
    KitsuneTheme {
        KitsuneTopAppBar(title = { Text("Home") })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "CollapsingTopAppBar — expanded")
@Composable
private fun KitsuneCollapsingTopAppBarPreview() {
    KitsuneTheme {
        KitsuneCollapsingTopAppBar(
            title = { Text("Library") },
            navigationIcon = { KitsuneBackButton(onNavigateUp = {}) }
        )
    }
}

// endregion
