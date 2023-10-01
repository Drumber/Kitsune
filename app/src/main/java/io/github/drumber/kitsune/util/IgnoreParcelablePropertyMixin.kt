package io.github.drumber.kitsune.util

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class IgnoreParcelablePropertyMixin {
    @JsonIgnore
    abstract fun getStability(): Int
}