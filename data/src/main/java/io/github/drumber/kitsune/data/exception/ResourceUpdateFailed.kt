package io.github.drumber.kitsune.data.exception

class ResourceUpdateFailed : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
}