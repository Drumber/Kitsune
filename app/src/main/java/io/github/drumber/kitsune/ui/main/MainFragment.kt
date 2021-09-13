package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentMainBinding
import io.github.drumber.kitsune.ui.adapter.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.widget.LoadStateSpanSizeLookup
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding()

    private val viewModel: MainFragmentViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)
        val animeAdapter = AnimeAdapter(glide)
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

        animeAdapter.addLoadStateListener { loadState ->
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
    }

}