package io.github.drumber.kitsune.ui.component

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.data.model.media.Media
import io.github.drumber.kitsune.databinding.SectionMainExploreBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import java.util.concurrent.CopyOnWriteArrayList

class ExploreSection(
    private val glide: RequestManager,
    private val title: String,
    private val initialData: List<Media>? = null,
    private val itemListener: OnItemClickListener<Media>? = null,
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
            itemSize = KitsunePref.mediaItemSize,
            transitionNameSuffix = "_$title",
            listener = itemListener
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

    fun setData(dataSet: List<Media>) {
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