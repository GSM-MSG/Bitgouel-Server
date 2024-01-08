package team.msg.domain.admin.presentation

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.msg.domain.admin.mapper.AdminMapper
import team.msg.domain.admin.presentation.data.web.QueryUsersWebRequest
import team.msg.domain.admin.service.AdminService
import team.msg.domain.user.presentation.data.response.UserDetailsResponse
import team.msg.domain.user.presentation.data.response.UsersResponse
import java.util.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService,
    private val adminMapper: AdminMapper
) {
    @GetMapping
    fun queryUser(webRequest: QueryUsersWebRequest, pageable: Pageable): ResponseEntity<UsersResponse> {
        val request = adminMapper.queryUsersWebRequestToDto(webRequest)
        val response = adminService.queryUsers(request, pageable)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{user_id}")
    fun approveUser(@PathVariable("user_id") userId: UUID): ResponseEntity<Void> {
        adminService.approveUser(userId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{user_id}")
    fun rejectUser(@PathVariable("user_id") userId: UUID): ResponseEntity<Void> {
        adminService.rejectUser(userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{user_id}")
    fun queryUserDetails(@PathVariable("user_id") userId: UUID): ResponseEntity<UserDetailsResponse> {
        val response = adminService.queryUserDetails(userId)
        return ResponseEntity.ok(response)
    }
}