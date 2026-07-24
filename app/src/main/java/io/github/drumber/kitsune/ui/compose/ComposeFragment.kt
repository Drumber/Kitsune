package io.github.drumber.kitsune.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference

/**
 * Base class for Fragments whose entire view is implemented in Compose.
 *
 * Subclasses implement [ComposeContent] and receive:
 * - Proper [ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed][androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed]
 *   wired via [composeView]
 * - [KitsuneMdcTheme][io.github.drumber.kitsune.ui.theme.KitsuneMdcTheme] wrapping, which
 *   inherits the theme variant / AMOLED / dynamic-color settings already applied to the Activity
 * - Koin context so that [koinViewModel][org.koin.androidx.compose.koinViewModel] works out of the box
 * - [FragmentDecorationPreference] with the default transparent status bar (override to change)
 *
 * ## Example
 * ```kotlin
 * class AppLogsFragment : ComposeFragment() {
 *
 *     @Composable
 *     override fun ComposeContent() {
 *         val viewModel: AppLogsViewModel = koinViewModel()
 *         AppLogsScreen(viewModel = viewModel)
 *     }
 * }
 * ```
 */
abstract class ComposeFragment : Fragment(), FragmentDecorationPreference {

    @Composable
    abstract fun ComposeContent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = composeView {
        ComposeContent()
    }
}
