package io.github.drumber.kitsune.data.source.network.report

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.report.api.ReportApi
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportNetworkDataSource(private val reportApi: ReportApi) {

    suspend fun getReports(filter: Filter): List<NetworkReport>? {
        return withContext(Dispatchers.IO) {
            reportApi.getReports(filter.options).get()
        }
    }

    suspend fun postReport(report: NetworkReport): NetworkReport? {
        return withContext(Dispatchers.IO) {
            reportApi.postReport(JSONAPIDocument(report)).get()
        }
    }
}
