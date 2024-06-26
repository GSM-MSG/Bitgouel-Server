package team.msg.domain.club.exception.constant

enum class ClubErrorCode(
    val status: Int
) {
    CLUB_NOT_FOUND(404),
    ALREADY_EXIST(409)
}