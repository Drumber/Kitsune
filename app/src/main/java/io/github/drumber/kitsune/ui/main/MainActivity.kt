package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ActivityMainBinding
import io.github.drumber.kitsune.ui.base.BaseActivity
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
            setupWithNavController(navController)

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
                // since we overwrote the old select listener added with setupWithNavController,
                // we have to manually inform NavigationUI about the event
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}