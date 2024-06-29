package io.github.drumber.kitsune.data.mapper

internal inline fun <reified T> T?.require(): T {
    return this ?: throw MappingException("Required value is null")
}

class MappingException(message: String) : IllegalArgumentException(message)
