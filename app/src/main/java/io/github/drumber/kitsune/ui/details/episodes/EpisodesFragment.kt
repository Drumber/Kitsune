package io.github.drumber.kitsune.ui.details.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMedia
import io.github.drumber.kitsune.data.presentation.dto.toMediaUnitDto
import io.github.drumber.kitsune.data.model.media.unit.MediaUnit
import io.github.drumber.kitsune.databinding.FragmentMediaListBinding
import io.github.drumber.kitsune.ui.adapter.paging.MediaUnitPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbarOnFailure
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EpisodesFragment : Fragment(R.layout.fragment_media_list),
    MediaUnitPagingAdapter.MediaUnitActionListener,
    NavigationBarView.OnItemReselectedListener {

    private val args: EpisodesFragmentArgs by navArgs()

    private var _binding: FragmentMediaListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EpisodesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMedia(args.media.toMedia())

        binding.collapsingToolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.apply {
            initWindowInsetsListener(consume = false)
            title = getString(
                when (args.media.type) {
                    MediaType.Anime -> R.string.title_episodes
                    MediaType.Manga -> R.string.title_chapters
                }
            )
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        binding.rvMedia.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.libraryUpdateResultFlow.collectLatest {
                    it.showSnackbarOnFailure(binding.rvMedia)
                }
            }
        }

        val adapter = MediaUnitPagingAdapter(
            Glide.with(this),
            args.media.toMedia().posterImageUrl,
            viewModel.libraryEntryWrapper.value != null,
            this
        )
        binding.rvMedia.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ResourceLoadStateAdapter(adapter),
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvMedia.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvMedia,
                        adapter.itemCount,
                        loadState
                    )
                }
            }
        }

        viewModel.libraryEntryWrapper.observe(viewLifecycleOwner) {
            adapter.setIsWatchedCheckboxEnabled(it != null)
            it?.progress?.let { progress ->
                adapter.updateLibraryWatchCount(progress)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataSource.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }
    }

    private fun showDetailsBottomSheet(mediaUnit: MediaUnit) {
        val sheetMediaUnit = MediaUnitDetailsBottomSheet()
        sheetMediaUnit.arguments = bundleOf(
            MediaUnitDetailsBottomSheet.BUNDLE_MEDIA_UNIT_ADAPTER to mediaUnit.toMediaUnitDto(),
            MediaUnitDetailsBottomSheet.BUNDLE_THUMBNAIL to args.media.toMedia().posterImageUrl
        )
        sheetMediaUnit.show(parentFragmentManager, MediaUnitDetailsBottomSheet.TAG)
    }

    override fun onMediaUnitClicked(mediaUnit: MediaUnit) {
        showDetailsBottomSheet(mediaUnit)
    }

    override fun onWatchStateChanged(mediaUnit: MediaUnit, isWatched: Boolean) {
        viewModel.setMediaUnitWatched(mediaUnit, isWatched)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.rvMedia.canScrollVertically(-1)) {
            binding.rvMedia.smoothScrollToPosition(0)
            binding.appBarLayout.setExpanded(true)
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}