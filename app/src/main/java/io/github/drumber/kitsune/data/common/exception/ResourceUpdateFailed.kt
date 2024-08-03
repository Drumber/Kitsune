package io.github.drumber.kitsune.data.common.exception

class ResourceUpdateFailed : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
}