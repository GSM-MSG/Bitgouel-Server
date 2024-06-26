package team.msg.domain.club.exception

import team.msg.domain.club.exception.constant.ClubErrorCode
import team.msg.global.error.exception.BitgouelException

class AlreadyExistClubException(
    message: String
) : BitgouelException(message,ClubErrorCode.ALREADY_EXIST.status)