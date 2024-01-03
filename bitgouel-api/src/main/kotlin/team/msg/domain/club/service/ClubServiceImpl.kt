package team.msg.domain.club.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.msg.domain.club.exception.ClubNotFoundException
import team.msg.domain.club.presentation.data.response.*
import team.msg.domain.club.repository.ClubRepository
import team.msg.domain.school.enums.HighSchool
import team.msg.domain.school.exception.SchoolNotFoundException
import team.msg.domain.school.repository.SchoolRepository
import team.msg.domain.student.exception.StudentNotFoundException
import team.msg.domain.student.presentation.data.response.AllStudentsResponse
import team.msg.domain.student.presentation.data.response.StudentDetailsResponse
import team.msg.domain.student.presentation.data.response.StudentResponse
import team.msg.domain.student.repository.StudentRepository
import java.util.*

@Service
class ClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val schoolRepository: SchoolRepository,
    private val studentRepository: StudentRepository
) : ClubService {

    /**
     * 모든 동아리를 조회하는 비즈니스 로직
     * @param 동아리를 조회하기 위한 학교 이름
     * @return 학교에 있는 취업동아리 리스트
     */
    @Transactional(readOnly = true)
    override fun queryAllClubsService(highSchool: HighSchool): ClubsResponse {
        val school = schoolRepository.findByHighSchool(highSchool)
            ?: throw SchoolNotFoundException("존재하지 않는 학교 입니다. info : [ highSchool = $highSchool ]")

        val clubs = clubRepository.findAllBySchool(school)

        val response = ClubsResponse(
            ClubResponse.listOf(clubs)
        )

        return response
    }

    /**
     * 동아리를 상세 조회하는 비즈니스 로직
     * @param 동아리를 상세 조회하기 위한 id
     * @return 동아리 상세 정보를 담은 dto
     */
    @Transactional(readOnly = true)
    override fun queryClubDetailsService(id: Long): ClubDetailsResponse {
        val club = clubRepository.findByIdOrNull(id)
            ?: throw ClubNotFoundException("존재하지 않는 동아리 입니다. info : [ clubId = $id ]")

        val headCount = studentRepository.countByClub(club).toInt()

        val response = ClubResponse.detailOf(club, headCount)

        return response
    }

    /**
     * 동아리를의 학생 리스트를 조회하는 비즈니스 로직
     * @param 동아리에 속한 학생 리스트를 조회하기 위한 id
     * @return 동아리에 속한 학생 리스트를 담은 dto
     */
    @Transactional(readOnly = true)
    override fun queryAllStudentsByClubId(id: Long): AllStudentsResponse {
        val club = clubRepository.findByIdOrNull(id)
            ?: throw ClubNotFoundException("존재하지 않는 동아리 입니다. info : [ clubId = $id ]")

        val students = studentRepository.findAllByClub(club)

        val response = AllStudentsResponse(
            StudentResponse.listOf(students)
        )

        return response
    }

    /**
     * 동아리에 소속된 학생의 상세정보를 조회하는 비즈니스 로직
     * @param 동아리 id, 동아리에 속한 상세정보를 조회할 학생 id
     * @return 동아리에 속한 학생의 상세정보를 담은 dto
     */
    @Transactional(readOnly = true)
    override fun queryStudentDetails(clubId: Long, studentId: UUID): StudentDetailsResponse {
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw ClubNotFoundException("존재하지 않는 동아리입니다. info : [ clubId = $clubId ]")

        val student = studentRepository.findByIdAndClub(studentId, club)
            ?: throw StudentNotFoundException("동아리에 존재하지 않는 학생입니다. info : [ clubId = $clubId, studentId = $studentId ]")

        val response = StudentResponse.detailOf(student)

        return response
    }
}