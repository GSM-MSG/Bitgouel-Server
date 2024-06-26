package team.msg.domain.club.presentation.data.request

import team.msg.common.enums.Field

class CreateClubRequest(
    val clubName: String,
    val field: Field
)