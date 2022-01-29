package io.github.drumber.kitsune.data.model.media

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Titles(
    @JsonProperty("en")
    @SerialName("en")
    val en: String? = null,
    @JsonProperty("en_jp")
    @SerialName("en_jp")
    val enJp: String? = null,
    @JsonProperty("ja_jp")
    @SerialName("ja_jp")
    val jaJp: String? = null
) : Parcelable
