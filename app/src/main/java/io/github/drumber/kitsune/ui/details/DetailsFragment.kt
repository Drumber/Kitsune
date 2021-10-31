package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsFragment : BaseFragment(R.layout.fragment_details, true),
    NavigationBarView.OnItemReselectedListener {

    private val args: DetailsFragmentArgs by navArgs()

    private val binding: FragmentDetailsBinding by viewBinding()

    private val viewModel: DetailsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(context?.isNightMode() == false) {
            activity?.clearLightStatusBar()
        }

        initAppBar()

        viewModel.initResourceAdapter(args.model)

        viewModel.resourceAdapter.observe(viewLifecycleOwner) { model ->
            binding.data = model

            val glide = GlideApp.with(this)

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


        binding.apply {
            content.initPaddingWindowInsetsListener(left = true, right = true)
            btnManageLibrary.setOnClickListener { showManageLibraryBottomSheet() }
        }
    }

    private fun initAppBar() {
        binding.apply {
            appBarLayout.addOnOffsetChangedListener(FadingToolbarOffsetListener(requireActivity(), toolbar))

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

    private fun showManageLibraryBottomSheet() {
        viewModel.resourceAdapter.value?.let {
            val action = DetailsFragmentDirections.actionDetailsFragmentToManageLibraryBottomSheet(it)
            findNavController().navigate(action)
        }
    }

    private fun goBack() {
        findNavController().navigateUp()
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        goBack()
    }

    override fun onPause() {
        super.onPause()
        if(activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
            activity?.setLightStatusBar()
        }
    }

}