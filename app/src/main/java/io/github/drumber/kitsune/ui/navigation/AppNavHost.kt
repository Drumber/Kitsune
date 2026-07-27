package io.github.drumber.kitsune.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import io.github.drumber.kitsune.ui.navigation.graph.authGraph
import io.github.drumber.kitsune.ui.navigation.graph.detailsGraph
import io.github.drumber.kitsune.ui.navigation.graph.homeGraph
import io.github.drumber.kitsune.ui.navigation.graph.photoViewGraph
import io.github.drumber.kitsune.ui.navigation.graph.profileGraph
import io.github.drumber.kitsune.ui.navigation.graph.socialGraph

/**
 * Single [NavHost] of the app. The destinations are contributed by one `NavGraphBuilder`
 * extension per feature area so the areas stay independent of each other.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = Routes.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        homeGraph(navController)
        detailsGraph(navController)
        socialGraph(navController)
        profileGraph(navController)
        photoViewGraph(navController)
        authGraph(navController)
    }
}
