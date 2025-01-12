package io.github.drumber.kitsune.domain.testutils

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.KStubbing

fun <T : Any, R> KStubbing<T>.onSuspend(methodCall: suspend T.() -> R) =
    on { runBlocking { methodCall() } }
