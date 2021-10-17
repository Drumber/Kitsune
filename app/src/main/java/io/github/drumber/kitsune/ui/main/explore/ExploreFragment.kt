package io.github.drumber.kitsune.ui.main.explore

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.FragmentExploreBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.ui.base.BaseCollectionViewModel
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExploreFragment : BaseCollectionFragment(R.layout.fragment_explore) {

    private val args: ExploreFragmentArgs by navArgs()

    private val binding: FragmentExploreBinding by viewBinding()

    private val viewModel: ExploreViewModel by viewModel()

    override val collectionViewModel: BaseCollectionViewModel
        get() = viewModel

    override val recyclerView: RecyclerView
        get() = binding.rvResource

    override val resourceLoadingBinding: LayoutResourceLoadingBinding?
        get() = binding.layoutLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setResourceSelector(args.resourceSelector)

        binding.toolbar.apply {
            initWindowInsetsListener(false)
            title = args.title
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    override fun onResourceClicked(model: ResourceAdapter, options: NavOptions) {
        val action = ExploreFragmentDirections.actionExploreFragmentToDetailsFragment2(model)
        findNavController().navigate(action, options)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        super.onNavigationItemReselected(item)
        binding.appBarLayout.setExpanded(true)
    }

}