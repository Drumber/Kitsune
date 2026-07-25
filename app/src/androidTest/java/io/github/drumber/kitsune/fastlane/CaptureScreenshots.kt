package io.github.drumber.kitsune.fastlane

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.KitsuneTestTags
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.utils.OkHttpIdlingResource
import io.github.drumber.kitsune.utils.filter.RequiresScreenshotMode
import okhttp3.OkHttpClient
import org.junit.AfterClass
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar

@RunWith(AndroidJUnit4::class)
class CaptureScreenshots : KoinComponent {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    var runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.DUMP
    )

    private val screenshotsConfig = captureScreenshotsConfig

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            assumeTrue(BuildConfig.SCREENSHOT_MODE_ENABLED)
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            CleanStatusBar.disable()
        }

        fun enterDemoMode() {
            CleanStatusBar()
                .setNetworkFullyConnected(true)
                .setShowNotifications(false)
                .setMobileNetworkLevel(4)
                .setClock("1200")
                .enable()
        }
    }

    @RequiresScreenshotMode
    @Test
    fun testTakeScreenshot() {
        enterDemoMode()

        val idlingResources = mutableListOf<OkHttpIdlingResource>()
        composeTestRule.activityRule.scenario.onActivity {
            val client: OkHttpClient = get()
            val imageClient: OkHttpClient = get(named("images"))
            idlingResources += OkHttpIdlingResource(client)
            idlingResources += OkHttpIdlingResource(imageClient)
        }
        IdlingRegistry.getInstance().register(*idlingResources.toTypedArray())

        waitForTag(KitsuneTestTags.HomeSearchBar)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        KitsunePref.darkMode = AppCompatDelegate.MODE_NIGHT_NO.toString()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        screenshotsConfig.filter { !it.isDarkMode }.forEach { config ->
            UiThreadStatement.runOnUiThread {
                KitsunePref.appTheme = config.appTheme
            }

            captureScreenshots(config)
        }

        UiThreadStatement.runOnUiThread {
            KitsunePref.darkMode = AppCompatDelegate.MODE_NIGHT_YES.toString()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        screenshotsConfig.filter { it.isDarkMode }.forEach { config ->
            UiThreadStatement.runOnUiThread {
                KitsunePref.appTheme = config.appTheme
            }

            captureScreenshots(config)
        }

        IdlingRegistry.getInstance().unregister(*idlingResources.toTypedArray())
        idlingResources.clear()
    }

    private fun captureScreenshots(config: CaptureConfig) {
        if (ScreenshotTarget.HOME_SCREEN in config.targets) {
            takeHomeScreenshots(config.name)
        }
        if (ScreenshotTarget.SEARCH_SCREEN in config.targets) {
            takeSearchScreenshots(config.name)
        }
        if (ScreenshotTarget.DETAILS_SCREEN in config.targets || ScreenshotTarget.DETAILS_RATINGS_SCREEN in config.targets) {
            takeDetailsScreenshot(config.name, config.targets)
        }
    }

    private fun takeHomeScreenshots(prefix: String) {
        waitForTag(KitsuneTestTags.HomeSearchBar)
        waitForOptionalNode(hasTestTag(KitsuneTestTags.MediaCard), 15_000L)
        Screengrab.screenshot("${prefix}_0_home_screen")
    }

    private fun takeSearchScreenshots(prefix: String) {
        waitForTag(KitsuneTestTags.HomeSearchBar)
        composeTestRule.onNodeWithTag(KitsuneTestTags.HomeSearchBar, useUnmergedTree = true).performClick()
        waitForTag(KitsuneTestTags.SearchInput)
        waitForOptionalNode(hasTestTag(KitsuneTestTags.MediaCard), 15_000L)

        Screengrab.screenshot("${prefix}_3_search_screen")

        pressBack()
        waitForTag(KitsuneTestTags.HomeSearchBar)
    }

    private fun takeDetailsScreenshot(prefix: String, targets: Set<ScreenshotTarget>) {
        openDeepLink("${Kitsu.BASE_URL}/anime/12")
        waitForTag(KitsuneTestTags.DetailsDescription, 45_000L)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        if (ScreenshotTarget.DETAILS_SCREEN in targets) {
            Screengrab.screenshot("${prefix}_1_details_screen")
        }

        composeTestRule.onNodeWithTag(KitsuneTestTags.DetailsContent, useUnmergedTree = true)
            .performTouchInput { swipeUp() }
        waitForOptionalNode(hasTestTag(KitsuneTestTags.DetailsCharactersButton), 5_000L)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        if (ScreenshotTarget.DETAILS_RATINGS_SCREEN in targets) {
            Screengrab.screenshot("${prefix}_2_details_ratings_screen")
        }

        pressBack()
        waitForTag(KitsuneTestTags.HomeSearchBar)
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

    private fun waitForTag(tag: String, timeoutMillis: Long = 15_000L) {
        waitForNode(hasTestTag(tag), timeoutMillis)
    }

    private fun waitForNode(matcher: SemanticsMatcher, timeoutMillis: Long) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodes(matcher, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForOptionalNode(matcher: SemanticsMatcher, timeoutMillis: Long): Boolean {
        return runCatching { waitForNode(matcher, timeoutMillis) }.isSuccess
    }
}
