package team.msg.domain.admin.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.msg.common.enums.ApproveStatus
import team.msg.domain.admin.presentation.data.request.QueryUsersRequest
import team.msg.domain.user.event.WithdrawUserEvent
import team.msg.domain.user.exception.UserAlreadyApprovedException
import team.msg.domain.user.exception.UserNotFoundException
import team.msg.domain.user.model.User
import team.msg.domain.user.presentation.data.response.UserDetailsResponse
import team.msg.domain.user.presentation.data.response.UserResponse
import team.msg.domain.user.presentation.data.response.UsersResponse
import team.msg.domain.user.repository.UserRepository
import java.util.*

@Service
class AdminServiceImpl(
    private val userRepository: UserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) : AdminService {
    /**
     * 유저를 전체 조회 및 이름으로 조회하는 비즈니스 로직입니다
     * @param 유저를 검색하기 위한 keyword 및 페이징을 처리하기 위한 pageable
     * @return 페이징된 학생 정보를 담은 Dto
     */
    override fun queryUsers(request: QueryUsersRequest, pageable: Pageable): UsersResponse {
        val users = userRepository.query(request.keyword, request.authority, pageable)

        return UserResponse.pageOf(users)
    }

    /**
     * 회원가입 대기 중인 유저를 승인하는 비즈니스 로직입니다
     * @param 승인할 유저를 검색하기 위한 userId
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun approveUser(userId: UUID) {
        val user = userRepository findById userId

        if(user.approveStatus == ApproveStatus.APPROVED)
            throw UserAlreadyApprovedException("이미 승인된 유저입니다. Info : [ userId = ${user.id} ]")

        val approvedUser = user.run {
            User(
                id = id,
                email = email,
                name = name,
                phoneNumber = phoneNumber,
                password = password,
                authority = authority,
                approveStatus = ApproveStatus.APPROVED
            )
        }

        userRepository.save(approvedUser)
    }

    /**
     * 회원가입 대기 중인 유저를 거절하는 비즈니스 로직입니다
     * @param 거절할 유저를 검색하기 위한 userId
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun rejectUser(userId: UUID) {
        val user = userRepository findById userId

        if(user.approveStatus == ApproveStatus.APPROVED)
            throw UserAlreadyApprovedException("이미 승인된 유저입니다. Info : [ userId = ${user.id} ]")

        applicationEventPublisher.publishEvent(WithdrawUserEvent(user))

        userRepository.delete(user)
    }

    /**
     * 유저의 상세 정보를 조회하는 비즈니스 로직입니다
     * @param 유저를 조회하기 위한 userId
     * @return 조회한 user의 정보를 담은 dto
     */
    @Transactional(readOnly = true)
    override fun queryUserDetails(userId: UUID): UserDetailsResponse {
        val user = userRepository findById userId

        return UserResponse.detailOf(user)
    }


    private infix fun UserRepository.findById(id: UUID): User =
        this.findByIdOrNull(id) ?: throw UserNotFoundException("유저를 찾을 수 없습니다. Info [ userId = $id ]")
}