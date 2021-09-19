package io.github.drumber.kitsune.util

sealed class ResponseData<T> {

    data class Success<T>(val data: T): ResponseData<T>()
    data class Error<T>(val e: Exception): ResponseData<T>()

}