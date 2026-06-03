package io.github.drumber.kitsune.data.source.network.group.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroup
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupCategory
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupMember
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface GroupsApi {

    @GET("groups")
    suspend fun getGroups(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkGroup>>

    @GET("groups/{id}")
    suspend fun getGroup(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkGroup>

    @GET("group-categories")
    suspend fun getGroupCategories(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkGroupCategory>>

    @GET("group-members")
    suspend fun getGroupMembers(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkGroupMember>>

}
