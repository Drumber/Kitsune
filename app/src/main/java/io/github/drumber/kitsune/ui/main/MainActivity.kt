package io.github.drumber.kitsune.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ActivityMainBinding
import io.github.drumber.kitsune.domain.model.preference.StartPagePref
import io.github.drumber.kitsune.domain.model.preference.getDestinationId
import io.github.drumber.kitsune.domain.model.ui.media.originalOrDown
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.ui.permissions.requestNotificationPermission
import io.github.drumber.kitsune.ui.permissions.showNotificationPermissionRejectedDialog
import io.github.drumber.kitsune.util.ui.RoundBitmapDrawable
import io.github.drumber.kitsune.util.extensions.setStatusBarColorRes
import io.github.drumber.kitsune.util.ui.getSystemBarsAndCutoutInsets
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val viewModel: MainActivityViewModel by viewModel()

    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var navController: NavController

    private var overrideStartDestination: Int? = null

    private val navigationBarView: NavigationBarView
        get() = binding.bottomNavigation
            ?: binding.navigationRail
            ?: error("There must exist a navigation bar view.")

    override fun onCreate(savedInstanceState: Bundle?) {
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)

        val userRepository = get<UserRepository>()
        userRepository.userReLoginPrompt.observe(this) {
            if (it) {
                promptUserReLogin()
                userRepository.userReLoginPrompt.postValue(false)
            }
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.childFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentLifecycleCallbacks() {
            private fun updateDecorationForFragment(fragment: Fragment) {
                var statusBarColorRes = android.R.color.transparent
                if (fragment is FragmentDecorationPreference && !fragment.hasTransparentStatusBar) {
                    statusBarColorRes = R.color.translucent_status_bar
                }
                setStatusBarColorRes(statusBarColorRes)
            }

            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                updateDecorationForFragment(f)
            }

            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                fm.fragments.lastOrNull()?.let { updateDecorationForFragment(it) }
            }
        }, true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { _, windowInsets ->
            if (!isNavigationBarViewVisible())
                return@setOnApplyWindowInsetsListener windowInsets

            val insets = windowInsets.getSystemBarsAndCutoutInsets()
            val consumedInsets = binding.bottomNavigation?.applyWindowInsets(insets)
                ?: binding.navigationRail?.applyWindowInsets(insets)
                ?: Insets.of(0, 0, 0, 0)
            // consume insets used by the navigation bar view
            // and propagate the remaining inset space to child fragments
            windowInsets.inset(consumedInsets)
        }

        navController = navHostFragment.navController
        navigationBarView.apply {
            setOnItemSelectedListener { item ->
                viewModel.currentNavRootDestId = item.itemId

                // handle reselect of navigation item and pass event to current fragment
                navHostFragment.childFragmentManager.fragments.let { fragments ->
                    if (item.itemId == selectedItemId
                        && fragments.size > 0
                        && fragments[0] is NavigationBarView.OnItemReselectedListener
                    ) {
                        (fragments[0] as NavigationBarView.OnItemReselectedListener).onNavigationItemReselected(
                            item
                        )
                    }
                }

                if (item.itemId == selectedItemId) {
                    // no need to navigate if we are already at the selected destination
                    return@setOnItemSelectedListener true
                }

                // navigate to the target destination
                NavigationUI.onNavDestinationSelected(item, navController)
            }


            userRepository.userLiveData
                .map { it?.avatar?.originalOrDown() }
                .distinctUntilChanged()
                .observe(this@MainActivity) { avatarUrl ->
                    if (avatarUrl.isNullOrBlank()) {
                        menu.findItem(R.id.profile_fragment).setIcon(R.drawable.selector_profile)
                        return@observe
                    }
                    Glide.with(this)
                        .asBitmap()
                        .load(avatarUrl)
                        .dontAnimate()
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                menu.findItem(R.id.profile_fragment).icon =
                                    RoundBitmapDrawable(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // set the selected bottom navigation item
            for (menuItem in navigationBarView.menu) {
                if (menuItem.itemId == destination.id) {
                    viewModel.currentNavRootDestId = menuItem.itemId
                }
                if (menuItem.itemId == viewModel.currentNavRootDestId) {
                    menuItem.isChecked = true
                }
            }

            // hide bottom navigation if the destination is not a main one
            toggleNavigationBarView(
                !isDestinationOnMainNavGraph(destination),
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            )
        }

        // override start fragment, but only on clean launch and when not launched by a deep link
        if (savedInstanceState == null && !isLaunchedByDeepLink()) {
            overrideStartDestination = getShortcutStartDestinationId()
            // if the app wasn't launched from an app shortcut
            // and the user has specified a custom start page
            // then set the start fragment to the custom one
            if (overrideStartDestination == null && KitsunePref.startFragment != StartPagePref.Home) {
                overrideStartDestination = KitsunePref.startFragment.getDestinationId()
            }
        }

        requestRequiredPermissions()
    }

    override fun onStart() {
        super.onStart()
        overrideStartDestination?.let {
            navigateToStartFragment(it)
            overrideStartDestination = null
        }
    }

    private fun requestRequiredPermissions() {
        if (!KitsunePref.flagUserDeniedNotificationPermission) {
            val requestNotificationPermissionLauncher =
                registerForActivityResult(RequestPermission()) { isGranted ->
                    if (isGranted) {
                        KitsunePref.checkForUpdatesOnStart = true
                        KitsunePref.flagUserDeniedNotificationPermission = false
                    } else {
                        KitsunePref.checkForUpdatesOnStart = false
                        KitsunePref.flagUserDeniedNotificationPermission = true
                        showNotificationPermissionRejectedDialog()
                    }
                }
            requestNotificationPermission(requestNotificationPermissionLauncher) {
                KitsunePref.flagUserDeniedNotificationPermission = true
            }
        }
    }

    private fun promptUserReLogin() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.putExtra(AuthenticationActivity.EXTRA_LOGGED_OUT, true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /** Checks if the activity was launched using an app link, */
    private fun isLaunchedByDeepLink(): Boolean {
        return intent.action == Intent.ACTION_VIEW && intent.data != null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    private fun getShortcutStartDestinationId(): Int? {
        return when (intent.action) {
            SHORTCUT_LIBRARY -> R.id.library_fragment
            SHORTCUT_SEARCH -> R.id.search_fragment
            SHORTCUT_SETTINGS -> R.id.settings_nav_graph
            else -> null
        }
    }

    private fun navigateToStartFragment(navigationId: Int) {
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(R.id.main_fragment, inclusive = false, saveState = true)
            .build()
        navController.navigate(navigationId, null, navOptions)
    }

    private fun isDestinationOnMainNavGraph(destination: NavDestination): Boolean {
        return destination.parent?.id == R.id.main_nav_graph
    }

    private fun isNavigationBarViewVisible(): Boolean {
        return navController.currentDestination
            ?.let { isDestinationOnMainNavGraph(it) } ?: true
    }

    private fun toggleNavigationBarView(hideNavigationBar: Boolean, animate: Boolean = true) {
        if (!animate) {
            navigationBarView.isVisible = !hideNavigationBar
        } else {
            when {
                binding.bottomNavigation != null -> animateBottomNavigation(hideNavigationBar)
                binding.navigationRail != null -> animateNavigationRail(hideNavigationBar)
            }
        }
    }

    private fun animateBottomNavigation(slideDown: Boolean) {
        binding.bottomNavigation?.apply {
            if (slideDown) {
                animate().translationY(this.height.toFloat())
                    .withEndAction { this.isVisible = false }
                    .duration =
                    resources.getInteger(R.integer.bottom_navigation_animation_duration)
                        .toLong()
            } else {
                animate().translationY(0f)
                    .withStartAction { this.isVisible = true }
                    .duration =
                    resources.getInteger(R.integer.bottom_navigation_animation_duration)
                        .toLong()
            }
        }
    }

    private fun animateNavigationRail(slideOut: Boolean) {
        binding.navigationRail?.apply {
            if (slideOut) {
                // different direction depending on if rail is left or right aligned
                val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                val translationFactor = if (isRtl) 1 else -1
                animate().translationX(this.width.toFloat() * translationFactor)
                    .withEndAction { this.isVisible = false }
                    .duration =
                    resources.getInteger(R.integer.navigation_rail_animation_duration)
                        .toLong()
            } else {
                animate().translationX(0f)
                    .withStartAction { this.isVisible = true }
                    .duration =
                    resources.getInteger(R.integer.navigation_rail_animation_duration)
                        .toLong()
            }
        }
    }

    private fun BottomNavigationView.applyWindowInsets(insets: Insets): Insets {
        updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
        return Insets.of(0, 0, 0, insets.bottom)
    }

    private fun NavigationRailView.applyWindowInsets(insets: Insets): Insets {
        val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
        val left = if (!isRtl) insets.left else 0
        val right = if (isRtl) insets.right else 0
        updatePadding(left = left, top = insets.top, right = right, bottom = insets.bottom)
        return Insets.of(left, 0, right, 0)
    }

    companion object {
        const val SHORTCUT_LIBRARY = "io.github.drumber.kitsune.LIBRARY"
        const val SHORTCUT_SEARCH = "io.github.drumber.kitsune.SEARCH"
        const val SHORTCUT_SETTINGS = "io.github.drumber.kitsune.SETTINGS"
    }

}

interface FragmentDecorationPreference {
    val hasTransparentStatusBar: Boolean
        get() = true
}
