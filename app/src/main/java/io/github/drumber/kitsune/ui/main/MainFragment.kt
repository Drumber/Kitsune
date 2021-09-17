package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.model.resource.anime.Anime
import io.github.drumber.kitsune.databinding.FragmentMainBinding
import io.github.drumber.kitsune.ui.adapter.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.widget.LoadStateSpanSizeLookup
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment(R.layout.fragment_main), AnimeAdapter.OnItemClickListener {

    private val binding: FragmentMainBinding by viewBinding()

    private val viewModel: MainFragmentViewModel by viewModel()

    private lateinit var animeAdapter: AnimeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()

        binding.toolbar.initWindowInsetsListener(false)
        binding.rvAnime.initMarginWindowInsetsListener(left = true, right = true, consume = false)
    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)
        animeAdapter = AnimeAdapter(glide, this)
        val gridLayout = GridLayoutManager(requireContext(), 2)

        binding.rvAnime.apply {
            layoutManager = gridLayout
            adapter = animeAdapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(animeAdapter),
                footer = ResourceLoadStateAdapter(animeAdapter)
            )
        }
        // this will make sure to display header and footer with full width
        gridLayout.spanSizeLookup = LoadStateSpanSizeLookup(animeAdapter, gridLayout.spanCount)
        
        lifecycleScope.launchWhenCreated {
            viewModel.anime.collectLatest {
                animeAdapter.submitData(it)
            }
        }

        binding.layoutLoading.btnRetry.setOnClickListener { animeAdapter.retry() }

        animeAdapter.addLoadStateListener(loadStateListener)
    }

    private val loadStateListener: (CombinedLoadStates) -> Unit = { loadState ->
        binding.apply {
            rvAnime.isVisible = loadState.source.refresh is LoadState.NotLoading
            layoutLoading.apply {
                root.isVisible = loadState.source.refresh !is LoadState.NotLoading
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
                tvError.isVisible = loadState.source.refresh is LoadState.Error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animeAdapter.removeLoadStateListener(loadStateListener)
    }

    override fun onItemClick(anime: Anime) {
        val model = ResourceAdapter.AnimeResource(anime)
        val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(model)
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(findNavController().graph.findStartDestination().id, inclusive = false, saveState = true)
            .setRestoreState(false)
            .build()
        findNavController().navigate(action, options)
    }
}