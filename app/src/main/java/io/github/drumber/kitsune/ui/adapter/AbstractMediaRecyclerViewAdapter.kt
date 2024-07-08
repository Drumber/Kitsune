package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.databinding.OnRebindCallback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.AbstractMediaRecyclerViewAdapter.AbstractMediaViewHolder
import java.util.concurrent.CopyOnWriteArrayList

abstract class AbstractMediaRecyclerViewAdapter<ViewHolder : AbstractMediaViewHolder<Item>, Item>(
    val dataSet: CopyOnWriteArrayList<Item>,
    private val glide: RequestManager,
    private val transitionNameSuffix: String?,
    private val listener: OnItemClickListener<Item>?
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.apply {
            contentWrapper.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            cardMedia.isInGridLayout = false
        }

        binding.addOnRebindCallback(object : OnRebindCallback<ItemMediaBinding>() {
            override fun onBound(binding: ItemMediaBinding) {
                // If the same media (with the same ID) is shown in more than one recyclerview,
                // the shared-element-transition won't work. To make the transition name unique again,
                // we may add a suffix (e.g. the section title on the home page).
                transitionNameSuffix?.let { binding.cardMedia.fixUniqueTransitionName(it) }
            }
        })

        return onCreateViewHolder(binding, glide) { view, position ->
            if (position < dataSet.size) {
                listener?.onItemClick(view, dataSet[position])
            }
        }
    }

    abstract fun onCreateViewHolder(
        binding: ItemMediaBinding,
        glide: RequestManager,
        listener: (View, Int) -> Unit
    ): ViewHolder

    /** Add a suffix to the transitionName if not already present. */
    private fun View.fixUniqueTransitionName(suffix: String) {
        ViewCompat.getTransitionName(this)?.let { transitionName ->
            if (!transitionName.endsWith(suffix))
                ViewCompat.setTransitionName(this, transitionName + suffix)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    abstract class AbstractMediaViewHolder<T>(
        binding: ItemMediaBinding,
        onClick: (View, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cardMedia.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(it, position)
                }
            }
        }

        abstract fun bind(data: T)
    }
}