package io.github.drumber.kitsune.util.json

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class IgnoreParcelablePropertyMixin {
    @JsonIgnore
    abstract fun getStability(): Int
}