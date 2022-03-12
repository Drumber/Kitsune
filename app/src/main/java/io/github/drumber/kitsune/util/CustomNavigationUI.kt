package io.github.drumber.kitsune.util

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.google.android.material.navigation.NavigationBarView
import java.lang.ref.WeakReference

object CustomNavigationUI {

    /**
     * Listens to destination change events on the given navController
     * and selects the first menu item found in the back stack.
     */
    fun NavigationBarView.bindToNavController(navController: NavController) {
        val weakReference = WeakReference(this)
        navController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?
                ) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }

                    // for each back queue item, check if there is a menu item with the same ID and set it as selected.
                    for (backQueItem in controller.backQueue.reversed()) {
                        val menuItem = view.menu.findItem(backQueItem.destination.id)
                        if (menuItem != null) {
                            menuItem.isChecked = true
                            break
                        }
                    }
                }
            })
    }

}