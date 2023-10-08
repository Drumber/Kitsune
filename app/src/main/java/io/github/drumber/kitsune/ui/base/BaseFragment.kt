package io.github.drumber.kitsune.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference

abstract class BaseFragment(
    @LayoutRes contentLayoutId: Int,
    override val hasTransparentStatusBar: Boolean = true
) : Fragment(contentLayoutId), FragmentDecorationPreference
