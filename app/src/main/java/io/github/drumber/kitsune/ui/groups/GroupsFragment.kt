package io.github.drumber.kitsune.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupsFragment : Fragment(),
    NavigationBarView.OnItemReselectedListener {

    private val viewModel: GroupsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        GroupsContent()
    }

    @Composable
    private fun GroupsContent() {
        val groups = viewModel.dataSource.collectAsLazyPagingItems()
        val categories by viewModel.categories.collectAsStateWithLifecycle()
        val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
        val isFollowingEnabled by viewModel.isFollowingEnabled.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

        GroupsScreen(
            groups = groups,
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            isLoggedIn = viewModel.isLoggedIn,
            isFollowingEnabled = isFollowingEnabled,
            searchQuery = searchQuery,
            onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
            onFollowingToggle = { viewModel.setFollowingEnabled(it) },
            onCategorySelect = { viewModel.setCategory(it) },
            onGroupClick = { group ->
                findNavController().navigateSafe(
                    R.id.groups_fragment,
                    GroupsFragmentDirections.actionGroupsFragmentToGroupDetailFragment(group.id)
                )
            },
            onNavigateUp = { findNavController().navigateUp() },
            scrollToTopEvents = viewModel.scrollToTopRequested
        )
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        viewModel.requestScrollToTop()
    }
}

