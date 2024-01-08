package team.msg.domain.student.repository.custom

import java.util.*

interface CustomStudentActivityHistoryRepository {
    fun deleteAllByStudentId(studentId: UUID)
    fun deleteAllByStudentActivityId(studentActivityId: UUID)
}