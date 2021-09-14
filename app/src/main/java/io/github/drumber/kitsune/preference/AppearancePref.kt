package io.github.drumber.kitsune.preference

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref

object AppearancePref : KotprefModel() {

    override val commitAllPropertiesByDefault = true

    var titles by enumValuePref(TitlesPref.Canoncial)

}

enum class TitlesPref {
    Canoncial, Romanized, English
}
