package io.github.drumber.kitsune.data.model

import android.net.Uri
import android.os.Parcelable
import com.github.jasminb.jsonapi.Links
import kotlinx.parcelize.Parcelize

@Parcelize
data class Page(val first: Int, val last: Int, val next: Int?, val prev: Int?) : Parcelable

fun Links.toPage() = Page(
    first = parseOffset(this.first.href),
    last = parseOffset(this.last.href),
    next = this.next?.href?.let { parseOffset(it) },
    prev = this.previous?.href?.let { parseOffset(it) }
)

private inline fun parseOffset(href: String) =
    Uri.parse(href).getQueryParameter("page[offset]")!!.toInt()
