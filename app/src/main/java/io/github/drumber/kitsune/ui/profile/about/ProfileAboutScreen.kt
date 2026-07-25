package io.github.drumber.kitsune.ui.profile.about

import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStats
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MediaItemCard
import io.github.drumber.kitsune.ui.profile.UserProfileUiState
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId

@Composable
fun ProfileAboutScreen(
    user: User?,
    isRefreshing: Boolean,
    isInitialLoading: Boolean,
    followState: UserProfileUiState? = null,
    onRefresh: () -> Unit,
    onFollowClick: () -> Unit = {},
    onFollowingClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onWaifuClick: (Character) -> Unit,
    onMediaClick: (Media) -> Unit,
    onCharacterClick: (Character) -> Unit,
    onProfileLinkClick: (ProfileLink) -> Unit,
    modifier: Modifier = Modifier
) {
    KitsunePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        if (user == null) return@KitsunePullToRefreshBox
        ProfileAboutContent(
            user = user,
            isInitialLoading = isInitialLoading,
            followState = followState,
            onFollowClick = onFollowClick,
            onFollowingClick = onFollowingClick,
            onFollowersClick = onFollowersClick,
            onWaifuClick = onWaifuClick,
            onMediaClick = onMediaClick,
            onCharacterClick = onCharacterClick,
            onProfileLinkClick = onProfileLinkClick
        )
    }
}

@Composable
private fun ProfileAboutContent(
    user: User,
    isInitialLoading: Boolean,
    followState: UserProfileUiState?,
    onFollowClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onWaifuClick: (Character) -> Unit,
    onMediaClick: (Media) -> Unit,
    onCharacterClick: (Character) -> Unit,
    onProfileLinkClick: (ProfileLink) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        if (!user.title.isNullOrBlank()) {
                item(key = "title_badge") { UserTitleBadge(title = user.title!!) }
            }
            item(key = "follow_counts") {
                FollowCountsRow(
                    followingCount = user.followingCount ?: 0,
                    followersCount = user.followersCount ?: 0,
                    onFollowingClick = onFollowingClick,
                    onFollowersClick = onFollowersClick
                )
            }
            if (followState?.canFollow == true) {
                item(key = "follow_button") {
                    FollowButton(
                        isFollowing = followState.isFollowing,
                        isProcessing = followState.isFollowProcessing,
                        onClick = onFollowClick
                    )
                }
            }
            item(key = "about_card") {
                AboutMeCard(
                    user = user,
                    onWaifuClick = onWaifuClick,
                    modifier = Modifier.padding(10.dp)
                )
            }
            val profileLinks = user.profileLinks.orEmpty()
            if (profileLinks.isNotEmpty()) {
                item(key = "profile_links") {
                    ProfileLinksChips(
                        profileLinks = profileLinks,
                        onProfileLinkClick = onProfileLinkClick,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
            item(key = "stats_card") {
                StatsCard(
                    stats = user.stats,
                    isLoading = isInitialLoading,
                    modifier = Modifier.padding(10.dp)
                )
            }
            val favorites = user.favorites.orEmpty()
            val favAnime = favorites.filter { it.item is Anime }.map { it.item as Anime }
            val favManga = favorites.filter { it.item is Manga }.map { it.item as Manga }
            val favChars = favorites.filter { it.item is Character }.map { it.item as Character }
            if (favAnime.isNotEmpty()) {
                item(key = "fav_anime") {
                    FavoriteMediaRow(
                        title = stringResource(R.string.title_favorite_anime),
                        items = favAnime,
                        onItemClick = onMediaClick
                    )
                }
            }
            if (favManga.isNotEmpty()) {
                item(key = "fav_manga") {
                    FavoriteMediaRow(
                        title = stringResource(R.string.title_favorite_manga),
                        items = favManga,
                        onItemClick = onMediaClick
                    )
                }
            }
            if (favChars.isNotEmpty()) {
                item(key = "fav_characters") {
                    FavoriteCharactersRow(
                        title = stringResource(R.string.title_favorite_characters),
                        characters = favChars,
                        onCharacterClick = onCharacterClick
                    )
                }
            }
        }
    }

@Composable
private fun UserTitleBadge(title: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FollowCountsRow(
    followingCount: Int,
    followersCount: Int,
    onFollowingClick: () -> Unit,
    onFollowersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(horizontal = 10.dp)) {
        TextButton(onClick = onFollowingClick) {
            Text(stringResource(R.string.profile_data_following, followingCount))
        }
        TextButton(onClick = onFollowersClick) {
            Text(stringResource(R.string.profile_data_followers, followersCount))
        }
    }
}

@Composable
private fun FollowButton(
    isFollowing: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isProcessing,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            if (isFollowing) {
                stringResource(R.string.action_unfollow)
            } else {
                stringResource(R.string.action_follow)
            }
        )
    }
}

