package io.github.drumber.kitsune.data.common.model

typealias Titles = Map<String, String?>

val Titles.en get() = get("en")
val Titles.enUs get() = get("en_us")
val Titles.enJp get() = get("en_jp")
val Titles.jaJp get() = get("ja_jp")

private val commonTitleKeys = listOf("en", "en_jp", "ja_jp")
fun Titles.withoutCommonTitles() =
    filterKeys { !commonTitleKeys.contains(it) }
