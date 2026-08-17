package io.github.drumber.kitsune.util.markwon

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.widget.TextView
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.request.Disposable
import coil3.request.ImageRequest
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.DrawableUtils
import io.noties.markwon.image.ImageSpanFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Coil3 image loading plugin for Markwon.
 */
class Coil3ImagesPlugin private constructor(
    context: Context,
    imageLoader: ImageLoader
) : AbstractMarkwonPlugin() {

    private val asyncDrawableLoader = Coil3AsyncDrawableLoader(
        context.applicationContext,
        imageLoader,
        context.resources
    )

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(org.commonmark.node.Image::class.java, ImageSpanFactory())
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.asyncDrawableLoader(asyncDrawableLoader)
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        AsyncDrawableScheduler.unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        AsyncDrawableScheduler.schedule(textView)
    }

    companion object {
        fun create(context: Context): Coil3ImagesPlugin =
            create(context, SingletonImageLoader.get(context))

        fun create(context: Context, imageLoader: ImageLoader): Coil3ImagesPlugin =
            Coil3ImagesPlugin(context, imageLoader)
    }

    private class Coil3AsyncDrawableLoader(
        private val context: Context,
        private val imageLoader: ImageLoader,
        private val resources: Resources
    ) : AsyncDrawableLoader() {

        private val cache = ConcurrentHashMap<AsyncDrawable, Disposable>()

        override fun load(drawable: AsyncDrawable) {
            val loaded = AtomicBoolean(false)
            val request = ImageRequest.Builder(context)
                .data(drawable.destination)
                .target(
                    onStart = { placeholder ->
                        placeholder?.let { drawable.setResultIfAttached(it) }
                    },
                    onSuccess = { result ->
                        if (cache.remove(drawable) != null || !loaded.get()) {
                            loaded.set(true)
                            drawable.setResultIfAttached(result)
                        }
                    },
                    onError = { error ->
                        if (cache.remove(drawable) != null) {
                            error?.let { drawable.setResultIfAttached(it) }
                        }
                    }
                )
                .build()

            val disposable = imageLoader.enqueue(request)
            if (!loaded.get()) {
                loaded.set(true)
                cache[drawable] = disposable
            }
        }

        override fun cancel(drawable: AsyncDrawable) {
            cache.remove(drawable)?.dispose()
        }

        override fun placeholder(drawable: AsyncDrawable): Drawable? = null

        private fun AsyncDrawable.setResultIfAttached(image: Image) {
            if (this.isAttached) {
                val drawable = image.asDrawable(resources)
                DrawableUtils.applyIntrinsicBoundsIfEmpty(drawable)
                this.result = drawable
            }
        }
    }
}
