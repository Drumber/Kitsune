package io.github.drumber.kitsune.preference

import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.model.SearchParams
import io.github.drumber.kitsune.data.model.TitlesPref
import io.github.drumber.kitsune.data.model.category.CategoryPrefWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object KitsunePref : KotprefModel(), KoinComponent {

    override val commitAllPropertiesByDefault = true
    override val kotprefName = context.getString(R.string.preference_file_key)

    var titles by enumValuePref(TitlesPref.Canonical, key = R.string.preference_key_titles)

    var appTheme by enumValuePref(AppTheme.DEFAULT)

    var darkMode by stringPref(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString(), key = R.string.preference_key_dark_mode)


    private var searchParamsJson by stringPref(Defaults.DEFAULT_SEARCH_PARAMS.toJsonString())

    var searchParams: SearchParams
        set(value) {
            searchParamsJson = value.toJsonString()
        }
        get() = searchParamsJson.fromJsonString()


    private var searchQueriesJson by stringPref("[]")

    val searchQueries = SearchQueryData(searchQueriesJson.fromJsonString()) {
        searchQueriesJson = it.toJsonString()
    }


    private var searchCategoriesJson by stringPref("[]")

    var searchCategories: List<CategoryPrefWrapper>
        set(value) {
            searchCategoriesJson = value.toJsonString()
        }
        get() = searchCategoriesJson.fromJsonString()


    private fun Any.toJsonString(): String {
        val objectMapper: ObjectMapper = get()
        return objectMapper.writeValueAsString(this)
    }

    private inline fun <reified T> String.fromJsonString(): T {
        val objectMapper: ObjectMapper = get()
        return objectMapper.readValue(this)
    }

}
