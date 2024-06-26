package team.msg.domain.club.presentation.data.request

import team.msg.common.enums.Field

data class CreateClubWebRequest(
    val clubName: String,
    val field: Field
)