package team.msg.domain.lecture.repository.custom

import java.util.*

interface CustomRegisteredLectureRepository {
    fun deleteAllByStudentId(studentId: UUID)
    fun existsOne(studentId: UUID,lectureId: UUID): Boolean
}