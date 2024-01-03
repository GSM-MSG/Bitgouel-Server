package team.msg.domain.lecture.service

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.msg.common.enums.ApproveStatus
import team.msg.common.util.UserUtil
import team.msg.domain.lecture.enums.LectureStatus
import team.msg.domain.lecture.enums.LectureType
import team.msg.domain.lecture.exception.AlreadyApprovedLectureException
import team.msg.domain.lecture.exception.AlreadySignedUpLectureException
import team.msg.domain.lecture.exception.InvalidLectureTypeException
import team.msg.domain.lecture.exception.LectureNotFoundException
import team.msg.domain.lecture.exception.NotAvailableSignUpDateException
import team.msg.domain.lecture.exception.OverMaxRegisteredUserException
import team.msg.domain.lecture.exception.UnApprovedLectureException
import team.msg.domain.lecture.model.Lecture
import team.msg.domain.lecture.model.RegisteredLecture
import team.msg.domain.lecture.presentation.data.request.CreateLectureRequest
import team.msg.domain.lecture.presentation.data.request.QueryAllLectureRequest
import team.msg.domain.lecture.presentation.data.response.LecturesResponse
import team.msg.domain.lecture.presentation.data.response.LectureDetailsResponse
import team.msg.domain.lecture.presentation.data.response.LectureResponse
import team.msg.domain.lecture.repository.LectureRepository
import team.msg.domain.lecture.repository.RegisteredLectureRepository
import team.msg.domain.student.exception.StudentNotFoundException
import team.msg.domain.student.model.Student
import team.msg.domain.student.repository.StudentRepository
import team.msg.domain.user.enums.Authority
import team.msg.domain.user.model.User
import java.time.LocalDateTime
import java.util.*

