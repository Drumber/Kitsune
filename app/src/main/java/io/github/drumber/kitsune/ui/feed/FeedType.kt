package io.github.drumber.kitsune.ui.feed

enum class FeedType {
    /** The global feed, shared with everyone. */
    GLOBAL,

    /** The personal feed showing posts from people the logged-in user follows. */
    FOLLOWING,

    /** A single user's profile feed: their own posts plus posts made on their wall. */
    USER
}
