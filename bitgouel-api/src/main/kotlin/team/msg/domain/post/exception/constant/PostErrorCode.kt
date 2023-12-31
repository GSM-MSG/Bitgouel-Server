package team.msg.domain.post.exception.constant

enum class PostErrorCode(
    val status: Int
){
    FORBIDDEN_POST(403),
    POST_NOT_FOUND(404)
}