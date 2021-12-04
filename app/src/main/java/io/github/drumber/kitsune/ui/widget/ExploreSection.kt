package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.SectionMainExploreBinding
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import java.util.concurrent.CopyOnWriteArrayList

class ExploreSection(
    private val glide: GlideRequests,
    private val title: String,
    private val initialData: List<MediaAdapter>? = null,
    private val itemListener: OnItemClickListener<MediaAdapter>? = null,
    private val headerListener: OnHeaderClickListener? = null
) {

    private lateinit var exploreAdapter: MediaRecyclerViewAdapter

    fun bindView(view: View) {
        val binding = SectionMainExploreBinding.bind(view)
        initView(view.context, binding)
    }

    private fun initView(context: Context, binding: SectionMainExploreBinding) {
        exploreAdapter = MediaRecyclerViewAdapter(
            if(initialData != null) CopyOnWriteArrayList(initialData) else CopyOnWriteArrayList(),
            glide,
            itemListener
        )

        binding.apply {
            rvMedia.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = exploreAdapter
            }

            tvTitle.text = title

            header.setOnClickListener {
                headerListener?.onHeaderClick()
            }
        }
    }

    fun setData(dataSet: List<MediaAdapter>) {
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