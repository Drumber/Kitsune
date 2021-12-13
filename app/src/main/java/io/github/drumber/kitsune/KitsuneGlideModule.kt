package io.github.drumber.kitsune

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class KitsuneGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val multiTransform = MultiTransformation(
            buildList {
                add(CenterCrop())
                if (BuildConfig.SCREENSHOT_MODE_ENABLED) {
                    val blurTransformation =
                        Class.forName("jp.wasabeef.glide.transformations.BlurTransformation")
                            .getConstructor(Integer::class.java, Integer::class.java)
                            .newInstance(15, 2)
                    add(blurTransformation as Transformation<Bitmap>)
                }
            }
        )
        builder.setDefaultRequestOptions(RequestOptions.bitmapTransform(multiTransform))
    }

}

fun GlideRequest<*>.addTransform(vararg transformations: Transformation<Bitmap>) = with(this) {
    val oldTransforms = this.transformations
        .filterKeys { it.isAssignableFrom(Bitmap::class.java) }
        .map { it.value as Transformation<Bitmap> }
    transform(MultiTransformation(oldTransforms + transformations))
}
