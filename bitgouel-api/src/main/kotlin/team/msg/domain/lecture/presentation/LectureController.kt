package team.msg.domain.lecture.presentation

import javax.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.msg.domain.lecture.mapper.LectureRequestMapper
import team.msg.domain.lecture.presentation.data.response.DepartmentsResponse
import team.msg.domain.lecture.presentation.data.response.InstructorsResponse
import team.msg.domain.lecture.presentation.data.response.LecturesResponse
import team.msg.domain.lecture.presentation.data.response.LectureDetailsResponse
import team.msg.domain.lecture.presentation.data.response.LinesResponse
import team.msg.domain.lecture.presentation.data.web.CreateLectureWebRequest
import team.msg.domain.lecture.presentation.data.web.QueryAllDepartmentsWebRequest
import team.msg.domain.lecture.presentation.data.web.QueryAllLecturesWebRequest
import team.msg.domain.lecture.presentation.data.web.QueryAllLinesWebRequest
import team.msg.domain.lecture.service.LectureService
import java.util.*

@RestController
@RequestMapping("/lecture")
class LectureController(
    private val lectureRequestMapper: LectureRequestMapper,
    private val lectureService: LectureService
) {
    @PostMapping
    fun createLecture(@Valid @RequestBody webRequest: CreateLectureWebRequest): ResponseEntity<Void> {
        val request = lectureRequestMapper.createLectureWebRequestToDto(webRequest)
        lectureService.createLecture(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping
    fun queryAllLectures(pageable: Pageable, WebRequest: QueryAllLecturesWebRequest): ResponseEntity<LecturesResponse>{
        val request = lectureRequestMapper.queryLectureWebRequestToDto(WebRequest)
        val response = lectureService.queryAllLectures(pageable, request)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @PostMapping("/{id}")
    fun signUpLecture(@PathVariable id: UUID): ResponseEntity<Void> {
        lectureService.signUpLecture(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping("/{id}")
    fun cancelSignUpLecture(@PathVariable id: UUID): ResponseEntity<Void> {
        lectureService.cancelSignUpLecture(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/{id}")
    fun queryLectureDetails(@PathVariable id: UUID): ResponseEntity<LectureDetailsResponse> {
        val response = lectureService.queryLectureDetails(id)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/instructor")
    fun queryInstructors(@RequestParam keyword: String): ResponseEntity<InstructorsResponse> {
        val response = lectureService.queryInstructors(keyword)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/line")
    fun queryAllLines(webRequest: QueryAllLinesWebRequest): ResponseEntity<LinesResponse> {
        val request = lectureRequestMapper.queryAllLinesWebRequestToDto(webRequest)
        val response = lectureService.queryAllLines(request)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/department")
    fun queryAllDepartments(webRequest: QueryAllDepartmentsWebRequest): ResponseEntity<DepartmentsResponse> {
        val request = lectureRequestMapper.queryAllDepartmentsWebRequestToDto(webRequest)
        val response = lectureService.queryAllDepartments(request)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/excel")
    fun lectureReceiptStatusExcel(): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        headers.setContentDispositionFormData("attachment", "sample.xlsx")
        val result = lectureService.lectureReceiptStatusExcel()

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(result)
    }
}