package io.github.drumber.kitsune.util.network

import android.os.LocaleList
import okhttp3.Interceptor
import okhttp3.Response

class AcceptLanguageHeaderInterceptor : Interceptor {

    // use default system languages for now
    private val acceptLanguage = LocaleList.getDefault().toLanguageTags()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept-Language", acceptLanguage)
            .build()
        return chain.proceed(request)
    }
}