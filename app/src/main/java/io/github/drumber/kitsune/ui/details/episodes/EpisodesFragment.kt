package io.github.drumber.kitsune.ui.details.episodes

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMedia
import io.github.drumber.kitsune.data.presentation.dto.toMediaUnitDto
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.databinding.FragmentMediaListBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.adapter.paging.MediaUnitPagingAdapter
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbarOnFailure
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EpisodesFragment : BaseCollectionFragment(R.layout.fragment_media_list),
    MediaUnitPagingAdapter.MediaUnitActionListener {

    private val args: EpisodesFragmentArgs by navArgs()

    private val binding: FragmentMediaListBinding by viewBinding()

    private val viewModel: EpisodesViewModel by viewModel()

    override val recyclerView: RecyclerView
        get() = binding.rvMedia

    override val resourceLoadingBinding: LayoutResourceLoadingBinding
        get() = binding.layoutLoading

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

        binding.rvMedia.initMarginWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.libraryUpdateResultFlow.collectLatest {
                it.showSnackbarOnFailure(binding.rvMedia)
            }
        }

        val adapter = MediaUnitPagingAdapter(
            Glide.with(this),
            args.media.toMedia().posterImageUrl,
            viewModel.libraryEntryWrapper.value != null,
            this
        )
        setRecyclerViewAdapter(adapter)

        viewModel.libraryEntryWrapper.observe(viewLifecycleOwner) {
            adapter.setIsWatchedCheckboxEnabled(it != null)
            it?.progress?.let { progress ->
                adapter.updateLibraryWatchCount(progress)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataSource.collectLatest { data ->
                adapter.submitData(data)
            }
        }
    }

    override fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
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
        if (recyclerView.canScrollVertically(-1)) {
            super.onNavigationItemReselected(item)
            binding.appBarLayout.setExpanded(true)
        } else {
            findNavController().navigateUp()
        }
    }

}