@Service
class LectureServiceImpl(
    private val lectureRepository: LectureRepository,
    private val registeredLectureRepository: RegisteredLectureRepository,
    private val studentRepository: StudentRepository,
    private val userUtil: UserUtil
) : LectureService{

    /**
     * 강의 개설을 처리하는 비지니스 로직입니다.
     * @param 생성할 강의의 데이터를 담은 request Dto
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun createLecture(request: CreateLectureRequest) {
        val user = userUtil.queryCurrentUser()

        val credit = when(request.lectureType){
            LectureType.MUTUAL_CREDIT_RECOGNITION_PROGRAM   -> request.credit
            LectureType.UNIVERSITY_EXPLORATION_PROGRAM      -> 0
            else -> throw InvalidLectureTypeException("유효하지 않은 강의 구분입니다. info : [ type = ${request.lectureType} ]")
        }

        val lecture = Lecture(
            id = UUID.randomUUID(),
            user = user,
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate,
            completeDate = request.completeDate,
            content = request.content,
            lectureType = request.lectureType,
            credit = credit,
            instructor = user.name,
            maxRegisteredUser = request.maxRegisteredUser
        )

        lectureRepository.save(lecture)
    }

    /**
     * 강의를 조회하는 비지니스 로직입니다.
     * @param 조회할 강의의 승인 상태를 담은 request dto와 pageable dto
     * @return 조회한 강의의 정보를 담은 list dto
     */
    @Transactional(readOnly = true)
    override fun queryAllLectures(pageable: Pageable, queryAllLectureRequest: QueryAllLectureRequest): LecturesResponse {
        val user = userUtil.queryCurrentUser()

        val approveStatus = queryAllLectureRequest.approveStatus
        val lectureType = queryAllLectureRequest.lectureType

        val lectures = when(user.authority) {
            Authority.ROLE_ADMIN -> lectureRepository.findAllByApproveStatusAndLectureType(pageable, approveStatus, lectureType)
            else -> lectureRepository.findAllByApproveStatusAndLectureType(pageable, ApproveStatus.APPROVED, lectureType)
        }

        val response = LecturesResponse(
            lectures.map {
                val headCount = registeredLectureRepository.countByLecture(it)
                LectureResponse.of(it,headCount)
            }
        )

        return response
    }

    /**
     * 강의에 대한 상세정보를 조회하는 비지니스 로직입니다.
     * @param 상세 조회할 강의의 id
     * @return 강의의 상세조회 정보를 담은 detail dto
     */
    @Transactional(readOnly = true)
    override fun queryLectureDetails(id: UUID): LectureDetailsResponse {
        val user = userUtil.queryCurrentUser()

        val lecture = lectureRepository findById id

        val headCount = registeredLectureRepository.countByLecture(lecture)

        val isRegistered = if(user.authority == Authority.ROLE_STUDENT) {
            val student = studentRepository findByUser user
            registeredLectureRepository.existsOne(student.id, lecture.id)
        } else false

        val response = LectureResponse.detailOf(lecture, headCount, isRegistered)

        return response
    }

    /**
     * 강의에 대해 수강신청하는 비지니스 로직입니다.
     * @param 수강신청을 하기 위한 강의 id
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun signUpLecture(id: UUID) {
        val user = userUtil.queryCurrentUser()

        val student = studentRepository findByUser user

        val lecture = lectureRepository findById id

        if(lecture.approveStatus == ApproveStatus.PENDING)
            throw UnApprovedLectureException("아직 승인되지 않은 강의입니다. info : [ lectureId = ${lecture.id} ]")

        if(lecture.getLectureStatus() == LectureStatus.CLOSE)
            throw NotAvailableSignUpDateException("수강신청이 가능한 시간이 아닙니다. info : [ lectureId = ${lecture.id}, currentTime = ${LocalDateTime.now()} ]")

        if(registeredLectureRepository.existsOne(student.id, lecture.id))
            throw AlreadySignedUpLectureException("이미 신청한 강의입니다. info : [ lectureId = ${lecture.id}, studentId = ${student.id} ]")

        val currentSignUpLectureStudent = registeredLectureRepository.countByLecture(lecture)

        if(lecture.maxRegisteredUser <= currentSignUpLectureStudent)
            throw OverMaxRegisteredUserException("수강 인원이 가득 찼습니다. info : [ maxRegisteredUser = ${lecture.maxRegisteredUser}, currentSignUpLectureStudent = $currentSignUpLectureStudent ]")

        val registeredLecture = RegisteredLecture(
            id = UUID.randomUUID(),
            student = student,
            lecture = lecture,
            completeDate = lecture.completeDate
        )

        registeredLectureRepository.save(registeredLecture)

        val updateCreditStudent = Student(
            id = student.id,
            user = student.user,
            club = student.club,
            grade = student.grade,
            classRoom = student.classRoom,
            number = student.number,
            cohort = student.cohort,
            credit = student.credit + lecture.credit,
            studentRole = student.studentRole
        )

        studentRepository.save(updateCreditStudent)
    }

    /**
     * 강의 개설 신청을 수락하는 비지니스 로직입니다.
     * @param 개설을 수락할 대기 상태의 강의 id
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun approveLecture(id: UUID) {
        val lecture = lectureRepository findById id

        if(lecture.approveStatus == ApproveStatus.APPROVED)
            throw AlreadyApprovedLectureException("이미 개설 신청이 승인된 강의입니다. info : [ lectureId = $id ]")

        val approveLecture = Lecture(
            id = lecture.id,
            user = lecture.user,
            name = lecture.name,
            startDate = lecture.startDate,
            endDate = lecture.endDate,
            completeDate = lecture.completeDate,
            content = lecture.content,
            lectureType = lecture.lectureType,
            credit = lecture.credit,
            instructor = lecture.instructor,
            maxRegisteredUser = lecture.maxRegisteredUser,
            approveStatus = ApproveStatus.APPROVED
        )

        lectureRepository.save(approveLecture)
    }

    /**
     * 강의 개설 신청을 거절하는 비지니스 로직입니다.
     * @param 개설을 거절할 대기 상태의 강의 id
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun rejectLecture(id: UUID) {
        val lecture = lectureRepository findById id

        if(lecture.approveStatus == ApproveStatus.APPROVED)
            throw AlreadyApprovedLectureException("이미 개설 신청이 승인된 강의입니다. info : [ lectureId = $id ]")

        lectureRepository.delete(lecture)
    }

    private infix fun LectureRepository.findById(id: UUID): Lecture = this.findByIdOrNull(id)
        ?: throw LectureNotFoundException("존재하지 않는 강의입니다. info : [ lectureId = $id ]")

    private infix fun StudentRepository.findByUser(user: User): Student = this.findByUser(user)
        ?: throw StudentNotFoundException("학생을 찾을 수 없습니다. info : [ userId = ${user.id} ]")
}