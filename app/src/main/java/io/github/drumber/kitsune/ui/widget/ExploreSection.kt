package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.SectionMainExploreBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import java.util.concurrent.CopyOnWriteArrayList

class ExploreSection(
    private val glide: GlideRequests,
    private val title: String,
    private val initialData: List<ResourceAdapter>? = null,
    private val itemListener: OnItemClickListener<ResourceAdapter>? = null,
    private val headerListener: OnHeaderClickListener? = null
) {

    private lateinit var exploreAdapter: ExploreSectionAdapter

    fun bindView(view: View) {
        val binding = SectionMainExploreBinding.bind(view)
        initView(view.context, binding)
    }

    private fun initView(context: Context, binding: SectionMainExploreBinding) {
        exploreAdapter = ExploreSectionAdapter(
            if(initialData != null) CopyOnWriteArrayList(initialData) else CopyOnWriteArrayList(),
            glide,
            itemListener
        )

        binding.apply {
            rvResource.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = exploreAdapter
            }

            tvTitle.text = title

            header.setOnClickListener {
                headerListener?.onHeaderClick()
            }
        }
    }

    fun setData(dataSet: List<ResourceAdapter>) {
        exploreAdapter.dataSet.apply {
            clear()
            addAll(dataSet)
        }
        exploreAdapter.notifyDataSetChanged()
    }

    fun interface OnHeaderClickListener {
        fun onHeaderClick()
    }

}