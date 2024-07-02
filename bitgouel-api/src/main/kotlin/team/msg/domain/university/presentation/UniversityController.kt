package team.msg.domain.university.presentation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.msg.domain.university.presentation.data.response.UniversitiesResponse
import team.msg.domain.university.service.UniversityService

@RestController
@RequestMapping("/university")
class UniversityController(
    private val universityService: UniversityService
) {
    @GetMapping
    fun queryUniversities(): ResponseEntity<UniversitiesResponse> {
        val response = universityService.queryUniversities()
        return ResponseEntity.ok(response)
    }
}