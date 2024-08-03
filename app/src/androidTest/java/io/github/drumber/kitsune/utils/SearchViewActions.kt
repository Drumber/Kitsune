package io.github.drumber.kitsune.utils

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf

fun searchText(query: String) = object : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return allOf(
            isDisplayed(),
            isAssignableFrom(SearchView::class.java)
        )
    }

    override fun getDescription() = "Type text into a SearchView"

    override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
        val searchView = view as SearchView
        searchView.setQuery(query, true)
    }
}