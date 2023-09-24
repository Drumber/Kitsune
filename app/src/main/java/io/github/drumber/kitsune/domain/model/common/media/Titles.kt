package io.github.drumber.kitsune.domain.model.common.media

typealias Titles = Map<String, String?>

val Titles.en get() = get("en")
val Titles.enJp get() = get("en_jp")
val Titles.jaJp get() = get("ja_jp")

private val commonTitleKeys = listOf("en", "en_jp", "ja_jp")
fun Titles.withoutCommonTitles() =
    filterKeys { !commonTitleKeys.contains(it) }
