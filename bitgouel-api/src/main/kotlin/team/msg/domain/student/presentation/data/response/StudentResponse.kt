package team.msg.domain.student.presentation.data.response

import team.msg.domain.student.model.Student
import team.msg.domain.user.enums.Authority
import java.util.*

class StudentResponse(
    val id: UUID,
    val name: String,
    val authority: Authority
) {
    companion object {
        fun listOf(students: List<Student>) = students.map {
            StudentResponse(
                id = it.id,
                name = it.user!!.name,
                authority = it.user!!.authority
            )
        }

        fun detailOf(student: Student) = StudentDetailsResponse(
            name = student.user!!.name,
            phoneNumber = student.user!!.phoneNumber,
            email = student.user!!.email,
            credit = student.credit
        )
    }
}

data class AllStudentsResponse(
    val students: List<StudentResponse>
)

data class StudentDetailsResponse(
    val name: String,
    val phoneNumber: String,
    val email: String,
    val credit: Int
)