@Composable
private fun AboutMeCard(
    user: User,
    onWaifuClick: (Character) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.title_about_me),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            if (!user.about.isNullOrBlank()) {
                Text(
                    text = user.about,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            ProfileInfoRow(
                iconRes = R.drawable.ic_person_24,
                label = stringResource(R.string.profile_data_gender),
                value = DataUtil.getGenderString(user.gender, context)
                    ?: stringResource(R.string.profile_data_private)
            )
            ProfileInfoRow(
                iconRes = R.drawable.ic_location_24,
                label = stringResource(R.string.profile_data_location),
                value = user.location?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.profile_data_private)
            )
            ProfileInfoRow(
                iconRes = R.drawable.ic_cake_24,
                label = stringResource(R.string.profile_data_birthday),
                value = DataUtil.formatDate(user.birthday)
                    ?: stringResource(R.string.profile_data_private)
            )
            ProfileInfoRow(
                iconRes = R.drawable.ic_calendar_24,
                label = stringResource(R.string.profile_data_join_date),
                value = DataUtil.formatUserJoinDate(user.createdAt, context)
                    ?: stringResource(R.string.profile_data_private)
            )
            if (user.waifu != null) {
                WaifuRow(
                    character = user.waifu,
                    waifuOrHusbando = user.waifuOrHusbando,
                    onWaifuClick = onWaifuClick
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WaifuRow(
    character: Character,
    waifuOrHusbando: String?,
    onWaifuClick: (Character) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onWaifuClick(character) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            imageUrl = character.image?.originalOrDown(),
            size = 24.dp,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = waifuOrHusbando.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = character.name.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileLinksChips(
    profileLinks: List<ProfileLink>,
    onProfileLinkClick: (ProfileLink) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(profileLinks.sortedBy { it.profileLinkSite?.id?.toIntOrNull() }) { profileLink ->
            val siteName = profileLink.profileLinkSite?.name
            SuggestionChip(
                onClick = { onProfileLinkClick(profileLink) },
                label = { Text(siteName.orEmpty()) },
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(getProfileSiteLogoResourceId(siteName)),
                        contentDescription = null,
                        modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                    )
                }
            )
        }
    }
}

@Composable
private fun StatsCard(
    stats: List<UserStats>?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val statsSectionRef = remember { arrayOfNulls<ProfileStatsSection>(1) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        AndroidView(
            factory = { ctx ->
                val tabLayout = TabLayout(ctx)
                val viewPager = ViewPager2(ctx).apply {
                    (getChildAt(0) as? androidx.recyclerview.widget.RecyclerView)
                        ?.isNestedScrollingEnabled = false
                }
                val section = ProfileStatsSection(viewPager, tabLayout)
                section.init(true)
                statsSectionRef[0] = section
                LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(tabLayout, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                    addView(viewPager, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                }
            },
            update = {
                statsSectionRef[0]?.submitStats(stats, false)
                statsSectionRef[0]?.setLoading(isLoading)
            }
        )
    }
}

@Composable
private fun FavoriteMediaRow(
    title: String,
    items: List<Media>,
    onItemClick: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { media ->
                MediaItemCard(
                    imageUrl = media.posterImageUrl,
                    title = media.title,
                    modifier = Modifier.size(width = 106.dp, height = 150.dp),
                    onClick = { onItemClick(media) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteCharactersRow(
    title: String,
    characters: List<Character>,
    onCharacterClick: (Character) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(characters, key = { it.id }) { character ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { onCharacterClick(character) }
                ) {
                    Avatar(
                        imageUrl = character.image?.originalOrDown(),
                        size = 72.dp,
                        contentDescription = character.name
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = character.name.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
