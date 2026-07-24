package io.github.drumber.kitsune.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import io.github.drumber.kitsune.ui.theme.KitsuneMdcTheme

/**
 * Creates a [ComposeView] suitable for returning from [Fragment.onCreateView].
 *
 * The strategy [ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed] ensures
 * the composition is torn down when the Fragment view is destroyed and re-created when
 * the view re-attaches, correctly tracking the Fragment view lifecycle.
 *
 * Content is automatically wrapped in [KitsuneMdcTheme] so that Material 3 tokens and
 * colours are available throughout the tree. The MDC bridge is used deliberately: the
 * hosting [BaseActivity][io.github.drumber.kitsune.ui.base.BaseActivity] already applies the
 * user's selected theme variant, AMOLED override and — when enabled — Material You dynamic
 * color to the Activity, so reading the active XML theme keeps Compose islands in sync with
 * the surrounding XML screens instead of second-guessing those preferences.
 *
 * **Koin ViewModel injection** — [koinViewModel][org.koin.androidx.compose.koinViewModel]
 * works inside any composable hosted by this view without additional setup. Koin 4.x wires
 * the Compose context automatically when [startKoin][org.koin.core.context.startKoin] is
 * called in [KitsuneApplication][io.github.drumber.kitsune.KitsuneApplication]:
 * ```kotlin
 * val viewModel: MyViewModel = koinViewModel()
 * ```
 *
 * ## Fragment adoption
 * ```kotlin
 * class MyFragment : Fragment() {
 *
 *     override fun onCreateView(
 *         inflater: LayoutInflater,
 *         container: ViewGroup?,
 *         savedInstanceState: Bundle?,
 *     ): View = composeView {
 *         MyScreen()
 *     }
 * }
 * ```
 *
 * For state collection use [collectAsStateWithLifecycle][androidx.lifecycle.compose.collectAsStateWithLifecycle]
 * on a `Flow`, or the [collectAsStateWithLifecycle][io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle]
 * extension on a `LiveData` provided by this package.
 */
fun Fragment.composeView(content: @Composable () -> Unit): ComposeView =
    ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            KitsuneMdcTheme {
                content()
            }
        }
    }
