package io.github.drumber.kitsune.domain_old.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.profilelinks.ProfileLinkSite
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ProfileLinkService {

    @GET("profile-link-sites")
    suspend fun allProfileLinkSites(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<ProfileLinkSite>>

    @POST("profile-links")
    suspend fun createProfileLink(
        @Body profileLink: ProfileLink
    ): JSONAPIDocument<ProfileLink>

    @PATCH("profile-links/{id}")
    suspend fun updateProfileLink(
        @Path("id") id: String,
        @Body profileLink: ProfileLink
    ): JSONAPIDocument<ProfileLink>

    @DELETE("profile-links/{id}")
    suspend fun deleteProfileLink(
        @Path("id") id: String
    ): Response<Unit>

}