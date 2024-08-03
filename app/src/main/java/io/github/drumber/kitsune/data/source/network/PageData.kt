package io.github.drumber.kitsune.data.source.network

import android.net.Uri
import com.github.jasminb.jsonapi.JSONAPIDocument

data class PageData<Value : Any>(
    val data: List<Value>?,
    val first: Int?,
    val last: Int?,
    val prev: Int?,
    val next: Int?
)

fun <Value : Any> JSONAPIDocument<List<Value>>.toPageData() = PageData(
    data = get(),
    first = links?.first?.href?.parseOffset(),
    last = links?.last?.href?.parseOffset(),
    next = links?.next?.href?.parseOffset(),
    prev = links?.previous?.href?.parseOffset()
)

private fun String.parseOffset() =
    Uri.parse(this).getQueryParameter("page[offset]")?.toInt()
