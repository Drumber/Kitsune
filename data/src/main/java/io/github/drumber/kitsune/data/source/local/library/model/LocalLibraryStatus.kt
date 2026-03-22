package io.github.drumber.kitsune.data.source.local.library.model

enum class LocalLibraryStatus(val orderId: Int) {
    Current(0),
    Planned(1),
    Completed(2),
    OnHold(3),
    Dropped(4)
}