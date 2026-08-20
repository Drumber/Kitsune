package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.filter
import androidx.paging.map
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.GroupMapper.toGroup
import io.github.drumber.kitsune.data.mapper.GroupMapper.toGroupCategory
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.presentation.model.group.GroupCategory
import io.github.drumber.kitsune.data.source.network.group.FollowedGroupsPagingDataSource
import io.github.drumber.kitsune.data.source.network.group.GroupsNetworkDataSource
import io.github.drumber.kitsune.data.source.network.group.GroupsPagingDataSource
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroup
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupMember
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import kotlinx.coroutines.flow.map

class GroupsRepository(
    private val groupsNetworkDataSource: GroupsNetworkDataSource
) {

    /**
     * Pager for the groups list, optionally filtered by a search [query] and/or a [categoryId],
     * sorted by the given [sort] attribute (defaults to the most popular groups first).
     */
    fun groupsPager(
        query: String? = null,
        categoryId: String? = null,
        sort: String = "-membersCount",
        pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            GroupsPagingDataSource(
                groupsNetworkDataSource,
                buildFilter(query, categoryId, sort, pageSize)
            )
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toGroup() }
    }

    /**
     * Pager for the groups the [userId] is a member of ("following"), optionally filtered by a
     * search [query] on the group name.
     */
    fun followedGroupsPager(
        userId: String,
        query: String? = null,
        pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            FollowedGroupsPagingDataSource(
                groupsNetworkDataSource,
                buildFollowedFilter(userId, query, pageSize)
            )
        }
    ).flow.map { pagingData ->
        pagingData.filter { it.group != null }.map { it.group!!.toGroup() }
    }

    /** Fetches a single group by id, including its category. */
    suspend fun getGroup(groupId: String): Group? {
        val filter = Filter()
            .include("category")
        return groupsNetworkDataSource.getGroup(groupId, filter)?.toGroup()
    }

    /** Fetches the list of group categories used to filter the groups list. */
    suspend fun getCategories(): List<GroupCategory> {
        val filter = Filter()
            .sort("name")
            .pageLimit(20)
        return groupsNetworkDataSource.getGroupCategories(filter)
            .map { it.toGroupCategory() }
    }

    /**
     * Returns the id of the membership record where [userId] is a member of the group with
     * [groupId], or `null` if the user is not a member.
     */
    suspend fun getMembershipId(groupId: String, userId: String): String? {
        val filter = Filter()
            .filter("group", groupId)
            .filter("user", userId)
            .fields("groupMembers", "id")
            .pageLimit(1)
        return groupsNetworkDataSource.getGroupMembersList(filter)?.firstOrNull()?.id
    }

    /**
     * Joins the group with [groupId] as the user with [userId] and returns the created membership
     * id, or `null` on failure.
     */
    suspend fun joinGroup(groupId: String, userId: String): String? {
        val member = NetworkGroupMember(
            id = null,
            group = NetworkGroup(id = groupId),
            user = NetworkUser(id = userId)
        )
        return groupsNetworkDataSource.createGroupMember(member)?.id
    }

    /** Leaves the group by deleting the membership record with [membershipId]. */
    suspend fun leaveGroup(membershipId: String): Boolean {
        return groupsNetworkDataSource.deleteGroupMember(membershipId)
    }

    private fun buildFilter(
        query: String?,
        categoryId: String?,
        sort: String,
        pageSize: Int
    ) = Filter()
        .include("category")
        .pageLimit(pageSize)
        .apply {
            if (!query.isNullOrBlank()) filter("query", query)
            else sort(sort)
            if (!categoryId.isNullOrBlank()) filter("category", categoryId)
        }

    private fun buildFollowedFilter(
        userId: String,
        query: String?,
        pageSize: Int
    ) = Filter()
        .filter("user", userId)
        .include("group", "group.category")
        .pageLimit(pageSize)
        .apply {
            if (!query.isNullOrBlank()) filter("query", query)
            else sort("-createdAt")
        }

}
