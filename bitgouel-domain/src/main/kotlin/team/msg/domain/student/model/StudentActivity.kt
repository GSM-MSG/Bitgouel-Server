package team.msg.domain.student.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import team.msg.common.entity.BaseUUIDEntity
import team.msg.common.enum.ApproveStatus
import team.msg.domain.teacher.model.Teacher
import java.time.LocalDateTime
import java.util.*

@Entity
class StudentActivity(

    override val id: UUID,

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    val title: String,

    @Column(columnDefinition = "VARCHAR(1000)", nullable = false)
    val content: String,

    @Column(columnDefinition = "INT", nullable = false)
    val credit: Int,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(10)", nullable = false)
    val approveStatus: ApproveStatus,

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    val activityDate: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", columnDefinition = "BINARY(16)", nullable = false)
    val student: Student,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", columnDefinition = "BINARY(16)", nullable = false)
    val teacher: Teacher

) : BaseUUIDEntity(id){
}