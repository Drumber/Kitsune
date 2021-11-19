package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.databinding.FragmentMainBinding
import io.github.drumber.kitsune.databinding.SectionMainExploreBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.widget.ExploreSection
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.network.ResponseData
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment(R.layout.fragment_main), OnItemClickListener<ResourceAdapter>,
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentMainBinding by viewBinding()

    private val viewModel: MainFragmentViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExploreSections()

        binding.toolbar.initWindowInsetsListener(false)
        binding.nsvContent.initMarginWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )
    }

    private fun initExploreSections() {
        val trending = createAnimeExploreSection(
            R.string.section_trending,
            Filter().limit(30),
            RequestType.TRENDING,
            binding.sectionTrending,
            viewModel.trending
        )

        val topAiring = createAnimeExploreSection(
            R.string.section_top_airing,
            MainFragmentViewModel.FILTER_TOP_AIRING,
            RequestType.ALL,
            binding.sectionTopAiring,
            viewModel.topAiring
        )

        val topUpcoming = createAnimeExploreSection(
            R.string.section_top_upcoming,
            MainFragmentViewModel.FILTER_TOP_UPCOMING,
            RequestType.ALL,
            binding.sectionTopUpcoming,
            viewModel.topUpcoming
        )

        val highestRated = createAnimeExploreSection(
            R.string.section_highest_rated,
            MainFragmentViewModel.FILTER_HIGHEST_RATED,
            RequestType.ALL,
            binding.sectionHighestRated,
            viewModel.highestRated
        )

        val mostPopular = createAnimeExploreSection(
            R.string.section_most_popular,
            MainFragmentViewModel.FILTER_MOST_POPULAR,
            RequestType.ALL,
            binding.sectionMostPopular,
            viewModel.mostPopular
        )
    }

    private fun createAnimeExploreSection(
        @StringRes titleRes: Int,
        filter: Filter,
        requestType: RequestType,
        sectionBinding: SectionMainExploreBinding,
        liveData: LiveData<ResponseData<List<Anime>>>
    ): ExploreSection {
        sectionBinding.apply {
            rvResource.isVisible = false
            layoutLoading.apply {
                tvError.isVisible = false
                btnRetry.isVisible = false
                root.isVisible = true
            }
        }
        val resourceSelector = ResourceSelector(ResourceType.Anime, filter, requestType)
        val section = createExploreSection(titleRes, resourceSelector, sectionBinding.root)
        liveData.observe(viewLifecycleOwner) { response ->
            if(response is ResponseData.Success) {
                section.setData(response.data.map { ResourceAdapter.AnimeResource(it) })
                sectionBinding.apply {
                    layoutLoading.root.isVisible = false
                    rvResource.isVisible = true
                }
            } else {
                sectionBinding.apply {
                    rvResource.isVisible = false
                    layoutLoading.apply {
                        root.isVisible = true
                        tvError.isVisible = true
                        progressBar.isVisible = false
                    }
                }
            }

        }
        return section
    }

    private fun createExploreSection(
        @StringRes titleRes: Int,
        resourceSelector: ResourceSelector,
        view: View
    ): ExploreSection {
        val title = getString(titleRes)
        val glide = GlideApp.with(this)
        val section = ExploreSection(glide, title, null, this) {
            val action = MainFragmentDirections.actionMainFragmentToResourceListFragment(resourceSelector, title)
            findNavController().navigateSafe(R.id.main_fragment, action)
        }
        section.bindView(view)
        return section
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.nsvContent.smoothScrollTo(0, 0)
        binding.appBarLayout.setExpanded(true)
    }

    override fun onItemClick(model: ResourceAdapter) {
        val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(model)
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
        findNavController().navigateSafe(R.id.main_fragment, action, options)
    }

}