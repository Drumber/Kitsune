package io.github.drumber.kitsune.ui.search.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.filter.facet.connectView
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.component.algolia.range.connectView
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference
import io.github.drumber.kitsune.ui.search.SearchViewModel
import io.github.drumber.kitsune.ui.search.categories.CategoriesDialogFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FacetFragment : Fragment(),
    FragmentDecorationPreference,
    NavigationBarView.OnItemReselectedListener {

    override val hasTransparentStatusBar = true

    private val connection = ConnectionHandler()

    private val viewModel: SearchViewModel by activityViewModel()

    private val kindState = FacetListViewState()
    private val seasonState = FacetListViewState()
    private val subtypeState = FacetListViewState()
    private val streamersState = FacetListViewState()
    private val ageRatingState = FacetListViewState()
    private val yearState = NumberRangeViewState()
    private val avgRatingState = NumberRangeViewState()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val clientStatus by viewModel.searchClientStatus.collectAsStateWithLifecycle()
        val filters by viewModel.filtersLiveData.collectAsStateWithLifecycle()
        val filterCount = filters?.getFilters()?.size ?: 0
        val categoriesCount = KitsunePref.searchCategories.size

        FacetScreen(
            clientStatus = clientStatus ?: SearchViewModel.SearchClientStatus.NotInitialized,
            onRetrySearchClient = { viewModel.initializeSearchClient() },
            filterCount = filterCount,
            onResetFilter = { viewModel.clearSearchFilter() },
            onNavigateUp = { findNavController().navigateUp() },
            onCategoriesClick = { showCategoriesDialog() },
            categoriesCount = categoriesCount,
            kindState = kindState,
            yearState = yearState,
            avgRatingState = avgRatingState,
            seasonState = seasonState,
            subtypeState = subtypeState,
            streamersState = streamersState,
            ageRatingState = ageRatingState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFilterFacets()
    }

    private fun observeFilterFacets() {
        viewModel.filterFacets.observe(viewLifecycleOwner) { filterFacets ->
            connection.clear()
            connection += filterFacets.kindConnector.connectView(kindState, filterFacets.kindPresenter)
            connection += filterFacets.yearConnector.connectView(yearState)
            connection += filterFacets.avgRatingConnector.connectView(avgRatingState)
            connection += filterFacets.seasonConnector.connectView(seasonState, filterFacets.seasonPresenter)
            connection += filterFacets.subtypeConnector.connectView(subtypeState, filterFacets.subtypePresenter)
            connection += filterFacets.streamersConnector.connectView(streamersState, filterFacets.streamersPresenter)
            connection += filterFacets.ageRatingConnector.connectView(ageRatingState, filterFacets.ageRatingPresenter)
        }
    }

    private fun showCategoriesDialog() {
        parentFragmentManager.fragments.forEach { fragment ->
            if (fragment is DialogFragment) {
                fragment.dismissAllowingStateLoss()
            }
        }
        val dialog = CategoriesDialogFragment.showDialog(parentFragmentManager)
        dialog.setOnDismissListener {
            viewModel.updateCategoryFilters()
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        connection.clear()
        super.onDestroyView()
    }
}
