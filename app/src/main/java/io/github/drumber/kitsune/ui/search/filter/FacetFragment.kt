package io.github.drumber.kitsune.ui.search.filter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.filter.facet.FacetListAdapter
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.filter.facet.connectView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentFilterFacetBinding
import io.github.drumber.kitsune.ui.search.SearchViewModel
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class FacetFragment : Fragment(R.layout.fragment_filter_facet) {

    private val binding: FragmentFilterFacetBinding by viewBinding()

    private val connection = ConnectionHandler()

    private val viewModel: SearchViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            initWindowInsetsListener()
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        viewModel.filterFacets.observe(viewLifecycleOwner) { filterFacets ->
            createFilterViews(filterFacets)
        }
    }

    private fun createFilterViews(filterFacets: SearchViewModel.FilterFacets) {
        val adapterKind = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvKind.initAdapter(adapterKind)
        connection += filterFacets.kindConnector.connectView(adapterKind, filterFacets.kindPresenter)

        val adapterSubtype = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvSubtype.initAdapter(adapterSubtype)
        connection += filterFacets.subtypeConnector.connectView(adapterSubtype, filterFacets.subtypePresenter)
    }

    private fun RecyclerView.initAdapter(adapter: RecyclerView.Adapter<*>) {
        this.adapter = adapter
        layoutManager = LinearLayoutManager(requireContext())
        autoScrollToStart(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connection.clear()
    }

}