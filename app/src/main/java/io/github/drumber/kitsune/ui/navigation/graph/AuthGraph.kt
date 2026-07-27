package io.github.drumber.kitsune.ui.navigation.graph

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.authentication.LoginScreen
import io.github.drumber.kitsune.ui.authentication.LoginViewModel
import io.github.drumber.kitsune.ui.navigation.Routes
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable<Routes.Login> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Login>()
        LoginDestination(
            wasLoggedOut = route.wasLoggedOut,
            onFinished = { navController.navigateUp() }
        )
    }
}

@Composable
fun LoginDestination(
    wasLoggedOut: Boolean,
    onFinished: () -> Unit
) {
    val viewModel: LoginViewModel = koinViewModel()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val successTemplate = stringResource(R.string.logged_in_success, "%s")

    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { result ->
            val user = result.success
            if (user != null) {
                Toast.makeText(
                    context,
                    successTemplate.format(user.displayName),
                    Toast.LENGTH_LONG
                ).show()
                onFinished()
            } else if (result.error != null) {
                Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        wasLoggedOut = wasLoggedOut,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onLogin = viewModel::login,
        onNavigateUp = onFinished
    )
}
