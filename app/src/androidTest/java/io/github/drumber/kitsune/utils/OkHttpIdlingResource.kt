package io.github.drumber.kitsune.utils

import androidx.test.espresso.IdlingResource
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

class OkHttpIdlingResource(okHttpClient: OkHttpClient) : IdlingResource {

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    private val dispatcher: Dispatcher

    init {
        dispatcher = okHttpClient.dispatcher
        okHttpClient.dispatcher.idleCallback = Runnable { callback?.onTransitionToIdle() }
    }

    override fun getName(): String {
        return OkHttpIdlingResource::class.java.name
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    override fun isIdleNow(): Boolean {
        return dispatcher.runningCallsCount() == 0
    }
}