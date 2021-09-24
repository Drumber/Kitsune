package io.github.drumber.kitsune.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paulrybitskyi.persistentsearchview.adapters.model.SuggestionItem
import com.paulrybitskyi.persistentsearchview.listeners.OnSuggestionChangeListener
import com.paulrybitskyi.persistentsearchview.utils.SuggestionCreationUtil
import com.paulrybitskyi.persistentsearchview.utils.VoiceRecognitionDelegate
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.constants.toStringRes
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.model.toStringRes
import io.github.drumber.kitsune.databinding.FragmentSearchBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.ui.base.BaseCollectionViewModel
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : BaseCollectionFragment(R.layout.fragment_search) {

    private val binding: FragmentSearchBinding by viewBinding()

    private val viewModel: SearchViewModel by viewModel()

    override val collectionViewModel: BaseCollectionViewModel
        get() = viewModel

    override val recyclerView: RecyclerView
        get() = binding.rvResource

    override val resourceLoadingBinding: LayoutResourceLoadingBinding?
        get() = binding.layoutLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            root.initPaddingWindowInsetsListener(
                left = true,
                top = true,
                right = true,
                bottom = false
            )

            chipResourceSelector.setOnClickListener { showResourceSelectorDialog() }
            chipSort.setOnClickListener { showSortDialog() }
        }

        viewModel.resourceSelector.observe(viewLifecycleOwner) {
            binding.apply {
                chipResourceSelector.setText(it.resourceType.toStringRes())
                val sortFilter = SortFilter.fromQueryParam(it.filter.options["sort"]) ?: SortFilter.POPULARITY_DESC
                chipSort.setText(sortFilter.toStringRes())
            }
        }

        initSearchView()
    }

    private fun initSearchView() {
        binding.searchView.apply {
            setVoiceRecognitionDelegate(VoiceRecognitionDelegate(this@SearchFragment))

            setOnLeftBtnClickListener {
                this.expand()
            }

            setOnSearchConfirmedListener { searchView, query ->
                saveSearchQuery(query)
                searchView.collapse()
                performSearch(query)
            }

            setOnSearchQueryChangeListener { searchView, oldQuery, newQuery ->
                val suggestions = if(newQuery.isBlank()) {
                    KitsunePref.searchQueries.getSearchQueries()
                } else {
                    KitsunePref.searchQueries.findSuggestions(newQuery)
                }
                setSuggestions(suggestions)
            }

            setOnSuggestionChangeListener(object : OnSuggestionChangeListener {
                override fun onSuggestionPicked(suggestion: SuggestionItem) {
                    val query = suggestion.itemModel.text
                    saveSearchQuery(query)
                    setSuggestions(KitsunePref.searchQueries.findSuggestions(query))
                    performSearch(query)
                }
                override fun onSuggestionRemoved(suggestion: SuggestionItem) {
                    KitsunePref.searchQueries.removeQuery(suggestion.itemModel.text)
                }
            })

            setSuggestionsDisabled(false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            binding.searchView.apply {
                if(isExpanded) {
                    collapse()
                    return@addCallback
                }
            }
            findNavController().navigateUp()
        }
    }


    private fun performSearch(query: String) {
        // TODO: search
    }

    private fun setSuggestions(queries: List<String>) {
        val suggestions = SuggestionCreationUtil.asRecentSearchSuggestions(queries)
        binding.searchView.setSuggestions(suggestions, true)
    }

    private fun saveSearchQuery(query: String) {
        KitsunePref.searchQueries.addQuery(query)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VoiceRecognitionDelegate.handleResult(binding.searchView, requestCode, resultCode, data)
    }

    private fun showResourceSelectorDialog() {
        val items = ResourceType.values().map { getString(it.toStringRes()) }.toTypedArray()
        val prevSelected = viewModel.currentResourceSelector.resourceType.ordinal
        var selectedNow = prevSelected
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_resource_type)
            .setNeutralButton(R.string.action_cancel) { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.action_ok) { dialog, which ->
                if(prevSelected != selectedNow) {
                    val resourceType = ResourceType.values()[selectedNow]
                    val selector = viewModel.currentResourceSelector.copy(resourceType = resourceType)
                    viewModel.setResourceSelector(selector)
                }
                dialog.dismiss()
            }
            .setSingleChoiceItems(items, prevSelected) { dialog, which ->
                selectedNow = which
            }
            .show()
    }

    private fun showSortDialog() {
        val items = SortFilter.values().map { getString(it.toStringRes()) }.toTypedArray()
        val lastSortFilter = SortFilter.fromQueryParam(viewModel.currentResourceSelector.filter.options["sort"])
        val prevSelected = lastSortFilter?.ordinal ?: 0
        var selectedNow = prevSelected
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_sort)
            .setNeutralButton(R.string.action_cancel) { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.action_ok) { dialog, which ->
                if(prevSelected != selectedNow) {
                    val sortFilter = SortFilter.values()[selectedNow]
                    val prevFilter = viewModel.currentResourceSelector.filter
                    val selector = viewModel.currentResourceSelector.copy(filter = prevFilter.sort(sortFilter.queryParam))
                    viewModel.setResourceSelector(selector)
                }
                dialog.dismiss()
            }
            .setSingleChoiceItems(items, prevSelected) { dialog, which ->
                selectedNow = which
            }
            .show()
    }

    override fun onResourceClicked(model: ResourceAdapter, options: NavOptions) {
        val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(model)
        findNavController().navigate(action, options)
    }

}