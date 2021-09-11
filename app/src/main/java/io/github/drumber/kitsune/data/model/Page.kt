package io.github.drumber.kitsune.data.model

import android.net.Uri
import com.github.jasminb.jsonapi.Links

data class Page(val first: Int, val last: Int, val next: Int?, val prev: Int?)

fun Links.toPage() = Page(
    first = parseOffset(this.first.href),
    last = parseOffset(this.last.href),
    next = this.next?.href?.let { parseOffset(it) },
    prev = this.previous?.href?.let { parseOffset(it) }
)

private inline fun parseOffset(href: String) =
    Uri.parse(href).getQueryParameter("page[offset]")!!.toInt()
