package team.msg.domain.user.presentation.data.response

import org.springframework.data.domain.Page
import team.msg.common.enums.ApproveStatus
import team.msg.domain.user.enums.Authority
import team.msg.domain.user.model.User
import java.util.*

data class UserResponse (
    val id: UUID,
    val name: String,
    val authority: Authority
) {
    companion object {
        fun pageOf(user: User, organization: String) = UserPageResponse(
            name = user.name,
            email = user.email,
            phoneNumber = user.phoneNumber,
            authority = user.authority,
            organization = organization
        )

        fun of(user: User) = AdminUserResponse(
            id = user.id,
            name = user.name,
            authority = user.authority,
            approveStatus = user.approveStatus
        )

        fun pageOf(users: Page<User>) = UsersResponse(
            users.map {
                of(it)
            }
        )

        fun detailOf(user: User) = UserDetailsResponse(
            id = user.id,
            name = user.name,
            authority = user.authority,
            approveStatus = user.approveStatus
        )
    }
}

data class UserPageResponse (
    val name: String,
    val email: String,
    val phoneNumber: String,
    val authority: Authority,
    val organization: String
)

data class AdminUserResponse(
    val id: UUID,
    val name: String,
    val authority: Authority,
    val approveStatus: ApproveStatus
)

data class UsersResponse(
    val users: Page<AdminUserResponse>
)

data class UserDetailsResponse(
    val id: UUID,
    val name: String,
    val authority: Authority,
    val approveStatus: ApproveStatus
)