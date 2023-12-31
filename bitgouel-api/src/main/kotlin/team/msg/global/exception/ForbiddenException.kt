package team.msg.global.exception

import team.msg.global.error.GlobalErrorCode
import team.msg.global.error.exception.BitgouelException

class ForbiddenException(
    message: String
) : BitgouelException(message, GlobalErrorCode.FORBIDDEN.status)