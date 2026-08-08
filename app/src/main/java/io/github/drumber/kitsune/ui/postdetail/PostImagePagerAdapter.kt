package io.github.drumber.kitsune.ui.postdetail

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ItemPostImageBinding

/** Adapter backing the swipeable image gallery on the post detail screen. */
class PostImagePagerAdapter(
    private val glide: RequestManager,
    private val imageUrls: List<String>,
    private val onImageClicked: (String) -> Unit,
    /** Reports the aspect ratio (width / height) of the first image once it is loaded. */
    private val onFirstImageReady: ((aspectRatio: Float) -> Unit),
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
            var request = glide.load(url)
                .placeholder(R.drawable.ic_insert_photo_48)
                .fitCenter()

            if (position == 0 && onFirstImageReady != null) {
                request = request.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean = false

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        val width = resource.intrinsicWidth
                        val height = resource.intrinsicHeight
                        if (width > 0 && height > 0) {
                            onFirstImageReady.invoke(width.toFloat() / height.toFloat())
                        }
                        return false
                    }
                })
            }

            request.into(binding.ivImage)

            binding.root.setOnClickListener { onImageClicked(url) }
        }

    }

}
