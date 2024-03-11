package team.msg.domain.lecture.presentation.data.web

import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull
import org.springframework.web.bind.annotation.RequestParam
import team.msg.domain.lecture.enums.Division

data class QueryAllLinesWebRequest (
    @field:NotNull
    @Enumerated(EnumType.STRING)
    val division: Division,

    @RequestParam(required = false)
    val keyword: String = ""
)