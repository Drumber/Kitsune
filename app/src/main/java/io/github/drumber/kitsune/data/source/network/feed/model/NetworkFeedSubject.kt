package io.github.drumber.kitsune.data.source.network.feed.model

/**
 * Marker interface for the polymorphic `subject` relationship of a feed [NetworkActivity]. The
 * subject can either be a [NetworkPost] (a new post) or a comment on a post, in which case the
 * post being commented on is referenced via the comment's `post` relationship.
 */
interface NetworkFeedSubject
