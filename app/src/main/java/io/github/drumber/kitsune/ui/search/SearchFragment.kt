package io.github.drumber.kitsune.ui.search

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paulrybitskyi.persistentsearchview.adapters.model.SuggestionItem
import com.paulrybitskyi.persistentsearchview.listeners.OnSearchConfirmedListener
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
import io.github.drumber.kitsune.util.getColor
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.toPx
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
                val sortFilter = SortFilter.fromQueryParam(it.filter.options["sort"])
                    ?: SortFilter.POPULARITY_DESC
                chipSort.setText(sortFilter.toStringRes())
            }
        }

        initSearchView()
    }

    private fun initSearchView() {
        binding.searchView.apply {
            setVoiceRecognitionDelegate(VoiceRecognitionDelegate(this@SearchFragment))
            setSuggestionsDisabled(false)
            customOnSearchConfirmedListener = searchPerformedListener
            setAppBarLayout(binding.appBarLayout)

            lifecycleScope.launchWhenStarted {
                setCardBackgroundColor(context.theme.getColor(R.attr.colorSearchView))
                setSuggestionTextColor(ContextCompat.getColor(context, R.color.foreground))
                collapse(false) // make sure to collapse on view change
            }

            setOnLeftBtnClickListener { this.expand() }

            setOnExpandStateChangeListener { expanded ->
                setAppBarBackgrounds(expanded)

                binding.chipGroupFilter.isVisible = !expanded
                binding.rvResource.apply {
                    isEnabled = !expanded
                    if(ViewCompat.isLaidOut(this)) {
                        // toggle app bar behaviour to display recyclerview behind search overlay
                        val params = this.layoutParams as CoordinatorLayout.LayoutParams
                        params.behavior = if(expanded) {
                            null
                        } else {
                            AppBarLayout.ScrollingViewBehavior()
                        }
                        // compensate padding offset
                        val offset = 107.toPx()
                        this.updatePadding(top = if(expanded) offset else 0)
                        scrollBy(0, if(expanded) -offset else offset)
                        this.requestLayout()
                    }
                }
            }

            val initialSuggestions = if(isInputQueryEmpty) {
                KitsunePref.searchQueries.getSearchQueries()
            } else {
                KitsunePref.searchQueries.findSuggestions(inputQuery)
            }
            setSuggestions(initialSuggestions, false)

            setOnSearchQueryChangeListener { searchView, oldQuery, newQuery ->
                val suggestions = if (newQuery.isBlank()) {
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
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            binding.searchView.apply {
                if (isExpanded) {
                    collapse()
                    return@addCallback
                }
            }
            findNavController().navigateUp()
        }
    }


    private val searchPerformedListener = OnSearchConfirmedListener { searchView, query ->
        searchView.collapse()
        if(query.isNotBlank()) {
            saveSearchQuery(query)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        // TODO: search
    }

    private fun setSuggestions(queries: List<String>, expandIfNecessary: Boolean = true) {
        val suggestions = SuggestionCreationUtil.asRecentSearchSuggestions(queries)
        binding.searchView.setSuggestions(suggestions, expandIfNecessary)
    }

    private fun saveSearchQuery(query: String) {
        KitsunePref.searchQueries.addQuery(query)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VoiceRecognitionDelegate.handleResult(binding.searchView, requestCode, resultCode, data)
    }

    private var appBarAnimator: ValueAnimator? = null
    private fun setAppBarBackgrounds(isSearchExpanded: Boolean) {
        val colorSurface = requireContext().theme.getColor(R.attr.colorSurface)
        val colorTranslucent = Color.argb(200, Color.red(colorSurface), Color.green(colorSurface), Color.blue(colorSurface))

        val colorFrom = if(isSearchExpanded) colorSurface else colorTranslucent
        val colorTo = if(isSearchExpanded) colorTranslucent else colorSurface

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimator.duration = if(isSearchExpanded) 400 else 0
        colorAnimator.addUpdateListener {
            binding.appBarLayout.setBackgroundColor(it.animatedValue as Int)
        }

        appBarAnimator?.cancel()
        appBarAnimator = colorAnimator
        colorAnimator.start()

        binding.searchWrapper.setBackgroundColor(if(isSearchExpanded) Color.TRANSPARENT else colorSurface)
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
                if (prevSelected != selectedNow) {
                    val resourceType = ResourceType.values()[selectedNow]
                    val selector =
                        viewModel.currentResourceSelector.copy(resourceType = resourceType)
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
        val lastSortFilter =
            SortFilter.fromQueryParam(viewModel.currentResourceSelector.filter.options["sort"])
        val prevSelected = lastSortFilter?.ordinal ?: 0
        var selectedNow = prevSelected
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_sort)
            .setNeutralButton(R.string.action_cancel) { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.action_ok) { dialog, which ->
                if (prevSelected != selectedNow) {
                    val sortFilter = SortFilter.values()[selectedNow]
                    val prevFilter = viewModel.currentResourceSelector.filter
                    val selector =
                        viewModel.currentResourceSelector.copy(filter = prevFilter.sort(sortFilter.queryParam))
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