package io.github.drumber.kitsune.ui.adapter

fun interface OnItemClickListener<T> {
    fun onItemClick(item: T)
}