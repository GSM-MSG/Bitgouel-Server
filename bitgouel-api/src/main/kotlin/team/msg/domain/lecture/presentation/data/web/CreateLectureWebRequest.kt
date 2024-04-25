package team.msg.domain.lecture.presentation.data.web

import com.fasterxml.jackson.annotation.JsonFormat
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.Valid
import javax.validation.constraints.Future
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import team.msg.domain.lecture.enums.Semester
import java.time.LocalDateTime
import java.util.UUID

data class CreateLectureWebRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val content: String,

    @field:NotBlank
    val semester: Semester,

    @field:NotBlank
    val division: String,

    @field:NotBlank
    val department: String,

    @field:NotBlank
    val line: String,

    @field:NotNull
    val userId: UUID,

    @field:NotNull
    @FutureOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime,

    @field:NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime,

    @Valid
    @field:NotNull
    val lectureDates: List<LectureDateWebRequest>,

    @field:NotBlank
    val lectureType: String,

    @field:NotNull
    @field:Min(0)
    val credit: Int,

    @field:NotNull
    @field:Min(5)
    @field:Max(10)
    val maxRegisteredUser: Int
)