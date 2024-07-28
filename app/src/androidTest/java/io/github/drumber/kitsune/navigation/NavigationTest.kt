package io.github.drumber.kitsune.navigation

import android.net.Uri
import androidx.navigation.findNavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.paging.CharacterPagingAdapter.CharacterViewHolder
import io.github.drumber.kitsune.ui.adapter.paging.MediaUnitPagingAdapter.MediaUnitViewHolder
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.utils.OkHttpIdlingResource
import io.github.drumber.kitsune.utils.actionOnChild
import io.github.drumber.kitsune.utils.searchText
import io.github.drumber.kitsune.utils.waitForView
import okhttp3.OkHttpClient
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class NavigationTest : KoinComponent {

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    var runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private var idlingResource: OkHttpIdlingResource? = null

    @Before
    fun setup() {
        activityRule.scenario.onActivity {
            val client: OkHttpClient = get()
            idlingResource = OkHttpIdlingResource(client)
        }
        IdlingRegistry.getInstance().register(idlingResource!!)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun shouldNavigateToDestinationsFromHome() {
        onView(withId(R.id.main_fragment)).perform(click())

        Thread.sleep(1000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // navigate to trending section
        onView(
            allOf(
                withChild(withText(R.string.section_trending)),
                withId(R.id.header)
            )
        ).perform(click())

        Thread.sleep(1000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // click first media item
        onView(withId(R.id.rv_media)).perform(actionOnItemAtPosition<MediaViewHolder>(0, click()))
    }

    @Test
    fun shouldNavigateToSearchFragment() {
        onView(withId(R.id.search_fragment)).perform(click())

        Thread.sleep(3000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // perform search
        onView(withId(R.id.search_view)).perform(searchText("toradora"))
        Thread.sleep(1000)

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(isRoot()).perform(waitForView(R.id.rv_media, 10.seconds))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // click first media item
        onView(withId(R.id.rv_media)).perform(actionOnItemAtPosition<MediaViewHolder>(0, click()))
    }

    @Test
    fun shouldNavigateToLibraryFragment() {
        onView(withId(R.id.library_fragment)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    fun shouldNavigateToProfileFragmentAndSettings() {
        onView(withId(R.id.profile_fragment)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // open settings
        onView(withId(R.id.menu_settings)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // navigate to appearance
        onView(withText(R.string.nav_appearance)).perform(click())
    }

    @Test
    fun shouldNavigateToDetailsAndSubPages() {
        activityRule.scenario.onActivity { activity ->
            val navController = activity.findNavController(R.id.nav_host_fragment)
            navController.navigate(Uri.parse("https://kitsu.io/anime/12"))
        }

        onView(isRoot()).perform(waitForView(R.id.tv_description, 30.seconds))
        Thread.sleep(3000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(3000)

        // navigate to episodes
        onView(withId(R.id.btn_media_units)).perform(scrollTo())
        Thread.sleep(100)
        onView(withId(R.id.btn_media_units)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // click on first episode
        onView(withId(R.id.rv_media)).perform(
            actionOnItemAtPosition<MediaUnitViewHolder>(
                0,
                click()
            )
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // go back to details fragment
        onView(isRoot()).perform(pressBack())
        onView(isRoot()).perform(pressBack())

        // navigate to characters
        onView(withId(R.id.btn_characters)).perform(scrollTo())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.btn_characters)).perform(click())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // click on first character
        onView(withId(R.id.rv_media)).perform(
            actionOnItemAtPosition<CharacterViewHolder>(
                1,
                actionOnChild(withId(R.id.iv_character), click())
            )
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // go back to details fragment
        onView(isRoot()).perform(pressBack())
        onView(isRoot()).perform(pressBack())
    }
}