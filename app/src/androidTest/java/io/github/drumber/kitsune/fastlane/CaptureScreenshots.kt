package io.github.drumber.kitsune.fastlane

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.di.ImagesHttpClient
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.utils.OkHttpIdlingResource
import io.github.drumber.kitsune.utils.filter.RequiresScreenshotMode
import io.github.drumber.kitsune.utils.waitForView
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
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CaptureScreenshots : KoinComponent {

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

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

        val idlingResource = mutableListOf<OkHttpIdlingResource>()
        activityRule.scenario.onActivity {
            val client: OkHttpClient = get()
            val imageClient: OkHttpClient = get(named<ImagesHttpClient>())
            idlingResource.add(OkHttpIdlingResource(client))
            idlingResource.add(OkHttpIdlingResource(imageClient))
        }
        IdlingRegistry.getInstance().register(*idlingResource.toTypedArray())

        // wait for initial data load on first launch
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)

        // Light Mode
        KitsunePref.darkMode = AppCompatDelegate.MODE_NIGHT_NO.toString()
        screenshotsConfig.filter { !it.isDarkMode }.forEach { config ->
            UiThreadStatement.runOnUiThread {
                KitsunePref.appTheme = config.appTheme
            }

            captureScreenshots(config)
        }

        // Dark Mode
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

        IdlingRegistry.getInstance().unregister(*idlingResource.toTypedArray())
        idlingResource.clear()
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
        onView(withId(R.id.main_fragment)).perform(click())

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(1000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        Screengrab.screenshot("${prefix}_0_home_screen")
    }

    private fun takeSearchScreenshots(prefix: String) {
        onView(withId(R.id.main_fragment)).perform(click())
        onView(withId(R.id.search_bar)).perform(click())

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)

        onView(withId(R.id.btn_search)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        Screengrab.screenshot("${prefix}_3_search_screen")

        pressBack()
    }

    private fun takeDetailsScreenshot(prefix: String, targets: Set<ScreenshotTarget>) {
        activityRule.scenario.onActivity { activity ->
            val navController = activity.findNavController(R.id.nav_host_fragment)
            navController.navigate(Uri.parse("${Kitsu.BASE_URL}/anime/12"))
        }

        onView(isRoot()).perform(waitForView(R.id.tv_description, 30.seconds))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        if (ScreenshotTarget.DETAILS_SCREEN in targets) {
            Screengrab.screenshot("${prefix}_1_details_screen")
        }

        Thread.sleep(3000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        onView(withId(R.id.layout_ratings)).perform(scrollTo())
        onView(withId(R.id.nsv_content)).perform(swipeUp())
        Thread.sleep(100) // wait for scroll
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        if (ScreenshotTarget.DETAILS_RATINGS_SCREEN in targets) {
            Screengrab.screenshot("${prefix}_2_details_ratings_screen")
        }

        pressBack()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        activityRule.scenario.onActivity { activity ->
            val navHostFragment = activity.supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.childFragmentManager.executePendingTransactions()
        }
        Thread.sleep(300)
    }
}
