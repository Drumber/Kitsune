package io.github.drumber.kitsune.ui.main

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import io.github.drumber.kitsune.R

class MainActivityViewModel: ViewModel() {

    /** Destination ID of the current selected bottom navigation item. */
    @IdRes
    var currentNavRootDestId: Int = R.id.main_fragment

}