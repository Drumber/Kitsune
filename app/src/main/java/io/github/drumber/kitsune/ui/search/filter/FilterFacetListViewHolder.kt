package io.github.drumber.kitsune.ui.search.filter

import android.view.View
import android.view.ViewGroup
import com.algolia.instantsearch.android.filter.facet.FacetListViewHolder
import com.algolia.instantsearch.android.inflate
import com.algolia.search.model.search.Facet
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ItemFacetBinding

class FilterFacetListViewHolder(view: View) : FacetListViewHolder(view) {

    override fun bind(facet: Facet, selected: Boolean, onClickListener: View.OnClickListener) {
        val binding = ItemFacetBinding.bind(view)
        view.setOnClickListener(onClickListener)
        binding.apply {
            facetCount.text = facet.count.toString()
            facetCount.visibility = View.VISIBLE
            icon.visibility = if (selected) View.VISIBLE else View.INVISIBLE
            facetName.text = facet.value.replaceFirstChar(Char::titlecase)
        }
    }

    object Factory : FacetListViewHolder.Factory {

        override fun createViewHolder(parent: ViewGroup): FacetListViewHolder {
            return FilterFacetListViewHolder(parent.inflate(R.layout.item_facet))
        }
    }
}