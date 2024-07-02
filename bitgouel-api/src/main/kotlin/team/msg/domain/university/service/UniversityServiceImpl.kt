package team.msg.domain.university.service

import org.springframework.stereotype.Service
import team.msg.domain.university.presentation.data.response.UniversitiesResponse
import team.msg.domain.university.presentation.data.response.UniversityResponse
import team.msg.domain.university.repository.UniversityRepository

@Service
class UniversityServiceImpl(
    private val universityRepository: UniversityRepository
) : UniversityService {

    /**
     *  대학 리스트를 반환하는 비지니스 로직입니다.
     *  @return 대학 리스트
     */
    override fun queryUniversities(): UniversitiesResponse {
        val universities = universityRepository.findAll()

        val response = UniversitiesResponse(
            UniversityResponse.listOf(universities)
        )

        return response
    }
}