package team.msg.domain.club.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.msg.domain.club.presentation.data.response.AllClubResponse
import team.msg.domain.club.presentation.data.response.ClubResponse
import team.msg.domain.club.repository.ClubRepository
import team.msg.domain.school.enums.HighSchool
import team.msg.domain.school.exception.SchoolNotFoundException
import team.msg.domain.school.repository.SchoolRepository

@Service
class ClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val schoolRepository: SchoolRepository
) : ClubService {

    /**
     * 모든 동아리를 조회하는 비즈니스 로직
     * @param 동아리를 조회하기 위한 학교 이름
     */
    @Transactional(readOnly = true, rollbackFor = [Exception::class])
    override fun queryAllClubsService(highSchool: HighSchool): AllClubResponse {
        val school = schoolRepository.findByHighSchool(highSchool)
            ?: throw SchoolNotFoundException("존재하지 않는 학교 입니다. info : [ highSchool = $highSchool ]")

        val clubs = clubRepository.findAllBySchool(school)

        val response = AllClubResponse(
            ClubResponse.listOf(clubs)
        )

        return response
    }
}