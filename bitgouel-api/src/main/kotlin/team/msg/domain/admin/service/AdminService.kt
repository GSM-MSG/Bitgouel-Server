package team.msg.domain.admin.service

import org.springframework.data.domain.Pageable
import team.msg.domain.admin.presentation.data.request.QueryUsersRequest
import team.msg.domain.user.presentation.data.response.UserDetailsResponse
import team.msg.domain.user.presentation.data.response.UsersResponse
import java.util.UUID

interface AdminService {
    fun queryUsers(request: QueryUsersRequest, pageable: Pageable): UsersResponse
    fun approveUser(userId: UUID)
    fun rejectUser(userId: UUID)
    fun queryUserDetails(userId: UUID): UserDetailsResponse
}