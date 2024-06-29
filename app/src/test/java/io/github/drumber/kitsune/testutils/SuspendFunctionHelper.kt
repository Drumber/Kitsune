package io.github.drumber.kitsune.testutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.mockito.kotlin.KStubbing

fun <T : Any, R> KStubbing<T>.onSuspend(methodCall: suspend T.() -> R) =
    on { runBlocking { methodCall() } }

fun CoroutineScope.assertThatThrownBy(shouldRaiseThrowable: suspend () -> Unit) = Assertions.assertThatThrownBy {
    launch { shouldRaiseThrowable() }
}
