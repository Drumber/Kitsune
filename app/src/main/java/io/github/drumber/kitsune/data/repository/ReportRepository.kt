package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.ReportMapper.toNetworkReportReason
import io.github.drumber.kitsune.data.presentation.model.report.ReportReason
import io.github.drumber.kitsune.data.presentation.model.report.ReportTarget
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.data.source.network.report.ReportNetworkDataSource
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReport
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReportStatus
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

class ReportRepository(
    private val reportNetworkDataSource: ReportNetworkDataSource,
    private val userRepository: UserRepository
) {

    suspend fun hasAlreadyReported(naughtyItemId: String, type: ReportTarget): Boolean {
        val userId = userRepository.localUser.value?.id ?: return false
        val filter = Filter()
            .filter("user_id", userId)
            .filter("naughty_id", naughtyItemId)
            .filter("naughty_type", type.toNaughtyType())
        return reportNetworkDataSource.getReports(filter)?.isNotEmpty() ?: false
    }

    suspend fun submitReport(
        naughtyItemId: String,
        type: ReportTarget,
        reason: ReportReason,
        explanation: String?
    ): Boolean {
        val userId = userRepository.localUser.value?.id ?: return false

        val naughty = when (type) {
            ReportTarget.POST -> NetworkPost(id = naughtyItemId)
            ReportTarget.COMMENT -> NetworkComment(id = naughtyItemId)
            ReportTarget.MEDIA_REACTION -> NetworkMediaReaction(id = naughtyItemId)
        }

        val report = NetworkReport(
            reason = reason.toNetworkReportReason(),
            explanation = explanation,
            status = NetworkReportStatus.REPORTED,
            user = NetworkUser(id = userId),
            naughty = naughty
        )
        return reportNetworkDataSource.postReport(report) != null
    }

    private fun ReportTarget.toNaughtyType() = when (this) {
        ReportTarget.POST -> "Post"
        ReportTarget.COMMENT -> "Comment"
        ReportTarget.MEDIA_REACTION -> "Media-reaction"
    }
}
