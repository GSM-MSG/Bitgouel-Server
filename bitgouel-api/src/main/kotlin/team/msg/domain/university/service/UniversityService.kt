package team.msg.domain.university.service

import team.msg.domain.university.presentation.data.response.UniversitiesResponse

interface UniversityService {
    fun queryUniversities(): UniversitiesResponse
}