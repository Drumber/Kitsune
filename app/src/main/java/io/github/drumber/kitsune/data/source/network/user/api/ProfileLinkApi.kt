package io.github.drumber.kitsune.data.source.network.user.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.user.model.profilelinks.NetworkProfileLink
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ProfileLinkApi {

    @GET("profile-link-sites")
    suspend fun getAllProfileLinkSites(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkProfileLink>>

    @POST("profile-links")
    suspend fun createProfileLink(
        @Body profileLink: NetworkProfileLink
    ): JSONAPIDocument<NetworkProfileLink>

    @PATCH("profile-links/{id}")
    suspend fun updateProfileLink(
        @Path("id") id: String,
        @Body profileLink: NetworkProfileLink
    ): JSONAPIDocument<NetworkProfileLink>

    @DELETE("profile-links/{id}")
    suspend fun deleteProfileLink(
        @Path("id") id: String
    ): Response<Unit>

}