package io.github.drumber.kitsune.domain.model.infrastructure.media

typealias Titles = Map<String, String?>

val Titles.en get() = get("en")
val Titles.enJp get() = get("en_jp")
val Titles.jaJp get() = get("ja_jp")
