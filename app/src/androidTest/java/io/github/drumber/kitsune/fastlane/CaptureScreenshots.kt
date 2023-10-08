package io.github.drumber.kitsune.fastlane

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
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
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.utils.OkHttpIdlingResource
import io.github.drumber.kitsune.utils.waitForView
import okhttp3.OkHttpClient
import org.junit.AfterClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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

    companion object {
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

    @Test
    fun testTakeScreenshot() {
        enterDemoMode()

        var idlingResource: OkHttpIdlingResource? = null
        activityRule.scenario.onActivity {
            val client: OkHttpClient = get()
            idlingResource = OkHttpIdlingResource(client)
        }
        IdlingRegistry.getInstance().register(idlingResource!!)

        // Light Mode
        KitsunePref.darkMode = AppCompatDelegate.MODE_NIGHT_NO.toString()
        takeHomeScreenshots("light")
        takeSearchScreenshots("light")
        takeDetailsScreenshot("light")

        // Dark Mode
        UiThreadStatement.runOnUiThread {
            KitsunePref.darkMode = AppCompatDelegate.MODE_NIGHT_YES.toString()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        takeHomeScreenshots("dark")
        takeSearchScreenshots("dark")
        takeDetailsScreenshot("dark")

        // Purple theme with Dark Mode
        UiThreadStatement.runOnUiThread {
            KitsunePref.appTheme = AppTheme.PURPLE
        }
        takeHomeScreenshots("dark_purple")

        // Purple theme with Light Mode
        UiThreadStatement.runOnUiThread {
            KitsunePref.darkMode = AppCompatDelegate.MODE_NIGHT_NO.toString()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        takeHomeScreenshots("light_purple")

        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    private fun takeHomeScreenshots(prefix: String) {
        onView(withId(R.id.main_fragment)).perform(click())

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(1000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        Screengrab.screenshot("${prefix}_home_screen")
    }

    private fun takeSearchScreenshots(prefix: String) {
        onView(withId(R.id.search_fragment)).perform(click())

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        Screengrab.screenshot("${prefix}_search_screen")
    }

    private fun takeDetailsScreenshot(prefix: String) {
        activityRule.scenario.onActivity { activity ->
            val navController = activity.findNavController(R.id.nav_host_fragment)
            navController.navigate(Uri.parse("https://kitsu.io/anime/12"))
        }

        onView(isRoot()).perform(waitForView(R.id.tv_description, 30.seconds))
        Thread.sleep(1000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        Screengrab.screenshot("${prefix}_details_screen")

        onView(withId(R.id.chart_ratings)).perform(scrollTo())
        onView(withId(R.id.nsv_content)).perform(swipeUp())
        Thread.sleep(100) // wait for scroll
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        Screengrab.screenshot("${prefix}_details_ratings_screen")
        pressBack()
    }
}