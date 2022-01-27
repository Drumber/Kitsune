package io.github.drumber.kitsune.data.model.production

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("producers")
data class Producer(
    @Id val id: String?,
    val slug: String?,
    val name: String?
) : Parcelable
