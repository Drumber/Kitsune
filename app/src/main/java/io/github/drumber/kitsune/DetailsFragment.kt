package io.github.drumber.kitsune

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.setStatusBarColorRes

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

            // pass windowInsets down to the toolbar
            collapsingToolbar.setOnApplyWindowInsetsListener { view, windowInsets -> windowInsets }
            toolbar.initWindowInsetsListener()

            glide.load(model.coverImage)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)

            glide.load(model.posterImage)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.setStatusBarColorRes(R.color.translucent_status_bar)
    }

}