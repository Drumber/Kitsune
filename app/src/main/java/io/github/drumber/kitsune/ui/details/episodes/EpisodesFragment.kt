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
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.model.unit.MediaUnit
import io.github.drumber.kitsune.data.model.unit.MediaUnitAdapter
import io.github.drumber.kitsune.databinding.FragmentResourceListBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.adapter.paging.MediaUnitPagingAdapter
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.util.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class EpisodesFragment : BaseCollectionFragment(R.layout.fragment_resource_list) {

    private val args: EpisodesFragmentArgs by navArgs()

    private val binding: FragmentResourceListBinding by viewBinding()

    private val viewModel: EpisodesViewModel by viewModel()

    override val recyclerView: RecyclerView
        get() = binding.rvResource

    override val resourceLoadingBinding: LayoutResourceLoadingBinding
        get() = binding.layoutLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setResource(args.resource)

        binding.toolbar.apply {
            initWindowInsetsListener(false)
            title = getString(
                when (args.resource) {
                    is Anime -> R.string.title_episodes
                    is Manga -> R.string.title_chapters
                }
            )
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        val resourceAdapter = ResourceAdapter.fromMedia(args.resource)
        val adapter = MediaUnitPagingAdapter(GlideApp.with(this), resourceAdapter.posterImage) {
            showDetailsBottomSheet(it)
        }
        setRecyclerViewAdapter(adapter)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
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
            MediaUnitDetailsBottomSheet.BUNDLE_MEDIA_UNIT_ADAPTER to MediaUnitAdapter.fromMediaUnit(mediaUnit),
            MediaUnitDetailsBottomSheet.BUNDLE_THUMBNAIL to ResourceAdapter.fromMedia(args.resource).posterImage
        )
        sheetMediaUnit.show(parentFragmentManager, MediaUnitDetailsBottomSheet.TAG)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        super.onNavigationItemReselected(item)
        binding.appBarLayout.setExpanded(true)
    }

}