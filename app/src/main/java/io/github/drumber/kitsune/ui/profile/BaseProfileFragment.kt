package io.github.drumber.kitsune.ui.profile

import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Shared base for [MyProfileFragment] and [UserProfileFragment].
 *
 * Each subclass overrides [onCreateView] with [composeView][io.github.drumber.kitsune.ui.compose.composeView]
 * and hosts [ProfileScreen] directly. ViewBinding and the ViewPager2/TabLayout wiring that
 * previously lived here have been replaced by the stateless [ProfileScreen] composable.
 */
abstract class BaseProfileFragment : Fragment(),
    FragmentDecorationPreference,
    NavigationBarView.OnItemReselectedListener {

    override val hasTransparentStatusBar: Boolean = true

    protected abstract val viewModel: BaseProfileViewModel

    /**
     * Emitting to this flow signals [ProfileScreen] to expand the collapsed app bar and
     * animate the active tab's content back to the top.
     * Triggered whenever the bottom-nav / nav-rail Profile item is re-tapped.
     */
    protected val scrollToTopEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onNavigationItemReselected(item: MenuItem) {
        scrollToTopEvents.tryEmit(Unit)
    }
}
