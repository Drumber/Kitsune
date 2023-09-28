package io.github.drumber.kitsune.preference

import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumOrdinalPref
import com.chibatching.kotpref.enumpref.enumValuePref
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.domain.model.FilterCollection
import io.github.drumber.kitsune.domain.model.SearchParams
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.user.TitleLanguagePreference
import io.github.drumber.kitsune.domain.model.preference.CategoryPrefWrapper
import io.github.drumber.kitsune.domain.model.preference.StartPagePref
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryKind
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.util.logE
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import kotlin.reflect.KProperty

object KitsunePref : KotprefModel(), KoinComponent {

    override val commitAllPropertiesByDefault = true
    override val kotprefName = context.getString(R.string.preference_file_key)

    private val userRepository: UserRepository by inject()

    private var titlesIntern by enumValuePref(
        TitleLanguagePreference.Canonical,
        key = R.string.preference_key_titles
    )

    var titles: TitleLanguagePreference
        set(value) {
            titlesIntern = value
        }
        get() {
            return userRepository.user?.titleLanguagePreference ?: titlesIntern
        }

    var appTheme by enumValuePref(AppTheme.DEFAULT)

    var useDynamicColorTheme by booleanPref(
        false,
        key = R.string.preference_key_dynamic_color_theme
    )

    var darkMode by stringPref(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString(),
        key = R.string.preference_key_dark_mode
    )

    var mediaItemSize by enumOrdinalPref(MediaItemSize.LARGE)

    var startFragment by enumValuePref(StartPagePref.Home)

    var rememberSearchFilters by booleanPref(
        true,
        key = R.string.preference_key_remember_search_filters
    )

    var checkForUpdatesOnStart by booleanPref(
        true,
        key = R.string.preference_key_check_for_updates_on_start
    )


    private var searchParamsJson by stringPref(Defaults.DEFAULT_SEARCH_PARAMS.toJsonString())

    var searchParams: SearchParams
        set(value) {
            searchParamsJson = value.toJsonString()
        }
        get() = ::searchParamsJson.fromJsonString(Defaults.DEFAULT_SEARCH_PARAMS)


    private var searchFiltersJson by stringPref("{}")

    var searchFilters: FilterCollection
        set(value) {
            searchFiltersJson = value.toJsonString()
        }
        get() = ::searchFiltersJson.fromJsonString(FilterCollection())


    private var searchCategoriesJson by stringPref("[]")

    var searchCategories: List<CategoryPrefWrapper>
        set(value) {
            searchCategoriesJson = value.toJsonString()
        }
        get() = ::searchCategoriesJson.fromJsonString(emptyList())


    var libraryEntryKind by enumValuePref(LibraryEntryKind.All)

    private var libraryEntryStatusJson by stringPref("[]")

    var libraryEntryStatus: List<LibraryStatus>
        set(value) {
            libraryEntryStatusJson = value.toJsonString()
        }
        get() = ::libraryEntryStatusJson.fromJsonString(emptyList())


    var flagUserDeniedNotificationPermission by booleanPref(false)


    private fun Any.toJsonString(): String {
        val objectMapper: ObjectMapper = get()
        return objectMapper.writeValueAsString(this)
    }

    private inline fun <reified T> KProperty<*>.fromJsonString(defaultValue: T): T {
        val value = preferences.getString(getPrefKey(this), null) ?: return defaultValue
        val objectMapper: ObjectMapper = get()
        return try {
            objectMapper.readValue(value)
        } catch (e: JsonProcessingException) {
            logE("Failed to parse object from JSON. Returning default value.", e)
            remove(this) // reset preference
            defaultValue
        }
    }

}
