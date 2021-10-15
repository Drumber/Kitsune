package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.util.*
import kotlin.math.abs

class DetailsFragment : BaseFragment(R.layout.fragment_details, true) {

    private val args: DetailsFragmentArgs by navArgs()

    private val binding: FragmentDetailsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(context?.isNightMode() == false) {
            activity?.clearLightStatusBar()
        }

        initAppBar()

        val model = args.model
        val glide = GlideApp.with(this)

        binding.apply {
            data = model

            content.initPaddingWindowInsetsListener(left = true, right = true)

            glide.load(model.coverImage)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)

            glide.load(model.posterImage)
                .transform(CenterCrop(), RoundedCorners(8))
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }
    }

    private fun initAppBar() {
        binding.apply {
            appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                val maxOffset = appBarLayout.totalScrollRange
                val percent = abs(verticalOffset.toFloat() / maxOffset) // between 0.0 and 1.0

                val expandedColor = ContextCompat.getColor(requireContext(), R.color.white)
                val collapsedColor = requireActivity().theme.getColor(R.attr.colorOnSurface)

                // fade back arrow from white to colorOnSurface while collapsing the toolbar
                val iconTint = ColorUtils.blendARGB(expandedColor, collapsedColor, percent)
                toolbar.setNavigationIconTint(iconTint)

                // switch to light status bar in light mode
                if(activity?.isNightMode() == false) {
                    if(percent < 0.5 && activity?.isLightStatusBar() == true) {
                        activity?.clearLightStatusBar()
                    }
                    if(percent >= 0.5 && activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
                        activity?.setLightStatusBar()
                    }
                }
            })

            toolbar.setNavigationOnClickListener { goBack() }

            val defaultTitleMarginStart = collapsingToolbar.expandedTitleMarginStart
            val defaultTitleMarginEnd= collapsingToolbar.expandedTitleMarginStart
            ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbar) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
                collapsingToolbar.expandedTitleMarginStart = defaultTitleMarginStart + if(isRtl) insets.right else insets.left
                collapsingToolbar.expandedTitleMarginEnd = defaultTitleMarginEnd + if(isRtl) insets.left else insets.right
                windowInsets
            }
            toolbar.initWindowInsetsListener(consume = false)
        }
    }

    private fun goBack() {
        findNavController().navigateUp()
    }

    override fun onPause() {
        super.onPause()
        if(activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
            activity?.setLightStatusBar()
        }
    }

}