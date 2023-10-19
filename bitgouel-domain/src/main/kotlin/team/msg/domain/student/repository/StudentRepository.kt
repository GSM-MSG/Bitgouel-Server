package team.msg.domain.student.repository

import org.springframework.data.repository.CrudRepository
import team.msg.domain.student.model.Student
import java.util.UUID

interface StudentRepository : CrudRepository<Student, UUID> {
}