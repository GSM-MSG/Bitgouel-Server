package bitgouel.team.msg.domain.auth.exception

import bitgouel.team.msg.domain.user.exception.constant.UserErrorCode
import bitgouel.team.msg.global.error.exception.BitgouelException

class AlreadySignUpException(
    message: String
) : BitgouelException(message, UserErrorCode.ALREADY_SIGN_UP.status)