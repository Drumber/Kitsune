package io.github.drumber.kitsune.util.algolia

import com.algolia.instantsearch.filter.facet.FacetListItem
import com.algolia.instantsearch.filter.facet.FacetListPresenter

class SeasonListPresenter(
    private val sortOrder: Array<String> = arrayOf("spring", "summer", "fall", "winter")
) : FacetListPresenter {

    private val comparator = Comparator<FacetListItem> { (facetA, _), (facetB, _) ->
        val indexA = sortOrder.indexOf(facetA.value.lowercase())
        val indexB = sortOrder.indexOf(facetB.value.lowercase())
        indexA.compareTo(indexB)
    }

    override fun invoke(selectableItems: List<FacetListItem>): List<FacetListItem> {
        return selectableItems.sortedWith(comparator)
    }
}