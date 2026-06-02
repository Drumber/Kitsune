package io.github.drumber.kitsune.ui.postdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ItemPostImageBinding

/** Adapter backing the swipeable image gallery on the post detail screen. */
class PostImagePagerAdapter(
    private val glide: RequestManager,
    private val imageUrls: List<String>
) : RecyclerView.Adapter<PostImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ItemPostImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount() = imageUrls.size

    inner class ImageViewHolder(private val binding: ItemPostImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            glide.load(url)
                .placeholder(R.drawable.ic_insert_photo_48)
                .fitCenter()
                .into(binding.ivImage)
        }

    }

}
