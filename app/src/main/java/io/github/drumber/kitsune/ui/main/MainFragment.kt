package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.databinding.FragmentMainBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.widget.ExploreSection
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment(R.layout.fragment_main), OnItemClickListener<ResourceAdapter>,
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentMainBinding by viewBinding()

    private val viewModel: MainFragmentViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()

        binding.toolbar.initWindowInsetsListener(false)
        binding.nsvContent.initMarginWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

    }

    private fun initAdapter() {
        val trending = createExploreSection(
            getString(R.string.section_trending),
            ResourceSelector(ResourceType.Anime, Filter().limit(30), RequestType.TRENDING),
            binding.sectionTrending.root
        )
        viewModel.trending.observe(viewLifecycleOwner) { data ->
            trending.setData(data.map { ResourceAdapter.AnimeResource(it) })
        }

        val topAiring = createExploreSection(
            getString(R.string.section_top_airing),
            ResourceSelector(ResourceType.Anime, MainFragmentViewModel.FILTER_TOP_AIRING, RequestType.ALL),
            binding.sectionTopAiring.root
        )
        viewModel.topAiring.observe(viewLifecycleOwner) { data ->
            topAiring.setData(data.map { ResourceAdapter.AnimeResource(it) })
        }

        val topUpcoming = createExploreSection(
            getString(R.string.section_top_upcoming),
            ResourceSelector(ResourceType.Anime, MainFragmentViewModel.FILTER_TOP_UPCOMING, RequestType.ALL),
            binding.sectionTopUpcoming.root
        )
        viewModel.topUpcoming.observe(viewLifecycleOwner) { data ->
            topUpcoming.setData(data.map { ResourceAdapter.AnimeResource(it) })
        }

        val highestRated = createExploreSection(
            getString(R.string.section_highest_rated),
            ResourceSelector(ResourceType.Anime, MainFragmentViewModel.FILTER_HIGHEST_RATED, RequestType.ALL),
            binding.sectionHighestRated.root
        )
        viewModel.highestRated.observe(viewLifecycleOwner) { data ->
            highestRated.setData(data.map { ResourceAdapter.AnimeResource(it) })
        }

        val mostPopular = createExploreSection(
            getString(R.string.section_most_popular),
            ResourceSelector(ResourceType.Anime, MainFragmentViewModel.FILTER_MOST_POPULAR, RequestType.ALL),
            binding.sectionMostPopular.root
        )
        viewModel.mostPopular.observe(viewLifecycleOwner) { data ->
            mostPopular.setData(data.map { ResourceAdapter.AnimeResource(it) })
        }
    }

    private fun createExploreSection(
        title: String,
        resourceSelector: ResourceSelector,
        view: View
    ): ExploreSection {
        val glide = GlideApp.with(this)
        val section = ExploreSection(glide, title, null, this) {
            val action = MainFragmentDirections.actionMainFragmentToExploreFragment2(resourceSelector, title)
            findNavController().navigate(action)
        }
        section.bindView(view)
        return section
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.nsvContent.smoothScrollTo(0, 0)
    }

    override fun onItemClick(model: ResourceAdapter) {
        val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(model)
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(
                findNavController().graph.findStartDestination().id,
                inclusive = false,
                saveState = true
            )
            .setRestoreState(false)
            .build()
        findNavController().navigate(action, options)
    }

}