package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.ReportMapper.toNetworkReportReason
import io.github.drumber.kitsune.data.presentation.model.report.ReportReason
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.report.ReportNetworkDataSource
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReport
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReportStatus
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

class ReportRepository(
    private val reportNetworkDataSource: ReportNetworkDataSource,
    private val userRepository: UserRepository
) {

    suspend fun hasAlreadyReported(postId: String): Boolean {
        val userId = userRepository.localUser.value?.id ?: return false
        val filter = Filter()
            .filter("user_id", userId)
            .filter("naughty_id", postId)
            .filter("naughty_type", "Post")
        return reportNetworkDataSource.getReports(filter)?.isNotEmpty() ?: false
    }

    suspend fun submitReport(
        postId: String,
        reason: ReportReason,
        explanation: String?
    ): Boolean {
        val userId = userRepository.localUser.value?.id ?: return false
        val report = NetworkReport(
            reason = reason.toNetworkReportReason(),
            explanation = explanation,
            status = NetworkReportStatus.REPORTED,
            user = NetworkUser(id = userId),
            naughty = NetworkPost(id = postId)
        )
        return reportNetworkDataSource.postReport(report) != null
    }
}
