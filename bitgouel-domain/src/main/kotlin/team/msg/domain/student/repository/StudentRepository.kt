package team.msg.domain.student.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import team.msg.domain.club.model.Club
import team.msg.domain.student.model.Student
import team.msg.domain.user.model.User
import java.util.*

interface StudentRepository : CrudRepository<Student, UUID> {

    @EntityGraph(attributePaths = ["user"], type = EntityGraph.EntityGraphType.FETCH)
    fun findByUser(user: User): Student?
    @EntityGraph(attributePaths = ["user"], type = EntityGraph.EntityGraphType.FETCH)
    fun findStudentById(id: UUID): Student?
    @EntityGraph(attributePaths = ["user"], type = EntityGraph.EntityGraphType.FETCH)
    fun findByIdAndClub(id: UUID, club: Club): Student?
    fun countByClub(club: Club): Long
    fun findAllByClub(club: Club): List<Student>
}
