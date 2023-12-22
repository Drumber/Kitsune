package io.github.drumber.kitsune.testutils

import android.util.Log
import org.mockito.Mockito
import org.mockito.kotlin.any

inline fun <T, R> T.useMockedAndroidLogger(block: T.() -> R): R = Mockito.mockStatic(Log::class.java).use {
    it.`when`<Int> { Log.v(any(), any()) }.thenReturn(0)
    it.`when`<Int> { Log.v(any(), any(), any()) }.thenReturn(0)

    it.`when`<Int> { Log.d(any(), any()) }.thenReturn(0)
    it.`when`<Int> { Log.d(any(), any(), any()) }.thenReturn(0)

    it.`when`<Int> { Log.i(any(), any()) }.thenReturn(0)
    it.`when`<Int> { Log.i(any(), any(), any()) }.thenReturn(0)

    it.`when`<Int> { Log.w(any(), any<String>()) }.thenReturn(0)
    it.`when`<Int> { Log.w(any(), any(), any()) }.thenReturn(0)

    it.`when`<Int> { Log.e(any(), any()) }.thenReturn(0)
    it.`when`<Int> { Log.e(any(), any(), any()) }.thenReturn(0)

    block()
}
