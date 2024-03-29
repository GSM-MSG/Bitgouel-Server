package team.msg.domain.lecture.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import team.msg.common.entity.BaseUUIDEntity
import team.msg.domain.lecture.enums.Division
import team.msg.domain.lecture.enums.LectureStatus
import team.msg.domain.lecture.enums.LectureType
import team.msg.domain.lecture.enums.Semester
import team.msg.domain.user.model.User
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Lecture(

    @get:JvmName("getIdentifier")
    override var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)")
    val user: User,

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    val semester: Semester,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    val division: Division,

    @Column(columnDefinition = "VARCHAR(25)", nullable = false)
    val department: String,

    @Column(columnDefinition = "VARCHAR(25)", nullable = false)
    val line: String,

    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    val startDate: LocalDateTime,

    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    val endDate: LocalDateTime,

    @Column(columnDefinition = "VARCHAR(1000)", nullable = false)
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(40)", nullable = false)
    val lectureType: LectureType,

    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    val credit: Int,

    @Column(columnDefinition = "VARCHAR(10)", nullable = false)
    val instructor: String,
) : BaseUUIDEntity(id) {
    fun getLectureStatus(): LectureStatus {
        val currentTime = LocalDateTime.now()

        return if(currentTime.isAfter(startDate) && currentTime.isBefore(endDate))
            LectureStatus.OPENED
        else
            LectureStatus.CLOSED
    }
}