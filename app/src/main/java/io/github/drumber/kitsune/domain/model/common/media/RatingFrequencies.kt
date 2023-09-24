package io.github.drumber.kitsune.domain.model.common.media

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingFrequencies(
    @JsonProperty("2") val r2: String?,
    @JsonProperty("3") val r3: String?,
    @JsonProperty("4") val r4: String?,
    @JsonProperty("5") val r5: String?,
    @JsonProperty("6") val r6: String?,
    @JsonProperty("7") val r7: String?,
    @JsonProperty("8") val r8: String?,
    @JsonProperty("9") val r9: String?,
    @JsonProperty("10") val r10: String?,
    @JsonProperty("11") val r11: String?,
    @JsonProperty("12") val r12: String?,
    @JsonProperty("13") val r13: String?,
    @JsonProperty("14") val r14: String?,
    @JsonProperty("15") val r15: String?,
    @JsonProperty("16") val r16: String?,
    @JsonProperty("17") val r17: String?,
    @JsonProperty("18") val r18: String?,
    @JsonProperty("19") val r19: String?,
    @JsonProperty("20") val r20: String?,
) : Parcelable
