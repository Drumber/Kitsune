package io.github.drumber.kitsune.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.filterNotNull

/** Result keys handed back to a previous destination via its `SavedStateHandle`. */
object NavResults {
    const val LIBRARY_ENTRY_UPDATED = "library_entry_updated"
    const val REFRESH_FAVORITES = "refresh_favorites"
    const val POST_CREATED = "post_created"
}

/**
 * Navigates only while the current destination is resumed, which swallows the duplicated
 * navigation requests produced by fast double taps (the Compose equivalent of the old
 * `navigateSafe` extension).
 */
fun NavController.navigateSafe(route: Any, builder: NavOptionsBuilder.() -> Unit = {}) {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) {
        navigate(route, builder)
    }
}

/**
 * Switches between the navigation bar destinations, keeping one saved back stack per tab.
 */
fun NavController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Hands [value] back to the destination below this one on the back stack. */
fun <T : Any> NavController.setNavResult(key: String, value: T) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
}

/**
 * Observes a result previously set with [setNavResult] and consumes it, so returning to this
 * destination a second time does not replay it.
 */
@Composable
fun <T : Any> NavBackStackEntry.NavResultEffect(key: String, onResult: (T) -> Unit) {
    val savedStateHandle = savedStateHandle
    LaunchedEffect(key) {
        savedStateHandle.getStateFlow<T?>(key, null)
            .filterNotNull()
            .collect { result ->
                savedStateHandle.remove<T>(key)
                onResult(result)
            }
    }
}
