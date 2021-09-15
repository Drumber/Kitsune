package io.github.drumber.kitsune

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.util.getColor
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.setStatusBarColorRes
import kotlin.math.abs

class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val args: DetailsFragmentArgs by navArgs()

    private val binding: FragmentDetailsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.setStatusBarColorRes(android.R.color.transparent)

        val model = args.model
        val glide = GlideApp.with(this)

        binding.apply {
            data = model

            // fade back arrow from white to colorOnSurface while collapsing the toolbar
            appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                val maxOffset = appBarLayout.totalScrollRange
                val percent = abs(verticalOffset.toFloat() / maxOffset) // between 0.0 and 1.0

                val expandedColor = ContextCompat.getColor(requireContext(), R.color.white)
                val collapsedColor = requireActivity().theme.getColor(R.attr.colorOnSurface)

                val iconTint = ColorUtils.blendARGB(expandedColor, collapsedColor, percent)
                toolbar.setNavigationIconTint(iconTint)
            })

            toolbar.setNavigationOnClickListener { goBack() }

            // pass windowInsets down to the toolbar
            collapsingToolbar.setOnApplyWindowInsetsListener { view, windowInsets -> windowInsets }
            toolbar.initWindowInsetsListener(consume = false)

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

    private fun goBack() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.setStatusBarColorRes(R.color.translucent_status_bar)
    }

}