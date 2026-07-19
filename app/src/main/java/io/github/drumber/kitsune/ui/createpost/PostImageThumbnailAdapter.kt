package io.github.drumber.kitsune.ui.createpost

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.databinding.ItemCreatePostImageBinding
import java.util.Collections

class PostImageThumbnailAdapter(
    private val glide: RequestManager,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<PostImageThumbnailAdapter.ImageViewHolder>() {

    private val items = mutableListOf<String>()

    fun submitItems(newItems: List<String>) {
        if (items == newItems) return
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun currentItems(): List<String> = items.toList()

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition !in items.indices || toPosition !in items.indices) return
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ItemCreatePostImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ImageViewHolder(private val binding: ItemCreatePostImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: String) {
            glide.load(Uri.parse(uri))
                .centerCrop()
                .into(binding.ivThumbnail)

            binding.btnRemoveImage.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemoveClick(position)
                }
            }
        }
    }
}
