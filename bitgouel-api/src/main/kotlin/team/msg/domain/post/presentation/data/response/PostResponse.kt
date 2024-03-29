package team.msg.domain.post.presentation.data.response

import org.springframework.data.domain.Page
import team.msg.domain.post.enums.FeedType
import team.msg.domain.post.model.Post
import team.msg.domain.post.repository.custom.projection.PostProjection
import java.time.LocalDateTime
import java.util.*

data class PostResponse(
    val id: UUID,
    val title: String,
    val modifiedAt: LocalDateTime,
    val postSequence: Int
) {
    companion object {
        fun of(post: Post) =
            PostResponse(
                id = post.id,
                title = post.title,
                modifiedAt = post.modifiedAt,
                postSequence = post.postSequence
            )

        fun of(postProjection: PostProjection) =
            PostResponse(
                id = postProjection.id,
                title = postProjection.title,
                modifiedAt = postProjection.modifiedAt,
                postSequence = postProjection.postSequence
            )

        fun listOf(postProjections: List<PostProjection>) =
            PostsResponse(
                postProjections.map {
                    of(it)
                }
            )

        fun pageOf(posts: Page<Post>) =
            PagingPostsResponse(
                posts.map {
                    of(it)
                }
            )

        fun detailOf(post: Post, writer: String, isWriter: Boolean) =
            PostDetailsResponse(
                title = post.title,
                writer = writer,
                writtenBy = isWriter,
                content = post.content,
                feedType = post.feedType,
                modifiedAt = post.modifiedAt,
                links = post.links
            )
    }
}

data class PostsResponse(
    val posts: List<PostResponse>
)

data class PagingPostsResponse(
    val posts: Page<PostResponse>
)

data class PostDetailsResponse(
    val title: String,
    val writer: String,
    val writtenBy: Boolean,
    val content: String,
    val feedType: FeedType,
    val modifiedAt: LocalDateTime,
    val links: List<String>
)