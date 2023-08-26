package io.github.drumber.kitsune.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.preference.StartPagePref
import io.github.drumber.kitsune.domain.model.preference.getDestinationId
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.databinding.ActivityMainBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseActivity
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val viewModel: MainActivityViewModel by viewModel()

    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var navController: NavController

    private var overrideStartDestination: Int? = null

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
        navController = navHostFragment.navController
        binding.bottomNavigation.apply {
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
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // set the selected bottom navigation item
            for (menuItem in binding.bottomNavigation.menu) {
                if (menuItem.itemId == destination.id) {
                    viewModel.currentNavRootDestId = menuItem.itemId
                }
                if (menuItem.itemId == viewModel.currentNavRootDestId) {
                    menuItem.isChecked = true
                }
            }

            // hide bottom navigation if the destination is not a main one
            toggleBottomNavigation(!isDestinationOnMainNavGraph(destination), lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
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
    }

    override fun onStart() {
        super.onStart()
        overrideStartDestination?.let {
            navigateToStartFragment(it)
            overrideStartDestination = null
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

    private fun toggleBottomNavigation(hideBottomNav: Boolean, animate: Boolean = true) {
        binding.bottomNavigation.apply {
            if (hideBottomNav && this.isVisible) {
                if (animate) {
                    animate().translationY(this.height.toFloat())
                        .withEndAction { this.isVisible = false }
                        .duration =
                        resources.getInteger(R.integer.bottom_navigation_animation_duration)
                            .toLong()
                } else {
                    this.isVisible = false
                }
            } else if (!hideBottomNav && !this.isVisible) {
                if (animate) {
                    animate().translationY(0f)
                        .withStartAction { this.isVisible = true }
                        .duration =
                        resources.getInteger(R.integer.bottom_navigation_animation_duration)
                            .toLong()
                } else {
                    this.isVisible = true
                }
            }
        }
    }

    companion object {
        const val SHORTCUT_LIBRARY = "io.github.drumber.kitsune.LIBRARY"
        const val SHORTCUT_SEARCH = "io.github.drumber.kitsune.SEARCH"
        const val SHORTCUT_SETTINGS = "io.github.drumber.kitsune.SETTINGS"
    }

}