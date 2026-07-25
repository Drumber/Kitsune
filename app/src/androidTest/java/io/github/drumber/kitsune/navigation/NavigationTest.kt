package io.github.drumber.kitsune.navigation

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.IntentAction.SHORTCUT_SETTINGS
import io.github.drumber.kitsune.ui.KitsuneTestTags
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.utils.OkHttpIdlingResource
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@RunWith(AndroidJUnit4::class)
class NavigationTest : KoinComponent {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private var idlingResource: OkHttpIdlingResource? = null

    @Before
    fun setup() {
        composeTestRule.activityRule.scenario.onActivity {
            val client: OkHttpClient = get()
            idlingResource = OkHttpIdlingResource(client)
        }
        idlingResource?.let { IdlingRegistry.getInstance().register(it) }
    }

    @After
    fun tearDown() {
        idlingResource?.let { IdlingRegistry.getInstance().unregister(it) }
        idlingResource = null
    }

    @Test
    fun shouldNavigateToTopLevelDestinations() {
        waitForTag(KitsuneTestTags.HomeSearchBar)

        clickTopLevel(R.string.nav_feed)
        waitForText(R.string.feed_tab_global)

        clickTopLevel(R.string.nav_library)
        waitForAnyText(R.string.library_not_logged_in_title, R.string.library_status_watching)

        clickTopLevel(R.string.nav_profile)
        waitForAnyText(R.string.not_logged_in, R.string.profile_tab_about)

        clickTopLevel(R.string.nav_home)
        waitForTag(KitsuneTestTags.HomeSearchBar)
    }

    @Test
    fun shouldNavigateFromHomeToDetails() {
        waitForTag(KitsuneTestTags.HomeSearchBar)
        waitForTag(KitsuneTestTags.ExploreSectionHeader)

        composeTestRule.onAllNodesWithTag(KitsuneTestTags.ExploreSectionHeader, useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForTag(KitsuneTestTags.MediaCard, SEARCH_TIMEOUT_MS)
        composeTestRule.onAllNodesWithTag(KitsuneTestTags.MediaCard, useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForTag(KitsuneTestTags.DetailsDescription, DETAILS_TIMEOUT_MS)
    }

    @Test
    fun shouldNavigateToSearchAndFilters() {
        waitForTag(KitsuneTestTags.HomeSearchBar)
        composeTestRule.onNodeWithTag(KitsuneTestTags.HomeSearchBar, useUnmergedTree = true).performClick()

        waitForTag(KitsuneTestTags.SearchInput)
        composeTestRule.onNodeWithTag(KitsuneTestTags.SearchInput, useUnmergedTree = true)
            .performClick()
            .performTextInput("toradora")

        waitForOptionalNode(hasTestTag(KitsuneTestTags.SearchResults), SEARCH_TIMEOUT_MS)
        if (waitForOptionalNode(hasTestTag(KitsuneTestTags.MediaCard), SEARCH_TIMEOUT_MS)) {
            composeTestRule.onAllNodesWithTag(KitsuneTestTags.MediaCard, useUnmergedTree = true)
                .onFirst()
                .performClick()
            waitForTag(KitsuneTestTags.DetailsDescription, DETAILS_TIMEOUT_MS)
            pressBack()
            waitForTag(KitsuneTestTags.SearchInput)
        }

        composeTestRule.onNode(hasContentDescription(text(R.string.title_filter)), useUnmergedTree = true)
            .performClick()
        waitForText(R.string.title_filter)
        waitForAnyText(
            R.string.title_categories,
            R.string.search_provider_error,
            R.string.search_provider_not_available
        )
    }

    @Test
    fun shouldNavigateToDetailsSubPages() {
        openDeepLink("${Kitsu.BASE_URL}/anime/12")
        waitForTag(KitsuneTestTags.DetailsDescription, DETAILS_TIMEOUT_MS)

        composeTestRule.onNodeWithTag(KitsuneTestTags.DetailsEpisodesButton, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        waitForAnyText(R.string.title_episodes, R.string.title_chapters)
        waitForTag(KitsuneTestTags.EpisodesList)
        pressBack()
        waitForTag(KitsuneTestTags.DetailsDescription, DETAILS_TIMEOUT_MS)

        composeTestRule.onNodeWithTag(KitsuneTestTags.DetailsCharactersButton, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        waitForText(R.string.title_characters)
        waitForTag(KitsuneTestTags.CharactersList)
    }

    @Test
    fun shouldNavigateToSettingsAndAppearance() {
        openSettingsShortcut()
        waitForText(R.string.nav_settings)

        composeTestRule.onNode(hasText(text(R.string.nav_appearance)) and hasClickAction(), useUnmergedTree = true)
            .performClick()
        waitForText(R.string.nav_appearance)
        waitForText(R.string.preference_app_theme)

        pressBack()
        pressBack()
    }

    private fun clickTopLevel(@StringRes labelRes: Int) {
        composeTestRule.onNode(hasText(text(labelRes)) and hasClickAction(), useUnmergedTree = true)
            .performClick()
    }

    private fun openDeepLink(url: String) {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage(activity.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
            )
        }
    }

    private fun openSettingsShortcut() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.startActivity(
                Intent(activity, MainActivity::class.java).apply {
                    action = SHORTCUT_SETTINGS
                }
            )
        }
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        waitForNode(hasTestTag(tag), timeoutMillis)
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun waitForText(@StringRes resId: Int, timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        waitForNode(hasText(text(resId)), timeoutMillis)
        composeTestRule.onNode(hasText(text(resId)), useUnmergedTree = true).assertIsDisplayed()
    }

    private fun waitForAnyText(@StringRes vararg resIds: Int, timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        val matchers = resIds.map { hasText(text(it)) }
        composeTestRule.waitUntil(timeoutMillis) {
            matchers.any { matcher -> hasNode(matcher) }
        }
    }

    private fun waitForNode(matcher: SemanticsMatcher, timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        composeTestRule.waitUntil(timeoutMillis) { hasNode(matcher) }
    }

    private fun waitForOptionalNode(matcher: SemanticsMatcher, timeoutMillis: Long): Boolean {
        return runCatching {
            composeTestRule.waitUntil(timeoutMillis) { hasNode(matcher) }
        }.isSuccess
    }

    private fun hasNode(matcher: SemanticsMatcher): Boolean {
        return composeTestRule.onAllNodes(matcher, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    private fun text(@StringRes resId: Int): String = composeTestRule.activity.getString(resId)

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 15_000L
        const val SEARCH_TIMEOUT_MS = 30_000L
        const val DETAILS_TIMEOUT_MS = 45_000L
    }
}
