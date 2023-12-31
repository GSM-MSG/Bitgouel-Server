package team.msg.domain.user.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.msg.domain.user.enums.Authority
import team.msg.domain.user.model.User
import team.msg.domain.user.repository.custom.projection.UserNameProjectionData
import java.util.*

interface CustomUserRepository {
    fun query(keyword: String, authority: Authority, pageable: Pageable): Page<User>
    fun queryNameById(id: UUID): UserNameProjectionData?
}