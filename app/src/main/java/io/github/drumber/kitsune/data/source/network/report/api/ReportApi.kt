package io.github.drumber.kitsune.data.source.network.report.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.report.model.NetworkReport
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface ReportApi {

    @GET("reports")
    suspend fun getReports(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkReport>>

    @POST("reports")
    suspend fun postReport(
        @Body report: JSONAPIDocument<NetworkReport>
    ): JSONAPIDocument<NetworkReport>

}
