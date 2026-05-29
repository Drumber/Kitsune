package io.github.drumber.kitsune.data.source.network

import android.net.Uri
import com.github.jasminb.jsonapi.JSONAPIDocument

/**
 * Holds the data of a single page that is paginated using a cursor instead of an offset.
 * This is used by the Kitsu activity feed endpoints (powered by GetStream), which only
 * provide a `next` cursor link for forward pagination.
 */
data class CursorPageData<Value : Any>(
    val data: List<Value>?,
    val next: String?
)

fun <Value : Any> JSONAPIDocument<List<Value>>.toCursorPageData() = CursorPageData(
    data = get(),
    next = links?.next?.href?.parseCursor()
)

private fun String.parseCursor() =
    Uri.parse(this).getQueryParameter("page[cursor]")
