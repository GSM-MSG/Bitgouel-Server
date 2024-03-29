package team.msg.domain.lecture.service

import com.appmattus.kotlinfixture.kotlinFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import team.msg.common.util.UserUtil
import team.msg.domain.company.model.CompanyInstructor
import team.msg.domain.government.model.Government
import team.msg.domain.lecture.enums.Division
import team.msg.domain.lecture.enums.LectureStatus
import team.msg.domain.lecture.enums.LectureType
import team.msg.domain.lecture.enums.Semester
import team.msg.domain.lecture.exception.AlreadySignedUpLectureException
import team.msg.domain.lecture.exception.NotAvailableSignUpDateException
import team.msg.domain.lecture.exception.OverMaxRegisteredUserException
import team.msg.domain.lecture.exception.UnSignedUpLectureException
import team.msg.domain.lecture.model.Lecture
import team.msg.domain.lecture.model.LectureDate
import team.msg.domain.lecture.model.RegisteredLecture
import team.msg.domain.lecture.model.RegisteredLectureCount
import team.msg.domain.lecture.presentation.data.request.CreateLectureRequest
import team.msg.domain.lecture.presentation.data.request.QueryAllDepartmentsRequest
import team.msg.domain.lecture.presentation.data.request.QueryAllLectureRequest
import team.msg.domain.lecture.presentation.data.request.QueryAllLinesRequest
import team.msg.domain.lecture.presentation.data.response.*
import team.msg.domain.lecture.repository.LectureDateRepository
import team.msg.domain.lecture.repository.LectureRepository
import team.msg.domain.lecture.repository.RegisteredLectureCountRepository
import team.msg.domain.lecture.repository.RegisteredLectureRepository
import team.msg.domain.professor.model.Professor
import team.msg.domain.professor.repository.ProfessorRepository
import team.msg.domain.student.exception.StudentNotFoundException
import team.msg.domain.student.model.Student
import team.msg.domain.student.repository.StudentRepository
import team.msg.domain.teacher.repository.TeacherRepository
import team.msg.domain.user.enums.Authority
import team.msg.domain.user.exception.UserNotFoundException
import team.msg.domain.user.model.User
import team.msg.domain.user.repository.UserRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class LectureServiceImplTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf
    val fixture = kotlinFixture()

    val lectureRepository = mockk<LectureRepository>()
    val lectureDateRepository = mockk<LectureDateRepository>()
    val registeredLectureRepository = mockk<RegisteredLectureRepository>()
    val registeredLectureCountRepository = mockk<RegisteredLectureCountRepository>()
    val studentRepository = mockk<StudentRepository>()
    val teacherRepository = mockk<TeacherRepository>()
    val professorRepository = mockk<ProfessorRepository>()
    val userRepository = mockk<UserRepository>()
    val userUtil = mockk<UserUtil>()
    val pageable = mockk<Pageable>()
    val lectureServiceImpl = LectureServiceImpl(
        lectureRepository,
        lectureDateRepository,
        registeredLectureRepository,
        registeredLectureCountRepository,
        studentRepository,
        teacherRepository,
        professorRepository,
        userRepository,
        userUtil
    )

    // createLecture 테스트 코드
    Given("CreateLectureRequest 가 주어질 때") {

        val user = fixture<User>()
        val request = fixture<CreateLectureRequest>()
        val lecture = fixture<Lecture>()
        val registeredLectureCount = fixture<RegisteredLectureCount>()
        val lectureDate = fixture<LectureDate>()
        val lectureDates = mutableListOf(lectureDate)

        every { userRepository.findByIdOrNull(any()) } returns user
        every { lectureRepository.save(any()) } returns lecture
        every { registeredLectureCountRepository.save(any()) } returns registeredLectureCount
        every { lectureDateRepository.saveAll(any<List<LectureDate>>()) } returns lectureDates

        When("Lecture 등록 요청을 하면") {
            lectureServiceImpl.createLecture(request)

            Then("Lecture 가 저장이 되어야 한다.") {
                verify(exactly = 1) { lectureRepository.save(any()) }
            }

            Then("registeredLectureCount 가 저장이 되어야 한다.") {
                verify(exactly = 1) { registeredLectureCountRepository.save(any()) }
            }

            Then("LectureDate 가 저장이 되어야 한다.") {
                verify(exactly = 1) { lectureDateRepository.saveAll(any<List<LectureDate>>()) }
            }
        }

        When("존재하지 않는 유저 id로 요청을 하면") {
            every { userRepository.findByIdOrNull(any()) } returns null

            Then("UserNotFoundException 예외가 발생해야 한다.") {
                shouldThrow<UserNotFoundException> {
                    lectureServiceImpl.createLecture(request)
                }
            }

        }
    }

    //queryAllLectures 테스트 코드
    Given("queryAllLectureRequest와 Pageable가 주어질 때") {

        val queryAllLectureRequest = fixture<QueryAllLectureRequest>()

        val name = "name"
        val content = "content"
        val instructor = "instructor"
        val headCount = 0
        val maxRegisteredUser = 5
        val startDate = LocalDateTime.MIN
        val endDate = LocalDateTime.MAX
        val completeDate = LocalDateTime.MAX
        val lectureStatus = LectureStatus.OPENED
        val semester = Semester.FIRST_YEAR_FALL_SEMESTER
        val division = Division.AUTOMOBILE_INDUSTRY
        val line = "line"
        val department = "department"

        val creditLectureId = UUID.randomUUID()
        val creditLectureType = LectureType.MUTUAL_CREDIT_RECOGNITION_PROGRAM

        val universityLectureId = UUID.randomUUID()
        val universityLectureType = LectureType.UNIVERSITY_EXPLORATION_PROGRAM

        val creditLecture = fixture<Lecture> {
            property(Lecture::id) { creditLectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { creditLectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { endDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::semester) { semester }
            property(Lecture::division) { division }
            property(Lecture::line) { line }
            property(Lecture::department) { department }
        }
        val universityLecture = fixture<Lecture> {
            property(Lecture::id) { universityLectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { universityLectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { endDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::semester) { semester }
            property(Lecture::division) { division }
            property(Lecture::line) { line }
            property(Lecture::department) { department }
        }
        val registeredLectureCount = fixture<RegisteredLectureCount> {
            property(RegisteredLectureCount::registeredUser) { headCount }
            property(RegisteredLectureCount::maxRegisteredUser) { maxRegisteredUser }
        }

        val creditLectureResponse = fixture<LectureResponse> {
            property(LectureResponse::id) { creditLectureId }
            property(LectureResponse::name) { name }
            property(LectureResponse::content) { content }
            property(LectureResponse::lectureType) { creditLectureType }
            property(LectureResponse::headCount) { headCount }
            property(LectureResponse::maxRegisteredUser) { maxRegisteredUser }
            property(LectureResponse::startDate) { startDate }
            property(LectureResponse::endDate) { endDate }
            property(LectureResponse::lecturer) { instructor }
            property(LectureResponse::lectureStatus) { lectureStatus }
            property(LectureResponse::semester) { semester }
            property(LectureResponse::division) { division }
            property(LectureResponse::line) { line }
            property(LectureResponse::department) { department }
        }
        val universityLectureResponse = fixture<LectureResponse> {
            property(LectureResponse::id) { universityLectureId }
            property(LectureResponse::name) { name }
            property(LectureResponse::content) { content }
            property(LectureResponse::lectureType) { universityLectureType }
            property(LectureResponse::headCount) { headCount }
            property(LectureResponse::maxRegisteredUser) { maxRegisteredUser }
            property(LectureResponse::startDate) { startDate }
            property(LectureResponse::endDate) { endDate }
            property(LectureResponse::lecturer) { instructor }
            property(LectureResponse::lectureStatus) { lectureStatus }
            property(LectureResponse::semester) { semester }
            property(LectureResponse::division) { division }
            property(LectureResponse::line) { line }
            property(LectureResponse::department) { department }
        }

        every { registeredLectureCountRepository.findByLecture(any()) } returns registeredLectureCount

        When("주어진 LectureType이 null이라면") {
            every { lectureRepository.findAllByLectureType(any(), any()) } returns PageImpl(listOf(creditLecture, universityLecture))

            val response = fixture<LecturesResponse> {
                property(LecturesResponse::lectures) { PageImpl(listOf(creditLectureResponse, universityLectureResponse)) }
            }

            val result = lectureServiceImpl.queryAllLectures(pageable, queryAllLectureRequest)

            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }

        When("주어진 LectureType이 상호학점인정과정이라면") {
            every { lectureRepository.findAllByLectureType(any(), any()) } returns PageImpl(listOf(creditLecture))

            val response = fixture<LecturesResponse> {
                property(LecturesResponse::lectures) { PageImpl(listOf(creditLectureResponse)) }
            }

            val result = lectureServiceImpl.queryAllLectures(pageable, queryAllLectureRequest)

            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }

        When("주어진 LectureType이 대학탐방프로그램이라면") {
            every { lectureRepository.findAllByLectureType(any(), any()) } returns PageImpl(listOf(universityLecture))

            val response = fixture<LecturesResponse> {
                property(LecturesResponse::lectures) { PageImpl(listOf(universityLectureResponse)) }
            }

            val result = lectureServiceImpl.queryAllLectures(pageable, queryAllLectureRequest)

            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }
    }

    // queryLectureDetails 테스트 코드
    Given("Lecture id가 주어질 때") {

        val userId = UUID.randomUUID()
        val studentAuthority = Authority.ROLE_STUDENT
        val user = fixture<User> {
            property(User::id) { userId }
            property(User::authority) { studentAuthority }
        }

        val studentId = UUID.randomUUID()
        val student = fixture<Student> {
            property(Student::id) { studentId }
            property(Student::user) { user }
        }

        val lectureId = UUID.randomUUID()
        val name = "name"
        val content = "content"
        val instructor = "instructor"
        val headCount = 0
        val maxRegisteredUser = 5
        val credit = 2
        val startDate = LocalDateTime.MIN
        val endDate = LocalDateTime.MAX
        val lectureStatus = LectureStatus.OPENED
        val lectureType = LectureType.MUTUAL_CREDIT_RECOGNITION_PROGRAM
        val isRegistered = false
        val semester = Semester.FIRST_YEAR_FALL_SEMESTER
        val division = Division.AUTOMOBILE_INDUSTRY
        val line = "line"
        val department = "department"
        val completeDate = LocalDate.MAX
        val startTime = LocalTime.MIN
        val endTime = LocalTime.MAX
        val lectureDate = fixture<LectureDate> {
            property(LectureDate::completeDate) { completeDate }
            property(LectureDate::startTime) { startTime }
            property(LectureDate::endTime) { endTime }
        }
        val lectureDates = mutableListOf(lectureDate)

        val lecture = fixture<Lecture> {
            property(Lecture::id) { lectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { lectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { endDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::credit) { credit }
            property(Lecture::semester) { semester }
            property(Lecture::division) { division }
            property(Lecture::line) { line }
            property(Lecture::department) { department }
        }

        val registeredLectureCount = fixture<RegisteredLectureCount> {
            property(RegisteredLectureCount::registeredUser) { 0 }
            property(RegisteredLectureCount::maxRegisteredUser) { maxRegisteredUser }
            property(RegisteredLectureCount::lecture) { lecture }
        }

        val lectureDateResponse = fixture<LectureDateResponse> {
            property(LectureDateResponse::completeDate) { completeDate }
            property(LectureDateResponse::startTime) { startTime }
            property(LectureDateResponse::endTime) { endTime }
        }

        val lectureDateResponses = mutableListOf(lectureDateResponse)

        val response = fixture<LectureDetailsResponse> {
            property(LectureDetailsResponse::name) { name }
            property(LectureDetailsResponse::content) { content }
            property(LectureDetailsResponse::lectureType) { lectureType }
            property(LectureDetailsResponse::headCount) { headCount }
            property(LectureDetailsResponse::maxRegisteredUser) { maxRegisteredUser }
            property(LectureDetailsResponse::startDate) { startDate }
            property(LectureDetailsResponse::endDate) { endDate }
            property(LectureDetailsResponse::lectureDates) { lectureDateResponses }
            property(LectureDetailsResponse::lecturer) { instructor }
            property(LectureDetailsResponse::lectureStatus) { lectureStatus }
            property(LectureDetailsResponse::isRegistered) { isRegistered }
            property(LectureDetailsResponse::createAt) { lecture.createdAt }
            property(LectureDetailsResponse::credit) { credit }
            property(LectureDetailsResponse::semester) { semester }
            property(LectureDetailsResponse::division) { division }
            property(LectureDetailsResponse::line) { line }
            property(LectureDetailsResponse::department) { department }
        }

        every { userUtil.queryCurrentUser() } returns user
        every { lectureRepository.findByIdOrNull(lectureId) } returns lecture
        every { lectureDateRepository.findAllByLecture(lecture) } returns lectureDates
        every { studentRepository.findByUser(any()) } returns student
        every { registeredLectureRepository.existsOne(any(), any()) } returns isRegistered
        every { registeredLectureCountRepository.findByLecture(any()) } returns registeredLectureCount

        When("강의 상세 정보를 조회하면") {
            val result = lectureServiceImpl.queryLectureDetails(lectureId)

            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }

        When("조회한 학생이 신청한 강의라면") {
            every { registeredLectureRepository.existsOne(any(), any()) } returns true

            val result = lectureServiceImpl.queryLectureDetails(lectureId)

            Then("isResistered가 true여야 한다."){
                result.isRegistered shouldBe true
            }
        }
    }

    //signUpLecture 테스트 코드
    Given("Lecture id가 주어질 때") {

        val userId = UUID.randomUUID()
        val studentAuthority = Authority.ROLE_STUDENT
        val user = fixture<User> {
            property(User::id) { userId }
            property(User::authority) { studentAuthority }
        }

        val studentId = UUID.randomUUID()
        val student = fixture<Student> {
            property(Student::id) { studentId }
            property(Student::user) { user }
        }

        val lectureId = UUID.randomUUID()
        val name = "name"
        val content = "content"
        val instructor = "instructor"
        val maxRegisteredUser = 5
        val credit = 2
        val headCount = 0
        val startDate = LocalDateTime.MIN
        val endDate = LocalDateTime.MAX
        val lectureType = LectureType.MUTUAL_CREDIT_RECOGNITION_PROGRAM
        val lectureDate = fixture<LectureDate>()

        val lecture = fixture<Lecture> {
            property(Lecture::id) { lectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { lectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { endDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::credit) { credit }
        }

        val registeredLectureCount = fixture<RegisteredLectureCount> {
            property(RegisteredLectureCount::registeredUser) { 0 }
            property(RegisteredLectureCount::maxRegisteredUser) { maxRegisteredUser }
            property(RegisteredLectureCount::lecture) { lecture }
        }

        val missDateLectureId = UUID.randomUUID()
        val missEndDate = LocalDateTime.MIN

        val missDateLecture = fixture<Lecture> {
            property(Lecture::id) { missDateLectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { lectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { missEndDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::credit) { credit }
        }

        val fullRegisteredLectureCount = fixture<RegisteredLectureCount> {
            property(RegisteredLectureCount::registeredUser) { maxRegisteredUser }
            property(RegisteredLectureCount::maxRegisteredUser) { maxRegisteredUser }
        }

        val registeredLecture = fixture<RegisteredLecture>()

        every { userUtil.queryCurrentUser() } returns user
        every { studentRepository.findByUser(any()) } returns student
        every { lectureRepository.findByIdOrNull(lectureId) } returns lecture
        every { registeredLectureRepository.existsOne(any(), any()) } returns false
        every { registeredLectureRepository.save(any()) } returns registeredLecture
        every { studentRepository.save(any()) } returns student
        every { registeredLectureCountRepository.save(any()) } returns registeredLectureCount
        every { registeredLectureCountRepository.findByLecture(lecture) } returns registeredLectureCount


        When("학생이 강의 수강 신청을 하면") {
            lectureServiceImpl.signUpLecture(lectureId)

            Then("RegisteredLecture 가 저장되어야 한다.") {
                verify(exactly = 1) { registeredLectureRepository.save(any()) }
            }

            Then("Student가 저장되어야 한다.") {
                verify(exactly = 1) { studentRepository.save(any()) }
            }
        }

        When("현재 유저가 학생이 아니라면") {
            every { studentRepository.findByUser(user) } returns null

            Then("StudentNotFoundException이 발생해야 한다.") {
                shouldThrow<StudentNotFoundException> {
                    lectureServiceImpl.signUpLecture(lectureId)
                }
            }
        }

        When("수강 신청 시간이 아닌 강의에 수강 신청을 하면") {
            every { lectureRepository.findByIdOrNull(missDateLectureId) } returns missDateLecture

            Then("NotAvailableSignUpDateException이 발생해야 한다.") {
                shouldThrow<NotAvailableSignUpDateException> {
                    lectureServiceImpl.signUpLecture(missDateLectureId)
                }
            }
        }

        When("이미 수강 신청한 강의에 수강 신청을 하면") {
            every { registeredLectureRepository.existsOne(any(), any()) } returns true

            Then("AlreadySignedUpLectureException이 발생해야 한다.") {
                shouldThrow<AlreadySignedUpLectureException> {
                    lectureServiceImpl.signUpLecture(lectureId)
                }
            }
        }

        When("수강 인원이 가득 찬 강의에 수강 신청을 하면") {
            every { registeredLectureCountRepository.findByLecture(any()) } returns fullRegisteredLectureCount

            Then("OverMaxRegisteredUserException이 발생해야 한다.") {
                shouldThrow<OverMaxRegisteredUserException> {
                    lectureServiceImpl.signUpLecture(lectureId)
                }
            }
        }
    }

    //cancelSignUpLecture 테스트 코드
    Given("Lecture id가 주어질 때") {

        val userId = UUID.randomUUID()
        val studentAuthority = Authority.ROLE_STUDENT
        val user = fixture<User> {
            property(User::id) { userId }
            property(User::authority) { studentAuthority }
        }

        val studentId = UUID.randomUUID()
        val student = fixture<Student> {
            property(Student::id) { studentId }
            property(Student::user) { user }
        }

        val lectureId = UUID.randomUUID()
        val name = "name"
        val content = "content"
        val instructor = "instructor"
        val maxRegisteredUser = 5
        val credit = 2
        val startDate = LocalDateTime.MIN
        val endDate = LocalDateTime.MAX
        val lectureType = LectureType.MUTUAL_CREDIT_RECOGNITION_PROGRAM

        val lecture = fixture<Lecture> {
            property(Lecture::id) { lectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { lectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { endDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::credit) { credit }
        }

        val missDateLectureId = UUID.randomUUID()
        val missEndDate = LocalDateTime.MIN

        val missDateLecture = fixture<Lecture> {
            property(Lecture::id) { missDateLectureId }
            property(Lecture::name) { name }
            property(Lecture::content) { content }
            property(Lecture::lectureType) { lectureType }
            property(Lecture::startDate) { startDate }
            property(Lecture::endDate) { missEndDate }
            property(Lecture::instructor) { instructor }
            property(Lecture::credit) { credit }
        }

        val registeredLecture = fixture<RegisteredLecture>()

        every { userUtil.queryCurrentUser() } returns user
        every { studentRepository.findByUser(any()) } returns student
        every { lectureRepository.findByIdOrNull(lectureId) } returns lecture
        every { registeredLectureRepository.findByStudentAndLecture(any(), any()) } returns registeredLecture
        every { registeredLectureRepository.delete(any()) } returns Unit
        every { studentRepository.save(any()) } returns student


        When("학생이 강의 수강 신청을 취소하면") {
            lectureServiceImpl.cancelSignUpLecture(lectureId)

            Then("RegisteredLecture 가 삭제되어야 한다.") {
                verify(exactly = 1) { registeredLectureRepository.delete(any()) }
            }

            Then("Student가 저장되어야 한다.") {
                verify(exactly = 1) { studentRepository.save(any()) }
            }
        }

        When("현재 유저가 학생이 아니라면") {
            every { studentRepository.findByUser(user) } returns null

            Then("StudentNotFoundException이 발생해야 한다.") {
                shouldThrow<StudentNotFoundException> {
                    lectureServiceImpl.cancelSignUpLecture(lectureId)
                }
            }
        }

        When("수강 신청 시간이 아닌 강의에 수강 신청을 하면") {
            every { lectureRepository.findByIdOrNull(missDateLectureId) } returns missDateLecture

            Then("NotAvailableSignUpDateException이 발생해야 한다.") {
                shouldThrow<NotAvailableSignUpDateException> {
                    lectureServiceImpl.signUpLecture(missDateLectureId)
                }
            }
        }

        When("수강 신청을 하지 않았는데 수강 신청을 취소하면") {
            every { registeredLectureRepository.findByStudentAndLecture(any(), any()) } returns null

            Then("UnSignedUpLectureException이 발생해야 한다.") {
                shouldThrow<UnSignedUpLectureException> {
                    lectureServiceImpl.cancelSignUpLecture(lectureId)
                }
            }
        }
    }

    // queryInstructors 테스트 코드
    Given("강사와 keyword가 주어질 때"){
        val professorUserId = UUID.randomUUID()
        val professorUserName = "professor"
        val professorAuthority = Authority.ROLE_PROFESSOR
        val professorUser = fixture<User> {
            property(User::id) { professorUserId }
            property(User::name) { professorUserName }
            property(User::authority) { professorAuthority }
        }
        val university = "university"
        val professor = fixture<Professor> {
            property(Professor::user) { professorUser }
            property(Professor::university) { university }
        }
        val professorPair = Pair(professorUser, university)
        val professorResponse = LectureResponse.instructorOf(professorUser, university)

        val companyInstructorUserId = UUID.randomUUID()
        val companyInstructorUserName = "companyInstructor"
        val companyInstructorAuthority = Authority.ROLE_COMPANY_INSTRUCTOR
        val companyInstructorUser = fixture<User> {
            property(User::id) { companyInstructorUserId }
            property(User::name) { companyInstructorUserName }
            property(User::authority) { companyInstructorAuthority }
        }
        val company = "company"
        val companyInstructor = fixture<CompanyInstructor> {
            property(CompanyInstructor::user) { companyInstructorUser }
            property(CompanyInstructor::company) { company }
        }
        val companyInstructorPair = Pair(companyInstructorUser, company)
        val companyInstructorResponse = LectureResponse.instructorOf(companyInstructorUser, company)

        val governmentUserName = "government"
        val governmentAuthority = Authority.ROLE_GOVERNMENT
        val governmentUser = fixture<User> {
            property(User::name) { governmentUserName }
            property(User::authority) { governmentAuthority }
        }
        val governmentId = UUID.randomUUID()
        val governmentName = "governmentName"
        val government = fixture<Government> {
            property(Government::id) { governmentId }
            property(Government::user) { governmentUser }
            property(Government::governmentName) { governmentName }
        }
        val governmentPair = Pair(governmentUser, governmentName)
        val governmentResponse = LectureResponse.instructorOf(governmentUser, governmentName)

        When("keyword가 빈 문자열일 때") {
            every { userRepository.queryInstructorsAndOrganization(any()) } returns listOf(professorPair, companyInstructorPair, governmentPair)

            val response = InstructorsResponse(listOf(professorResponse, companyInstructorResponse, governmentResponse))

            val keyword = ""

            val result = lectureServiceImpl.queryInstructors(keyword)
            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }

        When("keyword가 특정 강사의 이름이나 기관에 포함되는 문자열일 때") {
            every { userRepository.queryInstructorsAndOrganization(any()) } returns listOf(professorPair, companyInstructorPair)

            val response = InstructorsResponse(listOf(professorResponse, companyInstructorResponse))

            val keyword = "y"

            val result = lectureServiceImpl.queryInstructors(keyword)
            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }

    }

    // queryAllLines 테스트 코드
    Given("강의와 Division, keyword가 주어질 때"){
        val emptyKeyword = ""
        val keyword = "기"
        val division = Division.AUTOMOBILE_INDUSTRY

        val request = fixture<QueryAllLinesRequest> {
            property(QueryAllLinesRequest::keyword) { keyword }
            property(QueryAllLinesRequest::division) { division }
        }
        val emptyKeywordRequest = fixture<QueryAllLinesRequest> {
            property(QueryAllLinesRequest::keyword) { emptyKeyword }
            property(QueryAllLinesRequest::division) { division }
        }

        val lines = mutableListOf("기계")
        val emptyKeywordLines = mutableListOf("기계", "자동차")

        val response = LectureResponse.lineOf(lines)
        val emptyKeywordResponse = LectureResponse.lineOf(emptyKeywordLines)

        every { lectureRepository.findAllLineByDivision(division, keyword) } returns lines
        every { lectureRepository.findAllLineByDivision(division, emptyKeyword) } returns emptyKeywordLines
        When("keyqord가 빈 문자열일 때") {
            val result = lectureServiceImpl.queryAllLines(emptyKeywordRequest)
            Then("result와 response가 같아야 한다") {
                result shouldBe emptyKeywordResponse
            }
        }

        When("keyword가 특정 계열에 포함되는 문자열일 때"){
            val result = lectureServiceImpl.queryAllLines(request)
            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }
    }

    // queryAllDepartments 테스트 코드
    Given("강의와 keyword가 주어질 때"){
        val emptyKeyword = ""
        val keyword = "자"

        val request = fixture<QueryAllDepartmentsRequest> {
            property(QueryAllLinesRequest::keyword) { keyword }
        }
        val emptyKeywordRequest = fixture<QueryAllDepartmentsRequest> {
            property(QueryAllLinesRequest::keyword) { emptyKeyword }
        }

        val departments = mutableListOf("자동차공학")
        val emptyKeywordDepartments = mutableListOf("자동차공학", "기계공학")

        val response = LectureResponse.departmentOf(departments)
        val emptyKeywordResponse = LectureResponse.departmentOf(emptyKeywordDepartments)

        When("keyqord가 빈 문자열일 때") {
            every { lectureRepository.findAllDepartment(any()) } returns emptyKeywordDepartments
            val result = lectureServiceImpl.queryAllDepartments(emptyKeywordRequest)
            Then("result와 response가 같아야 한다") {
                result shouldBe emptyKeywordResponse
            }
        }

        When("keyword가 특정 학과에 포함되는 문자열일 때"){
            every { lectureRepository.findAllDepartment(any()) } returns departments
            val result = lectureServiceImpl.queryAllDepartments(request)
            Then("result와 response가 같아야 한다") {
                result shouldBe response
            }
        }
    }
})

