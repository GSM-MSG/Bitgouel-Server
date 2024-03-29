package team.msg.domain.admin.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.msg.common.enums.ApproveStatus
import team.msg.common.util.UserUtil
import team.msg.domain.admin.presentation.data.request.QueryUsersRequest
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
    private val userUtil: UserUtil
) : AdminService {
    /**
     * 유저를 전체 조회 및 이름, 역할, 승인 상태들로 조회하는 비즈니스 로직입니다
     * @param 유저를 검색할 조건으로 keyword, authority, approveStatus
     * @return 학생 정보를 리스트로 담은 Dto
     */
    override fun queryUsers(request: QueryUsersRequest): UsersResponse {
        val users = userRepository.query(request.keyword, request.authority, request.approveStatus)

        return UserResponse.listOf(users)
    }

    /**
     * 회원가입 대기 중인 유저를 승인하는 비즈니스 로직입니다
     * @param 승인할 유저들을 검색하기 위한 userIds
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun approveUsers(userIds: List<UUID>) {
        val users = userRepository.findByIdIn(userIds)

        users.forEach {
            if (it.approveStatus == ApproveStatus.APPROVED)
                throw UserAlreadyApprovedException("이미 승인된 유저입니다. Info : [ userId = ${it.id} ]")
        }

        val approvedUsers = users.map {
            User(
                id = it.id,
                email = it.email,
                name = it.name,
                phoneNumber = it.phoneNumber,
                password = it.password,
                authority = it.authority,
                approveStatus = ApproveStatus.APPROVED
            )
        }

        userRepository.saveAll(approvedUsers)
    }

    /**
     * 회원가입 대기 중인 유저를 거절하는 비즈니스 로직입니다
     * @param 거절할 유저들을 검색하기 위한 userIds
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun rejectUsers(userIds: List<UUID>) {
        val users = userRepository.findByIdIn(userIds)

        users.forEach {

            if(it.approveStatus == ApproveStatus.APPROVED)
                throw UserAlreadyApprovedException("이미 승인된 유저입니다. Info : [ userId = ${it.id} ]")

            userUtil.withdrawUser(it)
        }

        userRepository.deleteByIdIn(userIds)
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

    /**
     * 유저를 강제 탈퇴 시키는 비지니스 로직입니다
     * @param 유저를 삭제하기 위한 userIds
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun forceWithdraw(userIds: List<UUID>) {
        val users = userRepository.findByIdIn(userIds)

        users.forEach { userUtil.withdrawUser(it) }

        userRepository.deleteByIdIn(userIds)
    }


    private infix fun UserRepository.findById(id: UUID): User =
        this.findByIdOrNull(id) ?: throw UserNotFoundException("유저를 찾을 수 없습니다. Info [ userId = $id ]")
}