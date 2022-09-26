package io.github.drumber.kitsune.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.StartPagePref
import io.github.drumber.kitsune.data.model.getDestinationId
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.databinding.ActivityMainBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.util.CustomNavigationUI.bindToNavController
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val viewModel: MainActivityViewModel by viewModel()

    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
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
            bindToNavController(navController)

            // handle reselect of navigation item and pass event to current fragment
            // we use setOnItemSelectedListener instead of setOnItemReselectedListener because we still
            // want to be able to navigate back when clicking on the current menu item
            setOnItemSelectedListener { item ->
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
                    // no need to navigate if the we are already at the selected destination
                    return@setOnItemSelectedListener true
                }

                // navigate to the target destination
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        // hide bottom navigation if settings fragment or one of its subordinate fragments is displayed
        toggleBottomNavigation(isSettingsFragmentInBackStack(), false)
        navController.addOnDestinationChangedListener { _, _, _ ->
            toggleBottomNavigation(isSettingsFragmentInBackStack())
        }

        if (savedInstanceState == null) {
            if (!handleShortcutAction() && KitsunePref.startFragment != StartPagePref.Home) {
                setStartFragment(KitsunePref.startFragment.getDestinationId())
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

    override fun onBackPressed() {
        if (!handleBackPressed()) {
            super.onBackPressed()
        }
    }

    /**
     * Make sure to enter the start destination before exiting the app through back-button press.
     */
    private fun handleBackPressed(): Boolean {
        val backStackDestinations = navController.backQueue.filter { entry ->
            entry.destination !is NavGraph
        }
        // do nothing if there are more than 1 back stack items left
        if (backStackDestinations.size > 1) {
            return false
        }

        val startDestinationId = navController.graph.startDestinationId
        val hasStartDestination = navController.backQueue
            .any { it.destination.id == startDestinationId }

        // if the start destination is not in the back stack, navigate to it (launches a new instance of it)
        if (!hasStartDestination) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(backStackDestinations.first().destination.id, true)
                .build()
            navController.navigate(startDestinationId, null, navOptions)
            return true
        }
        return false
    }

    private fun handleShortcutAction(): Boolean {
        val navigationId = when (intent.action) {
            SHORTCUT_LIBRARY -> R.id.library_fragment
            SHORTCUT_SEARCH -> R.id.search_fragment
            SHORTCUT_SETTINGS -> R.id.settings_fragment
            else -> null
        }

        if (navigationId != null) {
            setStartFragment(navigationId)
            return true
        }
        return false
    }

    private fun setStartFragment(navigationId: Int) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.main_fragment, true)
            .build()
        navController.navigate(navigationId, null, navOptions)
    }

    private fun isSettingsFragmentInBackStack(): Boolean {
        return navController.backQueue.lastOrNull { entry ->
            entry.destination.id == R.id.settings_fragment
        } != null
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