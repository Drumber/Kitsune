package io.github.drumber.kitsune.di

import android.content.Context
import android.os.Build
import android.os.Parcelable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import coil3.ImageLoader
import coil3.asImage
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.algolia.search.model.filter.Filter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.github.jasminb.jsonapi.ResourceConverter
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import com.google.android.material.elevation.SurfaceColors
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.util.json.AlgoliaFacetValueDeserializer
import io.github.drumber.kitsune.util.json.AlgoliaNumericValueDeserializer
import io.github.drumber.kitsune.util.json.IgnoreParcelablePropertyMixin
import io.github.drumber.kitsune.util.network.AuthenticationInterceptor
import io.github.drumber.kitsune.util.network.AuthenticationInterceptorImpl
import io.github.drumber.kitsune.util.network.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import com.github.jasminb.jsonapi.DeserializationFeature as JsonApiDeserializationFeature

private const val HTTP_CACHE_DIR = "http_cache"
private const val HTTP_CACHE_SIZE = 1024L * 1024L * 5L // 5 MiB

private const val DEFAULT_IMAGE_CACHE_DIR = "image_cache"
private const val DEFAULT_IMAGE_CACHE_SIZE = 1024 * 1024 * 192L // 192 MB
private const val DEFAULT_IMAGE_MEMORY_CACHE_PERCENT = 0.15
private const val SOCIAL_IMAGE_CACHE_DIR = "image_cache_social"
private const val SOCIAL_IMAGE_CACHE_SIZE = 1024 * 1024 * 64L // 64 MB
private const val SOCIAL_IMAGE_MEMORY_CACHE_PERCENT = 0.10

object UnauthenticatedHttpClient
object ImagesHttpClient
object SocialImagesLoader

val networkModule = module {
    // default API HTTP client with authentication, logging and caching
    single { createApiHttpClient(get(), get()) }
    // unauthenticated HTTP client
    single(named<UnauthenticatedHttpClient>()) { createHttpClientBuilder().build() }
    // image loading HTTP client without caching and without logging
    single(named<ImagesHttpClient>()) { createHttpClientBuilder(false).build() }

    single { createObjectMapper() }
    factory<AuthenticationInterceptor> { AuthenticationInterceptorImpl(get()) }

    // default Coil image loader
    single { createImageLoader(androidApplication(), get(named<ImagesHttpClient>())) }
    // social Coil image loader (with separate cache folder)
    single(named<SocialImagesLoader>()) {
        createImageLoader(
            androidApplication(),
            get(named<ImagesHttpClient>()),
            cacheDir = SOCIAL_IMAGE_CACHE_DIR,
            diskCacheSize = SOCIAL_IMAGE_CACHE_SIZE,
            memoryCacheSizePercent = SOCIAL_IMAGE_MEMORY_CACHE_PERCENT
        )
    }
}

fun createHttpClientBuilder(addLoggingInterceptor: Boolean = true) = OkHttpClient.Builder()
    .addInterceptor(createUserAgentInterceptor())
    .apply {
        if (addLoggingInterceptor) {
            addNetworkInterceptor(createHttpLoggingInterceptor())
        }
    }
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)

private fun createApiHttpClient(
    context: Context,
    authenticationInterceptor: AuthenticationInterceptor
) = createHttpClientBuilder()
    .addInterceptor(authenticationInterceptor)
    .authenticator(authenticationInterceptor)
    .cache(
        Cache(
            directory = File(context.cacheDir, HTTP_CACHE_DIR),
            maxSize = HTTP_CACHE_SIZE
        )
    )
    .build()

private fun createHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
    level = when (BuildConfig.DEBUG) {
        true -> HttpLoggingInterceptor.Level.HEADERS
        false -> HttpLoggingInterceptor.Level.BASIC
    }
    redactHeader("Authorization")
}

fun createUserAgentInterceptor() =
    UserAgentInterceptor("Kitsune/${BuildConfig.VERSION_NAME}")

fun createImageLoader(
    context: Context,
    imageHttpClient: OkHttpClient,
    cacheDir: String = DEFAULT_IMAGE_CACHE_DIR,
    diskCacheSize: Long = DEFAULT_IMAGE_CACHE_SIZE,
    memoryCacheSizePercent: Double = DEFAULT_IMAGE_MEMORY_CACHE_PERCENT,
): ImageLoader {
    return ImageLoader.Builder(context)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(cacheDir))
                .maxSizeBytes(diskCacheSize)
                .build()
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, memoryCacheSizePercent)
                .build()
        }
        .components {
            add(OkHttpNetworkFetcherFactory(callFactory = { imageHttpClient }))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(AnimatedImageDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
            add(SvgDecoder.Factory())
        }
        .crossfade(true)
        .placeholder { SurfaceColors.SURFACE_1.getColor(it.context).toDrawable().asImage() }
        .error {
            AppCompatResources.getDrawable(it.context, R.drawable.default_placeholder)?.asImage()
        }
        .fallback {
            AppCompatResources.getDrawable(it.context, R.drawable.default_placeholder)?.asImage()
        }
        .build()
}

fun createObjectMapper(): ObjectMapper = jacksonMapperBuilder()
    .defaultPropertyInclusion(JsonInclude.Value.ALL_NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
    .addMixIn(Parcelable::class.java, IgnoreParcelablePropertyMixin::class.java)
    .addModule(
        SimpleModule().addDeserializer(
            Filter.Facet.Value::class.java,
            AlgoliaFacetValueDeserializer()
        )
    )
    .addModule(
        SimpleModule().addDeserializer(
            Filter.Numeric.Value::class.java,
            AlgoliaNumericValueDeserializer()
        )
    )
    .build()

fun createConverterFactory(
    httpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    vararg classes: Class<*>
): JSONAPIConverterFactory {
    val resourceConverter = ResourceConverter(objectMapper, *classes)
    // Feed responses include resource types (e.g. comments, mediaReactions) that are not
    // registered with every converter. Without this option the converter throws when it
    // encounters such a type in the `included` section, causing the whole request to fail.
    resourceConverter.enableDeserializationOption(JsonApiDeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS)
    // The notifications feed has polymorphic `subject`/`target` relationships that can point to
    // resource types (users, anime, library entries, ...) which are not assignable to the
    // interface-typed relationship fields. Without this option the converter throws an
    // UnregisteredTypeException for such relationships, failing the whole request. With it, the
    // unmappable relationship is simply left unset.
    resourceConverter.enableDeserializationOption(JsonApiDeserializationFeature.ALLOW_UNKNOWN_TYPE_IN_RELATIONSHIP)
    resourceConverter.setGlobalResolver { url ->
        val request = httpClient.newCall(Request.Builder().url(url).build())
        request.execute().body?.bytes()
    }
    return JSONAPIConverterFactory(resourceConverter)
}

inline fun <reified T> createService(
    httpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    vararg classes: Class<*>,
    baseUrl: String = Kitsu.API_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(createConverterFactory(httpClient, objectMapper, *classes))
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(T::class.java)
}

inline fun <reified T> createService(
    httpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    baseUrl: String = Kitsu.API_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(T::class.java)
}
