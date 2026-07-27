package io.github.drumber.kitsune.util.image

import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.core.graphics.createBitmap
import coil3.Bitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageResult
import coil3.request.crossfade
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import coil3.util.DebugLogger
import io.github.drumber.kitsune.BuildConfig
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.io.File

private const val IMAGE_DISK_CACHE_DIR = "image_cache"
private const val IMAGE_DISK_CACHE_SIZE_BYTES = 256L * 1024 * 1024

/**
 * Builds the app-wide Coil [ImageLoader].
 *
 * Replaces the former `KitsuneGlideModule`: it reuses the unauthenticated `named("images")`
 * OkHttp client and, in screenshot mode, blurs every image so demo screenshots don't leak
 * real cover art.
 */
fun buildKitsuneImageLoader(
    context: PlatformContext,
    okHttpClient: OkHttpClient,
    cacheDirectory: File
): ImageLoader = ImageLoader.Builder(context)
    .components {
        add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add(AnimatedImageDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
        if (BuildConfig.SCREENSHOT_MODE_ENABLED) {
            add(BlurEverythingInterceptor)
        }
    }
    .memoryCache {
        MemoryCache.Builder()
            .maxSizePercent(context, 0.25)
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(cacheDirectory.resolve(IMAGE_DISK_CACHE_DIR).toOkioPath())
            .maxSizeBytes(IMAGE_DISK_CACHE_SIZE_BYTES)
            .build()
    }
    .crossfade(true)
    .apply {
        if (BuildConfig.DEBUG) {
            logger(DebugLogger())
        }
    }
    .build()

private object BlurEverythingInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request.newBuilder()
            .transformations(chain.request.transformations + ScreenshotBlurTransformation)
            .build()
        return chain.withRequest(request).proceed()
    }
}

/**
 * Cheap downscale-then-upscale blur, used only when `SCREENSHOT_MODE_ENABLED` is set.
 * Replaces the former `jp.wasabeef:glide-transformations` BlurTransformation.
 */
private object ScreenshotBlurTransformation : Transformation() {

    private const val DOWNSCALE_FACTOR = 12

    override val cacheKey: String = "screenshot-blur-$DOWNSCALE_FACTOR"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val smallWidth = (input.width / DOWNSCALE_FACTOR).coerceAtLeast(1)
        val smallHeight = (input.height / DOWNSCALE_FACTOR).coerceAtLeast(1)
        val config = input.config ?: Config.ARGB_8888
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

        val small = createBitmap(smallWidth, smallHeight, config)
        Canvas(small).drawBitmap(input, null, Rect(0, 0, smallWidth, smallHeight), paint)

        val output = createBitmap(input.width, input.height, config)
        Canvas(output).drawBitmap(small, null, Rect(0, 0, input.width, input.height), paint)
        small.recycle()
        return output
    }
}
