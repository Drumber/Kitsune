package io.github.drumber.kitsune.ui.search.filter

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.algolia.instantsearch.android.filter.facet.FacetListAdapter
import com.algolia.instantsearch.android.list.autoScrollToStart
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.filter.facet.connectView
import com.algolia.instantsearch.filter.range.connectView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.slider.RangeSlider
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentFilterFacetBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.search.SearchViewModel
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.Error
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.Initialized
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.NotAvailable
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.NotInitialized
import io.github.drumber.kitsune.ui.search.categories.CategoriesDialogFragment
import io.github.drumber.kitsune.ui.widget.ExpandableLayout
import io.github.drumber.kitsune.ui.widget.algolia.IntNumberRangeView
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FacetFragment : BaseFragment(R.layout.fragment_filter_facet, true),
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentFilterFacetBinding by viewBinding()

    private val connection = ConnectionHandler()

    private val viewModel: SearchViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            initWindowInsetsListener(false)
            setNavigationOnClickListener { findNavController().navigateUp() }
            setOnMenuItemClickListener { onMenuItemClicked(it) }
        }

        binding.nsvContent.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        viewModel.filterFacets.observe(viewLifecycleOwner) { filterFacets ->
            createFilterViews(filterFacets)
        }

        binding.layoutSearchProviderStatus.btnRetry.setOnClickListener {
            viewModel.initializeSearchClient()
        }

        viewModel.searchClientStatus.observe(viewLifecycleOwner) { status ->
            binding.apply {
                nsvContent.isVisible = status == Initialized
                layoutSearchProviderStatus.apply {
                    root.isVisible = status != Initialized
                    btnRetry.isVisible = status == Error || status == NotAvailable
                    tvStatus.isVisible = btnRetry.isVisible
                    progressBar.isVisible = status == NotInitialized
                }
            }
        }

        binding.toolbar.menu.findItem(R.id.menu_reset_filter).isVisible = false
        viewModel.filtersLiveData.observe(viewLifecycleOwner) { filters ->
            val filterCount = filters?.getFilters()?.size ?: 0
            binding.toolbar.menu.findItem(R.id.menu_reset_filter).isVisible = filterCount > 0
            updateCategoriesCounter()
        }

        initCategoriesCard()
    }

    private fun onMenuItemClicked(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_reset_filter -> {
                viewModel.clearSearchFilter()
                true
            }

            else -> false
        }
    }

    private fun initCategoriesCard() {
        binding.cardCategories.setOnClickListener {
            showCategoriesDialog()
        }
        updateCategoriesCounter()
    }

    private fun showCategoriesDialog() {
        parentFragmentManager.fragments.forEach { fragment ->
            if (fragment is DialogFragment) {
                // dismiss any open dialogs
                fragment.dismissAllowingStateLoss()
            }
        }
        val dialog = CategoriesDialogFragment.showDialog(parentFragmentManager)
        dialog.setOnDismissListener {
            updateCategoriesCounter()
            viewModel.updateCategoryFilters()
        }
    }

    private fun updateCategoriesCounter() {
        val numCategories = KitsunePref.searchCategories.size
        binding.tvCategoriesCounter.apply {
            isVisible = numCategories > 0
            text = numCategories.toString()
        }
    }

    private fun createFilterViews(filterFacets: SearchViewModel.FilterFacets) {
        val adapterKind = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvKind.initAdapter(adapterKind, binding.tvKind)
        connection += filterFacets.kindConnector.connectView(
            adapterKind,
            filterFacets.kindPresenter
        )

        val yearView = IntNumberRangeView(binding.sliderYear, lifecycleScope)
        binding.sliderYear.attachTextView(binding.tvYearValue)
        connection += filterFacets.yearConnector.connectView(yearView)

        val avgRatingView = IntNumberRangeView(binding.sliderAvgRating, lifecycleScope)
        binding.sliderAvgRating.attachTextView(binding.tvAvgRatingValue, "%d%% - %d%%")
        connection += filterFacets.avgRatingConnector.connectView(avgRatingView)

        val adapterSeason = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvSeason.initAdapter(adapterSeason, binding.tvSeason)
        connection += filterFacets.seasonConnector.connectView(
            adapterSeason,
            filterFacets.seasonPresenter
        )

        val adapterSubtype = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvSubtype.initAdapter(adapterSubtype, binding.tvSubtype)
        binding.wrapperSubtype.connectButton(binding.btnExpandSubtype)
        connection += filterFacets.subtypeConnector.connectView(
            adapterSubtype,
            filterFacets.subtypePresenter
        )

        val adapterStreamers = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvStreamers.initAdapter(adapterStreamers, binding.tvStreamers)
        binding.wrapperStreamers.connectButton(binding.btnExpandStreamers)
        connection += filterFacets.streamersConnector.connectView(
            adapterStreamers,
            filterFacets.streamersPresenter
        )

        val adapterAgeRating = FacetListAdapter(FilterFacetListViewHolder.Factory)
        binding.rvAgeRating.initAdapter(adapterAgeRating, binding.tvAgeRating)
        connection += filterFacets.ageRatingConnector.connectView(
            adapterAgeRating,
            filterFacets.ageRatingPresenter
        )
    }

    private fun RecyclerView.initAdapter(adapter: RecyclerView.Adapter<*>, label: View? = null) {
        this.adapter = adapter
        layoutManager = LinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        addItemDecoration(divider)
        autoScrollToStart(adapter)
        if (label != null) {
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    label.isVisible = adapter.itemCount > 0
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    label.isVisible = adapter.itemCount > 0
                }
            })
        }
    }

    private fun ExpandableLayout.connectButton(button: Button) {
        button.apply {
            setOnClickListener { toggle() }
            setText(if (isExpanded()) R.string.action_show_less else R.string.action_show_more)
        }
        expandedState.observe(viewLifecycleOwner) { expanded ->
            button.setText(if (expanded) R.string.action_show_less else R.string.action_show_more)
        }
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            button.isVisible = minHeight.toInt() <= measuredHeight
        }
    }

    private fun RangeSlider.attachTextView(textView: TextView, format: String = "%d - %d") {
        val updateText = { slider: RangeSlider ->
            val valueMin = slider.values[0].toInt()
            val valueMax = slider.values[1].toInt()
            textView.text = format.format(valueMin, valueMax)
        }
        addOnChangeListener { slider, _, _ ->
            updateText(slider)
        }
        if (values.size >= 2) {
            updateText(this)
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.nsvContent.canScrollVertically(-1)) {
            binding.nsvContent.smoothScrollTo(0, 0)
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connection.clear()
    }

}