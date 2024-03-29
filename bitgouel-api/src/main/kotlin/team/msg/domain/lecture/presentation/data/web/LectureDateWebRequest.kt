package team.msg.domain.lecture.presentation.data.web

import com.fasterxml.jackson.annotation.JsonFormat
import javax.validation.constraints.Future
import javax.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class LectureDateWebRequest(
    @field:NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val completeDate: LocalDate,

    @field:NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    val startTime: LocalTime,

    @field:NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    val endTime: LocalTime
)
