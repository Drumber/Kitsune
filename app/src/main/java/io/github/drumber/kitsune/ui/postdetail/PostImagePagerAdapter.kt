package io.github.drumber.kitsune.ui.postdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.load
import io.github.drumber.kitsune.databinding.ItemPostImageBinding

/** Adapter backing the swipeable image gallery on the post detail screen. */
class PostImagePagerAdapter(
    private val imageLoader: ImageLoader,
    private val imageUrls: List<String>,
    private val onImageClicked: (String) -> Unit,
    /** Reports the aspect ratio (width / height) of the first image once it is loaded. */
    private val onFirstImageReady: (aspectRatio: Float) -> Unit,
) : RecyclerView.Adapter<PostImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ItemPostImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position], position)
    }

    override fun getItemCount() = imageUrls.size

    inner class ImageViewHolder(private val binding: ItemPostImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String, position: Int) {
            binding.ivImage.load(url, imageLoader = imageLoader) {
                if (position == 0) {
                    listener(
                        onSuccess = { _, result ->
                            val width = result.image.width
                            val height = result.image.height
                            if (width > 0 && height > 0) {
                                onFirstImageReady.invoke(width.toFloat() / height.toFloat())
                            }
                        }
                    )
                }
            }

            binding.root.setOnClickListener { onImageClicked(url) }
        }

    }

}
