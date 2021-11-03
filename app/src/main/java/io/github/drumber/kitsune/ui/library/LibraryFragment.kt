package io.github.drumber.kitsune.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.FragmentLibraryBinding
import io.github.drumber.kitsune.ui.adapter.LibraryEntriesAdapter
import io.github.drumber.kitsune.ui.adapter.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.navigateSafe
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : BaseFragment(R.layout.fragment_library, false),
    LibraryEntriesAdapter.LibraryEntryActionListener,
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentLibraryBinding by viewBinding()

    private val viewModel: LibraryViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.initWindowInsetsListener(consume = false)
            rvLibraryEntries.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
            layoutNotLoggedIn.initPaddingWindowInsetsListener(left = true, top = true, right = true, consume = false)

            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }

        viewModel.userRepository.userLiveData.observe(viewLifecycleOwner) { user ->
            val isLoggedIn = user != null
            binding.apply {
                rvLibraryEntries.isVisible = isLoggedIn
                nsvNotLoggedIn.isVisible = !isLoggedIn
                // disable toolbar scrolling if library is not shown (not logged in)
                (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags = if (isLoggedIn) {
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                } else {
                    AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
                }
            }
        }

        viewModel.responseErrorListener = { error ->
            val snackbar = Snackbar.make(binding.rvLibraryEntries, "Error: ${error.message}", Snackbar.LENGTH_LONG)
            // solve snackbar misplacement (remove bottom margin)
            snackbar.view.initMarginWindowInsetsListener(left = true, right = true)
            snackbar.show()
        }

        setFragmentResultListener(RatingBottomSheet.RATING_REQUEST_KEY) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateRating(rating)
            }
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val glide = GlideApp.with(this)
        val adapter = LibraryEntriesAdapter(glide, this)

        adapter.addLoadStateListener { state ->
            if(view?.parent != null) {
                val isNotLoading = state.mediator?.refresh is LoadState.NotLoading || state.source.refresh is LoadState.NotLoading
                binding.apply {
                    rvLibraryEntries.isVisible = isNotLoading
                    layoutLoading.apply {
                        root.isVisible = !isNotLoading
                        progressBar.isVisible = state.refresh is LoadState.Loading
                        btnRetry.isVisible = state.refresh is LoadState.Error
                        tvError.isVisible = state.refresh is LoadState.Error
                    }
                }
            }
        }

        binding.rvLibraryEntries.apply {
            this.adapter = adapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(adapter),
                footer = ResourceLoadStateAdapter(adapter)
            )
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.dataSource.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onItemClicked(item: LibraryEntry) {
        val resource = item.anime ?: item.manga
        if (resource != null) {
            val resourceAdapter = ResourceAdapter.fromResource(resource)
            val action = LibraryFragmentDirections.actionLibraryFragmentToDetailsFragment(resourceAdapter)
            findNavController().navigate(action)
        }
    }

    override fun onEpisodeWatchedClicked(item: LibraryEntry) {
        viewModel.markEpisodeWatched(item)
    }

    override fun onEpisodeUnwatchedClicked(item: LibraryEntry) {
        viewModel.markEpisodeUnwatched(item)
    }

    override fun onRatingClicked(item: LibraryEntry) {
        viewModel.lastRatedLibraryEntry = item
        val resourceAdapter = (item.anime ?: item.manga)?.let { ResourceAdapter.fromResource(it) }
        val sheetLibraryRating = RatingBottomSheet()
        val bundle = bundleOf(
            RatingBottomSheet.BUNDLE_TITLE to resourceAdapter?.title,
            RatingBottomSheet.BUNDLE_RATING to item.ratingTwenty
        )
        sheetLibraryRating.arguments = bundle
        sheetLibraryRating.show(parentFragmentManager, RatingBottomSheet.TAG)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.rvLibraryEntries.smoothScrollToPosition(0)
        binding.appBarLayout.setExpanded(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.responseErrorListener = null
    }

}