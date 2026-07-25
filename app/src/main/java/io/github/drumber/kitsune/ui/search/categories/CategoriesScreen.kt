package io.github.drumber.kitsune.ui.search.categories

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    hasError: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onUnselectAll: () -> Unit,
    containerFactory: (android.content.Context) -> FrameLayout
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(stringResource(R.string.title_categories)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                    }
                },
                actions = {
                    IconButton(onClick = onUnselectAll) {
                        Icon(Icons.Filled.Deselect, contentDescription = stringResource(R.string.action_unselect_all))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        CategoriesContent(
            isLoading = isLoading,
            hasError = hasError,
            onRetry = onRetry,
            containerFactory = containerFactory,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun CategoriesContent(
    isLoading: Boolean,
    hasError: Boolean,
    onRetry: () -> Unit,
    containerFactory: (android.content.Context) -> FrameLayout,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = containerFactory, modifier = Modifier.fillMaxSize())
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        if (hasError) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(stringResource(R.string.error_resource_loading))
                androidx.compose.material3.TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}
