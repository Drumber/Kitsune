package io.github.drumber.kitsune.ui.base

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.WindowCompat
import io.github.drumber.kitsune.R

abstract class BaseDialogFragment(
    @LayoutRes layoutRes: Int,
    private val isEdgeToEdge: Boolean = true
) : AppCompatDialogFragment(layoutRes) {

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes?.dimAmount = 0.8f
            setWindowAnimations(R.style.Theme_Kitsune_Slide)

            if (isEdgeToEdge) {
                WindowCompat.setDecorFitsSystemWindows(this, false)
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
            }
        }
    }

    override fun getTheme(): Int {
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.fullScreenDialogTheme, typedValue, true)
        return typedValue.data
    }

}