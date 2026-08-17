package io.github.drumber.kitsune.data.source.network.report.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkFeedSubject
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("reports")
data class NetworkReport(
    @Id
    val id: String? = null,
    val explanation: String? = null,
    val reason: NetworkReportReason? = null,
    val status: NetworkReportStatus? = null,

    @Relationship("user")
    val user: NetworkUser? = null,
    @Relationship("naughty")
    val naughty: NetworkFeedSubject? = null
)

// https://github.com/hummingbird-me/kitsu-server/blob/the-future/app/models/report.rb#L34
enum class NetworkReportReason {
    @JsonProperty("nsfw")
    NSFW,
    @JsonProperty("offensive")
    OFFENSIVE,
    @JsonProperty("spoiler")
    SPOILER,
    @JsonProperty("bullying")
    BULLYING,
    @JsonProperty("other")
    OTHER,
    @JsonProperty("spam")
    SPAM
}

enum class NetworkReportStatus {
    @JsonProperty("reported")
    REPORTED,
    @JsonProperty("resolved")
    RESOLVED,
    @JsonProperty("declined")
    DECLINED
}
