package team.msg.domain.post.presentation.data.request

data class UpdatePostRequest(
    val title: String,
    val content: String,
    val links: List<String>
)