package io.github.drumber.kitsune.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import org.hamcrest.core.AllOf.allOf

fun actionOnChild(matcher: Matcher<View>, action: ViewAction) = object : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return allOf(isDisplayed(), matcher)
    }

    override fun getDescription(): String = "Performing action on child"

    override fun perform(uiController: UiController, view: View) {
        val results = TreeIterables.breadthFirstViewTraversal(view).filter { matcher.matches(it) }

        if (results.isEmpty()) {
            throw RuntimeException("No view found with matcher ${StringDescription.asString(matcher)} in the hierarchy of $view")
        } else if (results.size > 1) {
            throw RuntimeException("Multiple views found with matcher ${StringDescription.asString(matcher)} in the hierarchy of $view")
        }

        action.perform(uiController, results.first())
    }
}