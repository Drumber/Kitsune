package io.github.drumber.kitsune.testutils

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.ThrowableAssert
import org.mockito.kotlin.KStubbing
import kotlin.test.assertFails

fun <T : Any, R> KStubbing<T>.onSuspend(methodCall: suspend T.() -> R) =
    on { runBlocking { methodCall() } }

suspend fun assertThatThrownBy(shouldRaiseThrowable: suspend () -> Unit): AbstractThrowableAssert<*, out Throwable> {
    val throwable = assertFails { shouldRaiseThrowable() }
    return CustomThrowableAssert(throwable).hasBeenThrown()
}

class CustomThrowableAssert<T : Throwable>(actual: T?) : ThrowableAssert<T>(actual) {
    public override fun hasBeenThrown(): ThrowableAssert<T> {
        return super.hasBeenThrown()
    }
}
