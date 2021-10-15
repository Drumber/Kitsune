package io.github.drumber.kitsune.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.setStatusBarColorRes

abstract class BaseFragment(
    @LayoutRes contentLayoutId: Int,
    private val transparentStatusBar: Boolean = false
): Fragment(contentLayoutId) {

    override fun onResume() {
        super.onResume()
        if(transparentStatusBar) {
            activity?.setStatusBarColorRes(android.R.color.transparent)
        }
    }

    override fun onPause() {
        super.onPause()
        if(transparentStatusBar) {
            activity?.setStatusBarColorRes(R.color.translucent_status_bar)
        }
    }

}