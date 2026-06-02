package io.github.drumber.kitsune.data.source.network.feed.model

/**
 * Marker interface for the polymorphic `subject`/`target` relationship of a feed [NetworkActivity].
 * The subject can be a [NetworkPost] (a new post), a comment on a post (in which case the post being
 * commented on is referenced via the comment's `post` relationship), or a media reaction (for
 * reaction-like notifications).
 */
interface NetworkFeedSubject
