package io.github.drumber.kitsune.preference

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.model.ResourceSelector
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object KitsunePref : KotprefModel(), KoinComponent {

    override val commitAllPropertiesByDefault = true

    var titles by enumValuePref(TitlesPref.Canoncial)

    private var searchFilterJson by stringPref(Defaults.DEFAULT_RESOURCE_SELECTOR.toJsonString())

    var searchFilter: ResourceSelector
        set(value) {
            searchFilterJson = value.toJsonString()
        }
        get() = searchFilterJson.fromJsonString(ResourceSelector::class.java)


    private inline fun Any.toJsonString(): String {
        val objectMapper: ObjectMapper = get()
        return objectMapper.writeValueAsString(this)
    }

    private inline fun <T> String.fromJsonString(type: Class<T>): T {
        val objectMapper: ObjectMapper = get()
        return objectMapper.readValue(this, type)
    }

}

enum class TitlesPref {
    Canoncial, Romanized, English
}
