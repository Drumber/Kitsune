package io.github.drumber.kitsune.ui.adapter

import android.view.View

fun interface OnItemClickListener<T> {
    fun onItemClick(view: View, item: T)
}