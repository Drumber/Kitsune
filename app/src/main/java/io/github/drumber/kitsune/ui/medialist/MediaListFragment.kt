package io.github.drumber.kitsune.ui.medialist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.FragmentMediaListBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.paging.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.paging.MangaAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.LoadStateSpanSizeLookup
import io.github.drumber.kitsune.ui.component.ResponsiveGridLayoutManager
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MediaListFragment : Fragment(R.layout.fragment_media_list),
    NavigationBarView.OnItemReselectedListener {

    companion object {
        private const val KEY_LAST_NAV_DESTINATION = "last_nav_destination"
    }

    private val args: MediaListFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentMediaListBinding::bind)

    private val viewModel: MediaListViewModel by viewModel()

    private var lastNavDestination: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(KEY_LAST_NAV_DESTINATION) == true) {
            lastNavDestination = savedInstanceState.getInt(KEY_LAST_NAV_DESTINATION)
        }

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.material_motion_duration_short_2).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (lastNavDestination == R.id.details_fragment) {
            postponeEnterTransition(500, TimeUnit.MILLISECONDS)
        }

        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)

        binding.rvMedia.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        viewModel.setMediaSelector(args.mediaSelector)

        binding.appBarLayout.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.apply {
            initPaddingWindowInsetsListener(left = true, right = true, consume = false)
            title = args.title
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        initRecyclerView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lastNavDestination?.let { outState.putInt(KEY_LAST_NAV_DESTINATION, it) }
    }

    private fun initRecyclerView() {
        val adapter = when (args.mediaSelector.mediaType) {
            MediaType.Anime -> AnimeAdapter(this::onMediaClicked)
            MediaType.Manga -> MangaAdapter(this::onMediaClicked)
        } as PagingDataAdapter<Media, *>

        val columnWidth = resources.getDimension(KitsunePref.mediaItemSize.widthRes) +
                2 * resources.getDimension(R.dimen.media_item_margin)
        val gridLayout = ResponsiveGridLayoutManager(requireContext(), columnWidth.toInt(), 2)
        gridLayout.spanSizeLookup = LoadStateSpanSizeLookup(adapter, gridLayout)

        binding.rvMedia.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ResourceLoadStateAdapter(adapter),
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvMedia.layoutManager = gridLayout

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvMedia,
                        adapter.itemCount,
                        loadStates
                    )

                    if (loadStates.refresh is LoadState.NotLoading) {
                        binding.rvMedia.doOnPreDraw { startPostponedEnterTransition() }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataSource.collectLatest { data ->
                    adapter.submitData(data as PagingData<Media>)
                }
            }
        }
    }

    fun onMediaClicked(view: View, model: Media) {
        val action =
            MediaListFragmentDirections.actionMediaListFragmentToDetailsFragment(model.toMediaDto())
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        lastNavDestination = R.id.details_fragment
        findNavController().navigateSafe(R.id.media_list_fragment, action, extras)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.rvMedia.canScrollVertically(-1)) {
            binding.rvMedia.smoothScrollOrJumpToTop()
            binding.appBarLayout.setExpanded(true)
        } else {
            findNavController().navigateUp()
        }
    }
}