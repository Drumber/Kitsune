package io.github.drumber.kitsune.utils

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException
import kotlin.time.Duration

class WaitForView(
    @IdRes private val viewId: Int,
    private val timeout: Duration
) : ViewAction {

    override fun getDescription(): String {
        return "wait up to ${timeout.inWholeMilliseconds} milliseconds to find a view with ID $viewId"
    }

    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()

        val endTime = System.currentTimeMillis() + timeout.inWholeMilliseconds

        do {
            for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                if (withId(viewId).matches(child))
                    return
            }
            uiController.loopMainThreadForAtLeast(500)
        } while (System.currentTimeMillis() < endTime)

        throw PerformException.Builder()
            .withActionDescription(description)
            .withCause(TimeoutException("Waited $timeout milliseconds"))
            .withViewDescription(HumanReadables.describe(view))
            .build()
    }
}

fun waitForView(@IdRes viewId: Int, timeout: Duration) = WaitForView(viewId, timeout)
