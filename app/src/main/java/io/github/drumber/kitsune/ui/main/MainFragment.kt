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
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
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
        binding.nsvContent.initMarginWindowInsetsListener(left = true, right = true, consume = false)

    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)

        val trending = ExploreSection(glide, "Trending This Week", null, this) {

        }
        trending.bindView(binding.sectionTrending.root)

        viewModel.trending.observe(viewLifecycleOwner) { data ->
            trending.setData(data.map { ResourceAdapter.AnimeResource(it) })
        }
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