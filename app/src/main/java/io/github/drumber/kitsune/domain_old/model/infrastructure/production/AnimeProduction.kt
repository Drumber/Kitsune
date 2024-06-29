package io.github.drumber.kitsune.domain_old.model.infrastructure.production

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("animeProductions")
data class AnimeProduction(
    @Id
    val id: String?,
    val role: AnimeProductionRole?,

    @Relationship("producer")
    val producer: Producer?
) : Parcelable

enum class AnimeProductionRole {
    @JsonProperty("licensor")
    Licensor,
    @JsonProperty("producer")
    Producer,
    @JsonProperty("studio")
    Studio
}
