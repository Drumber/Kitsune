package io.github.drumber.kitsune.util

sealed class ResponseData<T>(open val data: T?) {

    data class Success<T>(override val data: T): ResponseData<T>(data)
    data class Error<T>(val e: Exception, override val data: T? = null): ResponseData<T>(data)

}