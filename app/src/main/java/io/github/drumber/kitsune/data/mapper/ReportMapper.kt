package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.report.ReportReason
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReportReason

object ReportMapper {
    fun ReportReason.toNetworkReportReason() = when (this) {
        ReportReason.NSFW -> NetworkReportReason.NSFW
        ReportReason.OFFENSIVE -> NetworkReportReason.OFFENSIVE
        ReportReason.SPOILER -> NetworkReportReason.SPOILER
        ReportReason.BULLYING -> NetworkReportReason.BULLYING
        ReportReason.OTHER -> NetworkReportReason.OTHER
        ReportReason.SPAM -> NetworkReportReason.SPAM
    }
}
