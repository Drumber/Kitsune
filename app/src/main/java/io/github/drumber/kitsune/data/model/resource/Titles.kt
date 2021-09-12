package io.github.drumber.kitsune.data.model.resource

import com.fasterxml.jackson.annotation.JsonProperty

data class Titles(
    @JsonProperty("en") val en: String?,
    @JsonProperty("en_jp") val enJp: String?,
    @JsonProperty("ja_jp") val jaJp: String?
)
