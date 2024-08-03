package io.github.drumber.kitsune.data.utils

import androidx.paging.PagingSource
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Modified version of [androidx.paging.InvalidatingPagingSourceFactory] accepting a custom input property.
 */
class InvalidatingPagingSourceFactory<Key : Any, Value : Any, Input: Any>(
    private val pagingSourceFactory: (input: Input) -> PagingSource<Key, Value>
) {
    private val lock = ReentrantLock()

    private var pagingSources: List<PagingSource<Key, Value>> = emptyList()

    fun createPagingSource(input: Input): PagingSource<Key, Value> {
        return pagingSourceFactory(input).also {
            lock.withLock {
                pagingSources = pagingSources + it
            }
        }
    }

    fun invalidate() {
        val previousList = lock.withLock {
            pagingSources.also {
                pagingSources = emptyList()
            }
        }

        for (pagingSource in previousList) {
            if (!pagingSource.invalid) {
                pagingSource.invalidate()
            }
        }
    }
}