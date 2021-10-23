package io.github.drumber.kitsune.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.adapter.LibraryEntriesAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.ui.widget.ProfilePictureBehavior
import io.github.drumber.kitsune.util.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment(R.layout.fragment_profile, true),
    LibraryEntriesAdapter.LibraryEntryActionListener {

    private val binding: FragmentProfileBinding by viewBinding()

    private val viewModel: ProfileViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userModel.observe(viewLifecycleOwner) { user ->
            binding.user = user

            val glide = GlideApp.with(this)

            glide.load(user?.avatar)
                .dontAnimate()
                .placeholder(R.drawable.profile_picture_placeholder)
                .into(binding.ivProfileImage)

            glide.load(user?.coverImage)
                .centerCrop()
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)
        }

        binding.apply {
            btnSettings.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
                findNavController().navigate(action)
            }
            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }

            appBarLayout.addOnOffsetChangedListener(
                FadingToolbarOffsetListener(
                    requireActivity(),
                    toolbar
                )
            )

            ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbar) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                collapsingToolbar.expandedTitleMarginStart = insets.left +
                        resources.getDimensionPixelSize(R.dimen.profile_text_offset_expanded)
                windowInsets
            }
            coverSpacer.initMarginWindowInsetsListener(
                left = true,
                top = true,
                right = true,
                consume = false
            )
            toolbar.initWindowInsetsListener(consume = false)

            ViewCompat.setOnApplyWindowInsetsListener(ivProfileImage) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val params = ivProfileImage.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = params.behavior as ProfilePictureBehavior
                behavior.offsetX = insets.left.toFloat()
                behavior.offsetY = insets.top.toFloat()
                windowInsets
            }

            nsvContent.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val glide = GlideApp.with(this)
        val adapter = LibraryEntriesAdapter(glide, this)

        binding.rvLibraryEntries.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.dataSource.collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.episodeWatchProgressResponseListener = { responseData ->
            if (responseData is ResponseData.Success) {
                // TODO: use RemoteMediator to update item in local db instead of re-fetching all over network
                adapter.refresh()
            } else if (responseData is ResponseData.Error) {
                Toast.makeText(requireContext(), "Error: ${responseData.e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClicked(item: LibraryEntry) {
        val resource = item.anime ?: item.manga
        if (resource != null) {
            val resourceAdapter = ResourceAdapter.fromResource(resource)
            val action =
                ProfileFragmentDirections.actionProfileFragmentToDetailsFragment(resourceAdapter)
            findNavController().navigate(action)
        }
    }

    override fun onEpisodeWatchedClicked(item: LibraryEntry) {
        viewModel.markEpisodeWatched(item)
    }

    override fun onEpisodeUnwatchedClicked(item: LibraryEntry) {
        viewModel.markEpisodeUnwatched(item)
    }

    override fun onResume() {
        super.onResume()
        if (context?.isNightMode() == false) {
            activity?.clearLightStatusBar()
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
            activity?.setLightStatusBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.episodeWatchProgressResponseListener = null
    }

}