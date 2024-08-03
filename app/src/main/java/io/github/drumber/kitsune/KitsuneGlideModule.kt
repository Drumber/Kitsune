package io.github.drumber.kitsune

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.InputStream

@GlideModule
class KitsuneGlideModule : AppGlideModule(), KoinComponent {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val multiTransform = MultiTransformation(
            buildList {
                add(CenterCrop())
                if (BuildConfig.SCREENSHOT_MODE_ENABLED) {
                    val blurTransformation =
                        Class.forName("jp.wasabeef.glide.transformations.BlurTransformation")
                            .getConstructor(Integer.TYPE, Integer.TYPE)
                            .newInstance(15, 2)
                    add(blurTransformation as Transformation<Bitmap>)
                }
            }
        )
        builder.setDefaultRequestOptions(RequestOptions.bitmapTransform(multiTransform))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // replace Glides default OkHttpClient
        val okHttpClient: OkHttpClient = get(named("images"))
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
    }

}

fun <T> RequestBuilder<T>.addTransform(vararg transformations: Transformation<Bitmap>) = with(this) {
    val oldTransforms = this.transformations
        .filterKeys { it.isAssignableFrom(Bitmap::class.java) }
        .map { it.value as Transformation<Bitmap> }
    transform(MultiTransformation(oldTransforms + transformations))
}
