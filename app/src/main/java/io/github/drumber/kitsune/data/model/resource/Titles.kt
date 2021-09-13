package io.github.drumber.kitsune.data.model.resource

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class Titles(
    @JsonProperty("en") val en: String?,
    @JsonProperty("en_jp") val enJp: String?,
    @JsonProperty("ja_jp") val jaJp: String?
) : Parcelable
