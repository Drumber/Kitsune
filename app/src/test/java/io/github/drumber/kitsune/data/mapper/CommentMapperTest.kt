package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.CommentMapper.toComment
import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUpload
import io.github.drumber.kitsune.testutils.networkImage
import io.github.drumber.kitsune.testutils.networkUser
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class CommentMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkComment_to_Comment() {
        // given
        val user = networkUser(faker)
        val firstUpload = NetworkUpload(id = "u1", content = networkImage(faker), uploadOrder = 1)
        val secondUpload = NetworkUpload(id = "u2", content = networkImage(faker), uploadOrder = 0)
        val networkComment = NetworkComment(
            id = "7",
            content = "content",
            contentFormatted = "formatted",
            createdAt = "2024-01-01T00:00:00.000Z",
            likesCount = 3,
            repliesCount = 2,
            user = user,
            uploads = listOf(firstUpload, secondUpload)
        )

        // when
        val comment = networkComment.toComment(isLikedByMe = true, myLikeId = "like-1")

        // then
        assertThat(comment.id).isEqualTo("7")
        assertThat(comment.content).isEqualTo("content")
        assertThat(comment.contentFormatted).isEqualTo("formatted")
        assertThat(comment.createdAt).isEqualTo("2024-01-01T00:00:00.000Z")
        assertThat(comment.likesCount).isEqualTo(3)
        assertThat(comment.repliesCount).isEqualTo(2)
        assertThat(comment.isLikedByMe).isTrue()
        assertThat(comment.myLikeId).isEqualTo("like-1")
        assertThat(comment.authorId).isEqualTo(user.id)
        assertThat(comment.authorName).isEqualTo(user.name)
        assertThat(comment.authorAvatarUrl).isEqualTo(user.avatar?.toImage()?.largeOrDown())
        // imageUrl uses the first upload ordered by uploadOrder ascending
        assertThat(comment.imageUrl).isEqualTo(secondUpload.content?.toImage()?.largeOrDown())
    }

    @Test
    fun shouldApplyDefaults_whenNullableFieldsAreNull() {
        // given
        val networkComment = NetworkComment(id = "1")

        // when
        val comment = networkComment.toComment()

        // then
        assertThat(comment.likesCount).isEqualTo(0)
        assertThat(comment.repliesCount).isEqualTo(0)
        assertThat(comment.isLikedByMe).isFalse()
        assertThat(comment.myLikeId).isNull()
        assertThat(comment.authorId).isNull()
        assertThat(comment.imageUrl).isNull()
        assertThat(comment.embed).isNull()
    }

    @Test
    fun shouldThrow_whenIdIsNull() {
        // given
        val networkComment = NetworkComment(id = null)

        // expect
        assertThatThrownBy { networkComment.toComment() }
            .isInstanceOf(MappingException::class.java)
    }
}
