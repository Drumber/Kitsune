package io.github.drumber.kitsune.ui.groups

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.shape.MaterialShapeDrawable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.GroupCategory
import io.github.drumber.kitsune.databinding.FragmentGroupsBinding
import io.github.drumber.kitsune.ui.adapter.paging.GroupsPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupsFragment : Fragment(R.layout.fragment_groups),
 NavigationBarView.OnItemReselectedListener {

    private val binding by viewBinding(FragmentGroupsBinding::bind)

    private val viewModel: GroupsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets()

        val adapter = GroupsPagingAdapter(Glide.with(this)) { group ->
            findNavController().navigateSafe(
                R.id.groups_fragment,
                GroupsFragmentDirections.actionGroupsFragmentToGroupDetailFragment(group.id)
            )
        }
        binding.rvGroups.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvGroups.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener { adapter.refresh() }
        }

        setupSearchAndFilterControls()
        observeViewModel(adapter)
    }

    private fun applyWindowInsets() {
        binding.apply {
            appBarLayout.statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(context)
            toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            inputLayoutSearch.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
            horizontalScrollView.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
            rvGroups.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                bottom = true,
                consume = false
            )
        }
    }

    private fun setupSearchAndFilterControls() {
        binding.editSearch.apply {
            doAfterTextChanged { text ->
                viewModel.setSearchQuery(text?.toString())
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.setSearchQuery(text?.toString())
                    true
                } else {
                    false
                }
            }
        }

        binding.chipFollowing.apply {
            isVisible = viewModel.isLoggedIn
            isChecked = viewModel.isFollowingEnabled.value
            setOnClickListener { viewModel.setFollowingEnabled(isChecked) }
        }
        binding.dividerFollowing.isVisible = viewModel.isLoggedIn
    }

    private fun observeViewModel(adapter: GroupsPagingAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvGroups,
                        adapter.itemCount,
                        loadState
                    )
                    binding.swipeRefreshLayout.isRefreshing =
                        binding.swipeRefreshLayout.isRefreshing &&
                        loadState.refresh is LoadState.Loading &&
                        adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataSource.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { categories ->
                    bindCategories(categories)
                }
            }
        }
    }

    private fun bindCategories(categories: List<GroupCategory>) {
        val chipGroup = binding.chipGroupCategories
        chipGroup.removeAllViews()
        if (categories.isEmpty()) return

        val allChip = createCategoryChip(getString(R.string.groups_category_all), null)
        allChip.isChecked = viewModel.selectedCategoryId.value == null
        chipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = createCategoryChip(category.name ?: return@forEach, category.id)
            chip.isChecked = viewModel.selectedCategoryId.value == category.id
            chipGroup.addView(chip)
        }
    }

    private fun createCategoryChip(label: String, categoryId: String?): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            setOnClickListener {
                viewModel.setCategory(if (isChecked) categoryId else null)
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
        binding.rvGroups.smoothScrollToPosition(0)
    }
}
