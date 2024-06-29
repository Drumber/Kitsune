package io.github.drumber.kitsune.domain_old.model.common.library

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @param orderId   Unique order ID for storing the status in the library entries database and
 *                  to receive the entries in the here defined order.
 */
enum class LibraryStatus(val orderId: Int) {
    @JsonProperty("current")
    Current(0),

    @JsonProperty("planned")
    Planned(1),

    @JsonProperty("completed")
    Completed(2),

    @JsonProperty("on_hold")
    OnHold(3),

    @JsonProperty("dropped")
    Dropped(4)
}