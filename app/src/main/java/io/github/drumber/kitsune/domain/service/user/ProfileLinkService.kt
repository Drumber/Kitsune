package io.github.drumber.kitsune.domain.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLinkSite
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

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
    fun deleteProfileLink(
        @Path("id") id: String
    ): Call<ResponseBody>

}