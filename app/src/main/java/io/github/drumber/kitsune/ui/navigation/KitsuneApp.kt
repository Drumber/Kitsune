package io.github.drumber.kitsune.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.reflect.KClass

/**
 * Emits whenever the user taps the navigation item of the destination that is already shown.
 * Top level destinations collect this to scroll their content back to the top.
 */
val LocalReselectEvents = staticCompositionLocalOf<Flow<Unit>> { MutableSharedFlow() }

private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val label: Int
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = Routes.Home,
        routeClass = Routes.Home::class,
        selectedIcon = R.drawable.ic_home_24,
        unselectedIcon = R.drawable.ic_outline_home_24,
        label = R.string.nav_home
    ),
    TopLevelDestination(
        route = Routes.Feed,
        routeClass = Routes.Feed::class,
        selectedIcon = R.drawable.ic_rss_feed_24,
        unselectedIcon = R.drawable.ic_rss_feed_24,
        label = R.string.nav_feed
    ),
    TopLevelDestination(
        route = Routes.Library,
        routeClass = Routes.Library::class,
        selectedIcon = R.drawable.ic_view_list_24,
        unselectedIcon = R.drawable.ic_outline_view_list_24,
        label = R.string.nav_library
    ),
    TopLevelDestination(
        route = Routes.MyProfile,
        routeClass = Routes.MyProfile::class,
        selectedIcon = R.drawable.ic_person_24,
        unselectedIcon = R.drawable.ic_outline_person_24,
        label = R.string.nav_profile
    )
)

/** Destinations that are shown without the navigation bar / rail. */
private fun NavDestination?.hidesNavigationBar(): Boolean {
    if (this == null) return false
    return hierarchyHasRoute(Routes.SettingsGraph::class) || hierarchyHasRoute(Routes.WebView::class)
}

private fun NavDestination.hierarchyHasRoute(routeClass: KClass<*>): Boolean =
    hierarchy.any { it.hasRoute(routeClass) }

/**
 * Application shell: navigation bar (compact) or navigation rail (>= 600dp wide) around the
 * [AppNavHost], replacing `activity_main.xml` + `MainActivity`'s navigation wiring.
 */
@Composable
fun KitsuneApp(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    startDestination: Any = Routes.Home,
    navController: NavHostController = rememberNavController(),
    doubleBackToExit: Boolean = false,
    onExitRequested: () -> Unit = {}
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showNavigation = !currentDestination.hidesNavigationBar()
    val reselectEvents = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

    BackHandler(enabled = doubleBackToExit && navController.previousBackStackEntry == null) {
        onExitRequested()
    }

    CompositionLocalProvider(LocalReselectEvents provides reselectEvents as SharedFlow<Unit>) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            val onItemClick: (TopLevelDestination, Boolean) -> Unit = { destination, isSelected ->
                if (isSelected) {
                    reselectEvents.tryEmit(Unit)
                } else {
                    navController.navigateToTopLevel(destination.route)
                }
            }

            if (useNavigationRail) {
                Row(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showNavigation,
                        enter = slideInHorizontally { -it },
                        exit = slideOutHorizontally { -it }
                    ) {
                        NavigationRail {
                            topLevelDestinations.forEach { destination ->
                                val selected =
                                    currentDestination?.hierarchyHasRoute(destination.routeClass) == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { onItemClick(destination, selected) },
                                    icon = {
                                        NavigationIcon(destination, selected, avatarUrl)
                                    },
                                    label = { Text(stringResource(destination.label)) }
                                )
                            }
                        }
                    }
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showNavigation,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            NavigationBar {
                                topLevelDestinations.forEach { destination ->
                                    val selected =
                                        currentDestination?.hierarchyHasRoute(destination.routeClass) == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { onItemClick(destination, selected) },
                                        icon = {
                                            NavigationIcon(destination, selected, avatarUrl)
                                        },
                                        label = { Text(stringResource(destination.label)) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationIcon(
    destination: TopLevelDestination,
    selected: Boolean,
    avatarUrl: String?
) {
    // The profile item shows the signed-in user's avatar, mirroring the old menu item icon.
    if (destination.routeClass == Routes.MyProfile::class && !avatarUrl.isNullOrBlank()) {
        Avatar(
            imageUrl = avatarUrl,
            size = 24.dp,
            contentDescription = null
        )
    } else {
        Icon(
            painter = painterResource(
                if (selected) destination.selectedIcon else destination.unselectedIcon
            ),
            contentDescription = null
        )
    }
}
