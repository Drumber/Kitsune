package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ActivityMainBinding
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.util.CustomNavigationUI.bindToNavController
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val viewModel: MainActivityViewModel by viewModel()

    private val binding: ActivityMainBinding by viewBinding()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
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
                        (fragments[0] as NavigationBarView.OnItemReselectedListener).onNavigationItemReselected(item)
                    }
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
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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
                        .duration = resources.getInteger(R.integer.bottom_navigation_animation_duration).toLong()
                } else {
                    this.isVisible = false
                }
            } else if (!hideBottomNav && !this.isVisible) {
                if (animate) {
                    animate().translationY(0f)
                        .withStartAction { this.isVisible = true }
                        .duration = resources.getInteger(R.integer.bottom_navigation_animation_duration).toLong()
                } else {
                    this.isVisible = true
                }
            }
        }
    }

}