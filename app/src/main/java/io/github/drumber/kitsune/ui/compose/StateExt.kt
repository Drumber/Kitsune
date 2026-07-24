package io.github.drumber.kitsune.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Collects this [LiveData] as Compose [State], respecting the composition's current lifecycle.
 *
 * This bridges existing ViewModels that expose [LiveData] — such as
 * [AppLogsViewModel.logMessages][io.github.drumber.kitsune.ui.settings.AppLogsViewModel.logMessages] —
 * to Compose without requiring callers to manually convert to a [kotlinx.coroutines.flow.Flow] first.
 *
 * The returned state is nullable (`T?`) because [LiveData] may not have emitted a value yet;
 * use [collectAsStateWithLifecycle] with an explicit [initialValue] when a non-null initial is known.
 *
 * ## Usage
 * ```kotlin
 * @Composable
 * fun AppLogsScreen(viewModel: AppLogsViewModel) {
 *     val logs by viewModel.logMessages.collectAsStateWithLifecycle()
 *     Text(text = logs ?: "Loading…")
 * }
 * ```
 *
 * ## Flow state collection
 * For [kotlinx.coroutines.flow.Flow]-based state (e.g. [kotlinx.coroutines.flow.StateFlow]),
 * use [androidx.lifecycle.compose.collectAsStateWithLifecycle] directly — it is already provided
 * by the `lifecycle-runtime-compose` artifact and requires no wrapper:
 * ```kotlin
 * val followStates by viewModel.followStates.collectAsStateWithLifecycle()
 * ```
 *
 * ## ViewModel injection
 * Inside any composable hosted by [composeView] or [ComposeFragment], obtain a ViewModel with:
 * ```kotlin
 * val viewModel: MyViewModel = koinViewModel()  // org.koin.androidx.compose.koinViewModel
 * ```
 * No additional Koin setup is required — the Koin context is already provided by the host.
 */
@Composable
fun <T> LiveData<T>.collectAsStateWithLifecycle(): State<T?> =
    asFlow().collectAsStateWithLifecycle(initialValue = value)

/**
 * Collects this [LiveData] as Compose [State] with an explicit [initialValue],
 * respecting the composition's current lifecycle.
 *
 * Prefer this overload when a non-null sentinel (e.g. an empty string or an empty list)
 * is more useful than `null` before the first emission.
 *
 * ## Usage
 * ```kotlin
 * val logs by viewModel.logMessages.collectAsStateWithLifecycle(initialValue = "")
 * ```
 */
@Composable
fun <T : R, R> LiveData<T>.collectAsStateWithLifecycle(initialValue: R): State<R> =
    asFlow().collectAsStateWithLifecycle(initialValue = initialValue)
